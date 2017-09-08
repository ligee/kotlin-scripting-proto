package kotlin.script.jvm

import kotlin.reflect.KClass
import kotlin.script.*

open class JvmBasicScriptingHost<ScriptBase: Any, SM: JvmScriptMetadata, E: ScriptEvaluationEnvironment, in CS: JvmCompiledScript<ScriptBase>>(
        analyser: ScriptAnalyzer<SM>,
        compiler: JvmScriptCompiler<SM, E>,
        runner: ScriptRunner<ScriptBase, E, CS>,
        override val environment: E
): ScriptingHost<ScriptBase, SM, E, CS>(analyser, compiler, runner)


class ReplEvalEnvironment(
        bindings: LinkedHashMap<String, Any?>,
        val mutableReceivers: MutableList<KClass<*>> = arrayListOf()
) : ScriptEvaluationEnvironment(
        mutableReceivers,
        bindings,
        emptyList())


open class JvmReplScriptingHost<ScriptBase: Any, SM: JvmScriptMetadata>(
        analyser: ScriptAnalyzer<SM>,
        compiler: JvmScriptCompiler<SM, ReplEvalEnvironment>,
        runner: ScriptRunner<ScriptBase, ReplEvalEnvironment, JvmCompiledScript<ScriptBase>>,
        bindings: LinkedHashMap<String, Any?>
): ScriptingHost<ScriptBase, SM, ReplEvalEnvironment, JvmCompiledScript<ScriptBase>>(analyser, compiler, runner)
{
    override val environment = ReplEvalEnvironment(bindings)

    override fun updateEnvironment(script: ScriptSource, meta: SM, compiled: JvmCompiledScript<ScriptBase>, res: Any) {
        super.updateEnvironment(script, meta, compiled, res)
        environment.mutableReceivers.add(compiled.compiledClass)
    }
}
