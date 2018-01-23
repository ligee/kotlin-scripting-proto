package kotlin.script.jvm.runners

import kotlin.script.ResultWithDiagnostics
import kotlin.script.ScriptDiagnostic
import kotlin.script.ScriptEvaluationEnvironment
import kotlin.script.host.ScriptRunner

open class BasicJvmScriptRunner<in ScriptBase: Any>
    : ScriptRunner<ScriptBase>
{
    override fun run(scriptObject: ScriptBase, args: Iterable<Any?>): ResultWithDiagnostics<Any?> {
        try {
            // in the future, when (if) we'll stop to compile everything into constructor
            // run as SAM
            // return res
            if (scriptObject !is Class<*>)
                return ResultWithDiagnostics.Failure(ScriptDiagnostic("expecting class in this implementation, got ${scriptObject.javaClass}"))

            scriptObject.getConstructor().newInstance()

            return ResultWithDiagnostics.Success(null)
        }
        catch (e: Exception) {
            return ResultWithDiagnostics.Failure(ScriptDiagnostic(e.message ?: "$e"))
        }
    }
}