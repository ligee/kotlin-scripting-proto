package kotlin.script.jvm

import kotlin.reflect.KClass
import kotlin.script.*
import kotlin.script.host.BasicScriptingHost
import kotlin.script.host.ScriptRunner

open class JvmBasicScriptingHost<ScriptBase: Any, CC: JvmCompilerConfiguration, out E: ScriptEvaluationEnvironment, in CS: JvmCompiledScript<ScriptBase>>(
        configurationExtractor: ConfigurationExtractor<CC>,
        compiler: JvmScriptCompiler<CC>,
        runner: ScriptRunner<ScriptBase>,
        override val environment: E
): BasicScriptingHost<ScriptBase, CC, E, CS>(configurationExtractor, compiler, runner)


class ReplEvalEnvironment(
        bindings: LinkedHashMap<String, Any?>,
        val mutableReceivers: MutableList<KClass<*>> = arrayListOf()
) : ScriptEvaluationEnvironment(
        mutableReceivers,
        bindings,
        emptyList())


open class JvmReplScriptingHost<ScriptBase: Any, CC: JvmCompilerConfiguration>(
        configurationExtractor: ConfigurationExtractor<CC>,
        compiler: JvmScriptCompiler<CC>,
        runner: ScriptRunner<ScriptBase>,
        bindings: LinkedHashMap<String, Any?>
): BasicScriptingHost<ScriptBase, CC, ReplEvalEnvironment, JvmCompiledScript<ScriptBase>>(configurationExtractor, compiler, runner)
{
    override val environment = ReplEvalEnvironment(bindings)

    override fun updateEnvironment(config: CC, scriptObject: ScriptBase, res: Any?) {
        super.updateEnvironment(config, scriptObject, res)
        environment.mutableReceivers.add(scriptObject.javaClass.kotlin)
    }
}
