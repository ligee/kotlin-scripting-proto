package kotlin.script.jvm.impl

import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.addJvmSdkRoots
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.parsing.KotlinParserDefinition
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.script.KotlinScriptDefinition
import org.jetbrains.kotlin.utils.PathUtil
import kotlin.script.*
import kotlin.script.jvm.JvmScriptCompilerConfiguration
import kotlin.script.jvm.KJVMCompilerProxy
import kotlin.reflect.KClass
import kotlin.script.dependencies.Environment
import kotlin.script.dependencies.ScriptContents
import kotlin.script.experimental.dependencies.DependenciesResolver
import kotlin.script.experimental.dependencies.ScriptDependencies

class KJVMCompilerImpl(val scriptDefinition: KotlinScriptDefinition): KJVMCompilerProxy {
    override fun compile(scriptCompilerConfiguration: JvmScriptCompilerConfiguration): ResultWithDiagnostics<CompiledScript<*>> {
        val messageCollector = ScriptDiagnosticsMessageCollector()
        fun failure(vararg diagnostics: ScriptDiagnostic): ResultWithDiagnostics.Failure<CompiledScript<*>> =
                ResultWithDiagnostics.Failure(*messageCollector.diagnostics.toTypedArray(), *diagnostics)
        try {
            val disposable = Disposer.newDisposable()
            val kotlinCompilerConfiguration = org.jetbrains.kotlin.config.CompilerConfiguration().apply {
                add(JVMConfigurationKeys.SCRIPT_DEFINITIONS, BridgeScriptDefinition(scriptCompilerConfiguration))
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
            val psiFileFactory: PsiFileFactoryImpl = PsiFileFactory.getInstance(environment.project) as PsiFileFactoryImpl
            val scriptText = scriptCompilerConfiguration.scriptSourceFragments.getMergedScriptText()
            val scriptFileName = "script" // TODO: extract from file/url if available
            val virtualFile = LightVirtualFile("$scriptFileName${KotlinParserDefinition.STD_SCRIPT_EXT}", KotlinLanguage.INSTANCE, StringUtil.convertLineSeparators(scriptText)).apply {
                charset = CharsetToolkit.UTF8_CHARSET
            }
            val psiFile: KtFile = psiFileFactory.trySetupPsiForFile(virtualFile, KotlinLanguage.INSTANCE, true, false) as KtFile?
                    ?: return failure("Unable to make PSI file from script".asErrorDiagnostics())

            return failure("Not implemented yet".asErrorDiagnostics())
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

class BridgeDependenciesResolver(val scriptCompilerConfiguration: JvmScriptCompilerConfiguration) : DependenciesResolver {
    override fun resolve(scriptContents: ScriptContents, environment: Environment): DependenciesResolver.ResolveResult {
        return DependenciesResolver.ResolveResult.Success(ScriptDependencies(
                classpath = scriptCompilerConfiguration.dependencies.flatMap { it.classpath },
                imports = scriptCompilerConfiguration.importedPackages.toList()))
    }
}

class BridgeScriptDefinition(scriptCompilerConfiguration: JvmScriptCompilerConfiguration) : KotlinScriptDefinition(scriptCompilerConfiguration.scriptSignature.scriptBase.classifier as KClass<out Any>) {
    override val dependencyResolver: DependenciesResolver = BridgeDependenciesResolver(scriptCompilerConfiguration)
}
