package kotlin.script.jvm.impl

import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.addJvmSdkRoots
import org.jetbrains.kotlin.codegen.GeneratedClassLoader
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.script.KotlinScriptDefinition
import org.jetbrains.kotlin.utils.PathUtil
import kotlin.reflect.KClass
import kotlin.script.*
import kotlin.script.dependencies.Environment
import kotlin.script.dependencies.ScriptContents
import kotlin.script.experimental.dependencies.DependenciesResolver
import kotlin.script.experimental.dependencies.ScriptDependencies
import kotlin.script.experimental.dependencies.ScriptReport
import kotlin.script.jvm.JvmScriptCompileConfiguration
import kotlin.script.jvm.JvmScriptEvaluationEnvironment
import kotlin.script.jvm.KJVMCompilerProxy

class KJVMCompiledScript<out ScriptBase: Any>(override val configuration: JvmScriptCompileConfiguration, val generationState: GenerationState, val scriptClassFQName: String)
    : CompiledScript<ScriptBase, JvmScriptCompileConfiguration> {

    override fun instantiate(scriptEvaluationEnvironment: ScriptEvaluationEnvironment): ResultWithDiagnostics<ScriptBase> {
        val env = scriptEvaluationEnvironment as? JvmScriptEvaluationEnvironment
                ?: return ResultWithDiagnostics.Failure("Expected JvmScriptEvaluationEnvironment, but got ${scriptEvaluationEnvironment.javaClass.name}".asErrorDiagnostics())

        return try {
            val classLoader = GeneratedClassLoader(generationState.factory, env.baseClassLoader)

            val clazz = classLoader.loadClass(scriptClassFQName)
            (clazz as? ScriptBase)?.asSuccess()
                    ?: ResultWithDiagnostics.Failure("Compiled class expected to be a subclass of the <ScriptBase>, but got ${clazz.javaClass.name}".asErrorDiagnostics())
        }
        catch (e: Throwable) {
            ResultWithDiagnostics.Failure(ScriptDiagnostic("Unable to instantiate class $scriptClassFQName", exception = e))
        }
    }
}

class KJVMCompilerImpl: KJVMCompilerProxy<JvmScriptCompileConfiguration> {

    override fun compile(scriptCompilerConfiguration: JvmScriptCompileConfiguration,
                         configurator: ScriptConfigurator<JvmScriptCompileConfiguration>): ResultWithDiagnostics<CompiledScript<*, JvmScriptCompileConfiguration>> {
        val messageCollector = ScriptDiagnosticsMessageCollector()

        fun failure(vararg diagnostics: ScriptDiagnostic): ResultWithDiagnostics.Failure<CompiledScript<*, JvmScriptCompileConfiguration>> =
                ResultWithDiagnostics.Failure(*messageCollector.diagnostics.toTypedArray(), *diagnostics)

        try {
            val disposable = Disposer.newDisposable()
            val kotlinCompilerConfiguration = org.jetbrains.kotlin.config.CompilerConfiguration().apply {
                add(JVMConfigurationKeys.SCRIPT_DEFINITIONS, BridgeScriptDefinition(scriptCompilerConfiguration, configurator))
                put<MessageCollector>(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)
                put(JVMConfigurationKeys.RETAIN_OUTPUT_IN_MEMORY, true)
                addJvmSdkRoots(PathUtil.getJdkClassesRootsFromJre(scriptCompilerConfiguration.javaHomeDir.canonicalPath))
                addJvmClasspathRoots(scriptCompilerConfiguration.dependencies.flatMap { it.classpath })
                put(CommonConfigurationKeys.MODULE_NAME, "kotlin-script")
                languageVersionSettings = LanguageVersionSettingsImpl(
                        LanguageVersion.LATEST_STABLE, ApiVersion.LATEST_STABLE, mapOf(AnalysisFlag.skipMetadataVersionCheck to true)
                )
            }
            val environment = KotlinCoreEnvironment.createForProduction(disposable, kotlinCompilerConfiguration, EnvironmentConfigFiles.JVM_CONFIG_FILES)

            val generationState = KotlinToJVMBytecodeCompiler.analyzeAndGenerate(environment) ?: return failure()

            val script = environment.getSourceFiles()[0].script ?: return failure("Script must be parsed".asErrorDiagnostics())

            val res = KJVMCompiledScript<Any>(scriptCompilerConfiguration, generationState, script.fqName.asString())

            return ResultWithDiagnostics.Success(res)
        }
        catch (ex: Throwable) {
            return failure(ex.asDiagnostics())
        }
    }
}

