package org.jetbrains.kotlin.script.examples.jvm1

import kotlin.script.*
import kotlin.script.host.ScriptSelector
import kotlin.script.jvm.*
import kotlin.script.jvm.runners.BasicJvmScriptRunner

class MyConfigurationExtractor : ConfigurationExtractor<JvmScriptCompilerConfiguration>, ScriptSelector {

    override val fileExtension: String = "myscript.kts"

    override fun isKnownScript(script: ScriptSource): Boolean = true

    override fun extractCompilerConfiguration(script: ScriptSource, providedDeclarations: ProvidedDeclarations): ResultWithDiagnostics<JvmScriptCompilerConfiguration> {
//        return JvmCompilerConfiguration(
//        ).asSuccess()
        return ResultWithDiagnostics.Failure(ScriptDiagnostic("not implemented yet"))
    }
}

fun makeScriptingHost(compilerProxy: KJVMCompilerProxy, cache: CompiledJvmScriptsCache, environment: ScriptEvaluationEnvironment)
  : JvmBasicScriptingHost<MyScript, JvmScriptCompilerConfiguration, ScriptEvaluationEnvironment, JvmCompiledScript<MyScript>>
{
    val scriptCompiler = JvmScriptCompiler<JvmScriptCompilerConfiguration>(compilerProxy, cache)
    return JvmBasicScriptingHost(
            MyConfigurationExtractor(),
            scriptCompiler,
            BasicJvmScriptRunner(),
            environment)
}