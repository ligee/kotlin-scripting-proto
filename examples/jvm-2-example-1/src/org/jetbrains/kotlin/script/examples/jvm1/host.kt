package org.jetbrains.kotlin.script.examples.jvm1

import kotlin.script.*
import kotlin.script.host.ScriptSelector
import kotlin.script.jvm.*
import kotlin.script.jvm.runners.BasicJvmScriptRunner
import java.io.File
import java.net.URL
import kotlin.script.jvm.impl.KJVMCompilerImpl

class MyConfigurationExtractor : ScriptConfigurator<JvmScriptCompileConfiguration>, ScriptSelector {

    override val fileExtension: String = ".kts"

    override fun isKnownScript(script: ScriptSource): Boolean = true

    override fun getCompilerConfiguration(script: ScriptSource): ResultWithDiagnostics<JvmScriptCompileConfiguration> {
        return JvmScriptCompileConfiguration(
                scriptSourceFragments = ScriptSourceFragments(script, null),
                scriptSignature = ScriptSignature(MyScript::class, ProvidedDeclarations()),
                importedPackages = emptyList(),
                restrictions = ResolvingRestrictions(),
                importedScripts = emptyList(),
                dependencies = emptyList(),
                compilerOptions = emptyList(),
                javaHomeDir = File(System.getProperty("java.home")),
                previousScriptCompilerConfiguration = null
        ).asSuccess()
    }
}

fun makeScriptingHost(compilerProxy: KJVMCompilerProxy<JvmScriptCompileConfiguration>,
                      cache: CompiledJvmScriptsCache<JvmScriptCompileConfiguration>,
                      environment: ScriptEvaluationEnvironment)
  : JvmBasicScriptingHost<MyScript, JvmScriptCompileConfiguration, ScriptEvaluationEnvironment, JvmCompiledScript<MyScript, JvmScriptCompileConfiguration>>
{
    val scriptCompiler = JvmScriptCompiler(compilerProxy, cache)
    return JvmBasicScriptingHost(
            MyConfigurationExtractor(),
            scriptCompiler,
            BasicJvmScriptRunner(),
            environment)
}

fun main(vararg args: String) {
    if (args.size != 1) {
        println("usage: <app> <script file>")
    }
    else {
        val scriptFile = File(args[0])
        println("Executing script $scriptFile")

        val host = makeScriptingHost(KJVMCompilerImpl(), DummyCompiledJvmScriptCache(), ScriptEvaluationEnvironment(emptyList(), emptyMap(), emptyList(), null))

        val script = object : ScriptSource {
            override val location: URL? get() = scriptFile.toURI().toURL()
            override val text: String? get() = null
        }

        val res = host.eval(script, ProvidedDeclarations(), emptyList())

        res.reports.forEach {
            println(" : ${it.message}")
        }
    }
}