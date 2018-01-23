package kotlin.script.jvm

import kotlin.reflect.KClass
import kotlin.script.*
import kotlin.script.host.*

open class JvmBasicScriptingHost<ScriptBase: Any, out E: ScriptEvaluationEnvironment, in CS: CompiledScript<ScriptBase>>(
        configurationExtractor: ScriptConfigurator,
        compiler: JvmScriptCompiler,
        runner: ScriptRunner<ScriptBase>,
        override val environment: E
): BasicScriptingHost<ScriptBase, E, CS>(configurationExtractor, compiler, runner)

open class JvmScriptEvaluationEnvironment(
        val baseClassLoader: ClassLoader? = null,
        bindings: LinkedHashMap<String, Any?> = LinkedHashMap(),
        val mutableReceivers: MutableList<KClass<*>> = arrayListOf(),
        previousEvaluationEnvironment: JvmScriptEvaluationEnvironment? = null
) : ScriptEvaluationEnvironment(
        mutableReceivers,
        bindings,
        emptyList(),
        previousEvaluationEnvironment)

