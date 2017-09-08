package examples.e1

import kotlin.script.*
import kotlin.script.jvm.*
import kotlin.script.jvm.runners.BasicJvmScriptRunner

class MyScriptAnalyzer : ScriptAnalyzer<JvmScriptMetadata> {

    override fun extractMetadata(script: ScriptSource): ResultWithDiagnostics<JvmScriptMetadata> {
        return JvmScriptMetadata(
                null,
                MyScript::class,
                emptyList(),
                emptyList(),
                ScriptMetadata.Restrictions(),
                emptyList(),
                emptyList(),
                emptyList()
        ).asSuccess()
    }
}

fun makeScriptingHost(compilerProxy: KJVMCompilerProxy, cache: CompiledJvmScriptsCache, environment: ScriptEvaluationEnvironment)
  : JvmBasicScriptingHost<MyScript, JvmScriptMetadata, ScriptEvaluationEnvironment, JvmCompiledScript<MyScript>>
{
    val scriptCompiler = JvmScriptCompiler<JvmScriptMetadata, ScriptEvaluationEnvironment>(compilerProxy, cache)
    return JvmBasicScriptingHost(
            MyScriptAnalyzer(),
            scriptCompiler,
            BasicJvmScriptRunner(),
            environment)
}