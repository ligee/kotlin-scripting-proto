package kotlin.script.jvm.runners

import kotlin.script.ResultWithDiagnostics
import kotlin.script.ScriptDiagnostic
import kotlin.script.ScriptEvaluationEnvironment
import kotlin.script.host.ScriptRunner
import kotlin.script.jvm.JvmCompiledScript
import kotlin.script.jvm.JvmCompilerConfiguration

open class BasicJvmScriptRunner<in ScriptBase: Any>
    : ScriptRunner<ScriptBase>
{
    override fun run(scriptObject: ScriptBase, args: Iterable<Any?>): ResultWithDiagnostics<Any?> {
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