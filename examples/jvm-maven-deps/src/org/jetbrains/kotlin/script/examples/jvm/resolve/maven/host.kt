package org.jetbrains.kotlin.script.examples.jvm.resolve.maven

import org.jetbrains.kotlin.script.util.DependsOn
import org.jetbrains.kotlin.script.util.FilesAndMavenResolver
import org.jetbrains.kotlin.script.util.KotlinJars
import org.jetbrains.kotlin.script.util.Repository
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import kotlin.script.*
import kotlin.script.dependencies.ScriptContents
import kotlin.script.dependencies.ScriptDependenciesResolver
import kotlin.script.host.ScriptSelector
import kotlin.script.jvm.*
import kotlin.script.jvm.impl.KJVMCompilerImpl
import kotlin.script.jvm.runners.BasicJvmScriptRunner

class MyConfigurationExtractor : ScriptConfigurator, ScriptSelector {
    override val fileExtension: String = ".kts"

    override fun isKnownScript(script: ScriptSource): Boolean = true

    val stdlibFile: File by lazy {
        KotlinJars.stdlib ?: throw Exception("Unable to find kotlin stdlib, please specify it explicitly via \"kotlin.java.stdlib.jar\" property")
    }

    val selfFile: File by lazy {
        PathUtil.getResourcePathForClass(MyScriptWithMavenDeps::class.java).takeIf(File::exists)
            ?: throw Exception("Unable to get path to the script base")
    }

    val scriptUtilsJarFile: File by lazy {
        PathUtil.getResourcePathForClass(DependsOn::class.java).takeIf(File::exists)
                ?: throw Exception("Unable to get path to the kotlin-script-util.jar")
    }

    private val resolver by lazy { FilesAndMavenResolver() }

    override fun getInitialConfiguration(script: ScriptSource): ResultWithDiagnostics<ScriptCompileConfiguration> {
        return ScriptCompileConfiguration(
                ScriptCompileConfigurationParams.scriptSourceFragments to ScriptSourceFragments(script, null),
                ScriptCompileConfigurationParams.scriptSignature to ScriptSignature(MyScriptWithMavenDeps::class, ProvidedDeclarations()),
                ScriptCompileConfigurationParams.importedPackages to listOf(DependsOn::class.qualifiedName!!),
                ScriptCompileConfigurationParams.restrictions to ResolvingRestrictions(),
                ScriptCompileConfigurationParams.importedScripts to emptyList<String>(),
                ScriptCompileConfigurationParams.dependencies to listOf(
                        JvmDependency(listOf(stdlibFile)),
                        JvmDependency(listOf(selfFile)),
                        JvmDependency(listOf(scriptUtilsJarFile))),
                ScriptCompileConfigurationParams.compilerOptions to emptyList<String>(),
                JvmScriptCompileConfigurationParams.javaHomeDir to File(System.getProperty("java.home")),
                ScriptCompileConfigurationParams.updateConfigurationOnAnnotations to listOf(DependsOn::class, Repository::class)
        ).asSuccess()
    }

    override fun getUpdatedConfiguration(config: ScriptCompileConfiguration, processedScriptData: ProcessedScriptData): ResultWithDiagnostics<ScriptCompileConfiguration> {
        val annotations = processedScriptData[ProcessedScriptDataParams.annotations].toList()
        if (annotations.isEmpty()) return config.asSuccess()
        val scriptContents = object : ScriptContents {
            override val annotations: Iterable<Annotation> = annotations
            override val file: File? = null
            override val text: CharSequence? = null
        }
        val diagnostics = arrayListOf<ScriptDiagnostic>()
        fun report(severity: ScriptDependenciesResolver.ReportSeverity, message: String, position: ScriptContents.Position?) {
            diagnostics.add(ScriptDiagnostic(message, mapLegacyDiagnosticSeverity(severity), mapLegacyScriptPosition(position)))
        }
        try {
            val newDeps = resolver.resolve(scriptContents, emptyMap(), ::report, null).get()
                    ?: return config.asSuccess(diagnostics)
            val resolvedClasspath = newDeps.classpath.toList().takeIf { it.isNotEmpty() }
                    ?: return config.asSuccess(diagnostics)
            return ScriptCompileConfiguration(
                    config.data +
                            (ScriptCompileConfigurationParams.dependencies to
                                    config.data.get(ScriptCompileConfigurationParams.dependencies) as List<JvmDependency> + JvmDependency(resolvedClasspath))
            ).asSuccess(diagnostics)
        }
        catch (e: Throwable) {
            return ResultWithDiagnostics.Failure(*diagnostics.toTypedArray(), e.asDiagnostics())
        }
    }
}

fun main(vararg args: String) {
    if (args.size != 1) {
        println("usage: <app> <script file>")
    }
    else {
        val scriptFile = File(args[0])
        println("Executing script $scriptFile")

        val scriptCompiler = JvmScriptCompiler(KJVMCompilerImpl(), DummyCompiledJvmScriptCache())
        val configurationExtractor = MyConfigurationExtractor()
        val baseClassLoader = URLClassLoader(arrayOf(
                configurationExtractor.stdlibFile.toURI().toURL(),
                configurationExtractor.selfFile.toURI().toURL()))
        val host = JvmBasicScriptingHost(
                configurationExtractor,
                scriptCompiler,
                BasicJvmScriptRunner<MyScriptWithMavenDeps>(),
                JvmScriptEvaluationEnvironment(baseClassLoader))

        val script = object : ScriptSource {
            override val location: URL? get() = scriptFile.toURI().toURL()
            override val text: String? get() = null
        }

        val res = host.eval(script, ProvidedDeclarations(), emptyList())

        res.reports.forEach {
            println(" : ${it.message}" + if (it.exception == null) "" else ": ${it.exception}")
        }
    }
}