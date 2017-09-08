package kotlin.script.jvm.runners

import kotlin.script.ResultWithDiagnostics
import kotlin.script.ScriptDiagnostic
import kotlin.script.ScriptEvaluationEnvironment
import kotlin.script.ScriptRunner
import kotlin.script.jvm.JvmCompiledScript
import kotlin.script.jvm.JvmScriptMetadata

open class BasicJvmScriptRunner<ScriptBase: Any, in SM: JvmScriptMetadata, in E: ScriptEvaluationEnvironment>
    : ScriptRunner<ScriptBase, SM, E, JvmCompiledScript<ScriptBase>>
{
    override fun instantiate(compiledScript: JvmCompiledScript<ScriptBase>, meta: SM, environment: E): ResultWithDiagnostics<ScriptBase> {
        try {
            // construct class
            // return res
            return ResultWithDiagnostics.Failure(ScriptDiagnostic("not implemented yet"))
        }
        catch (e: Exception) {
            return ResultWithDiagnostics.Failure(ScriptDiagnostic(e.message ?: "$e"))
        }
    }

    override fun run(scriptObject: ScriptBase, meta: SM, environment: E): ResultWithDiagnostics<Any> {
        try {
            // run as SAM
            // return res
            return ResultWithDiagnostics.Failure(ScriptDiagnostic("not implemented yet"))
        }
        catch (e: Exception) {
            return ResultWithDiagnostics.Failure(ScriptDiagnostic(e.message ?: "$e"))
        }
    }
}