class ScriptDiagnosticsMessageCollector : MessageCollector {

    private val _diagnostics = arrayListOf<ScriptDiagnostic>()

    val diagnostics: List<ScriptDiagnostic> get() = _diagnostics

    override fun clear() {
        _diagnostics.clear()
    }

    override fun hasErrors(): Boolean =
            _diagnostics.any { it.severity == ScriptDiagnostic.Severity.ERROR }


    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation?) {
        val mappedSeverity = when (severity) {
            CompilerMessageSeverity.EXCEPTION,
            CompilerMessageSeverity.ERROR -> ScriptDiagnostic.Severity.ERROR
            CompilerMessageSeverity.STRONG_WARNING,
            CompilerMessageSeverity.WARNING -> ScriptDiagnostic.Severity.WARNING
            CompilerMessageSeverity.INFO -> ScriptDiagnostic.Severity.INFO
            CompilerMessageSeverity.LOGGING -> ScriptDiagnostic.Severity.DEBUG
            else -> null
        }
        if (mappedSeverity != null) {
            val mappedLocation = location?.let {
                ScriptSource.Location(ScriptSource.Position(it.line, it.column))
            }
            _diagnostics.add(ScriptDiagnostic(message, mappedSeverity, mappedLocation))
        }
    }
}

// A bridge to the current scripting

internal class ParsedScriptDataFromScriptContentsAdapter(val scriptContents: ScriptContents) : ParsedScriptData {
    override val annotations: Iterable<Annotation> get() = scriptContents.annotations
}

internal class BridgeDependenciesResolver(val scriptCompilerConfiguration: JvmScriptCompileConfiguration,
                                          val scriptConfigurator: ScriptConfiguratorExt<JvmScriptCompileConfiguration>
) : DependenciesResolver {

    override fun resolve(scriptContents: ScriptContents, environment: Environment): DependenciesResolver.ResolveResult {

        return try {
            val res = scriptConfigurator.updateCompilerConfigurationFromParsed(scriptCompilerConfiguration, ParsedScriptDataFromScriptContentsAdapter(scriptContents))

            when (res) {
                is ResultWithDiagnostics.Failure -> DependenciesResolver.ResolveResult.Failure(res.reports.map { ScriptReport(it.message) /* TODO: consider more precise mapping */ })
                is ResultWithDiagnostics.Success -> DependenciesResolver.ResolveResult.Success(ScriptDependencies(
                        classpath =  res.value!!.dependencies . flatMap { it.classpath }, // TODO: maybe it should return only increment from the initial config
                        imports = res.value!!.importedPackages.toList()))
            }
        }
        catch (e: Throwable) {
            DependenciesResolver.ResolveResult.Failure(ScriptReport(e.message ?: "unknown error $e"))
        }
    }
}

internal class BridgeScriptDefinition(scriptCompilerConfiguration: JvmScriptCompileConfiguration, scriptConfigurator: ScriptConfigurator<JvmScriptCompileConfiguration>)
    : KotlinScriptDefinition(scriptCompilerConfiguration.scriptSignature.scriptBase as KClass<out Any>)
{
    override val dependencyResolver: DependenciesResolver =
            if (scriptConfigurator is ScriptConfiguratorExt<JvmScriptCompileConfiguration>) BridgeDependenciesResolver(scriptCompilerConfiguration, scriptConfigurator)
            else DependenciesResolver.NoDependencies
}
