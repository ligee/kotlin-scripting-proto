package org.jetbrains.kotlin.script.examples.jvm.simple

import org.jetbrains.kotlin.script.util.KotlinJars
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import kotlin.script.*
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
        PathUtil.getResourcePathForClass(MyScript::class.java).takeIf(File::exists)
            ?: throw Exception("Unable to get path to the script base")
    }

    override fun getInitialConfiguration(script: ScriptSource): ResultWithDiagnostics<ScriptCompileConfiguration> {
        return ScriptCompileConfiguration(
                ScriptCompileConfigurationParams.scriptSourceFragments to ScriptSourceFragments(script, null),
                ScriptCompileConfigurationParams.scriptSignature to ScriptSignature(MyScript::class, ProvidedDeclarations()),
                ScriptCompileConfigurationParams.importedPackages to emptyList<String>(),
                ScriptCompileConfigurationParams.restrictions to ResolvingRestrictions(),
                ScriptCompileConfigurationParams.importedScripts to emptyList<String>(),
                ScriptCompileConfigurationParams.dependencies to listOf(
                        JvmDependency(listOf(stdlibFile)),
                        JvmDependency(listOf(selfFile))),
                ScriptCompileConfigurationParams.compilerOptions to emptyList<String>(),
                JvmScriptCompileConfigurationParams.javaHomeDir to File(System.getProperty("java.home"))
        ).asSuccess()
    }

    override fun getUpdatedConfiguration(config: ScriptCompileConfiguration, processedScriptData: ProcessedScriptData): ResultWithDiagnostics<ScriptCompileConfiguration> =
            config.asSuccess()
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
                BasicJvmScriptRunner<MyScript>(),
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