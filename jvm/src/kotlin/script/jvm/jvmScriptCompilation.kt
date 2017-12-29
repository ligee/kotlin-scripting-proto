package kotlin.script.jvm

import kotlin.reflect.KClass
import kotlin.script.*

class JvmCompiledScript<ScriptBase: Any>(
        val compiledClass: KClass<ScriptBase>
) : CompiledScript<ScriptBase> {
    override fun instantiate(scriptEvaluationEnvironment: ScriptEvaluationEnvironment): ResultWithDiagnostics<ScriptBase> {
        // construct class
        // return res
        return ResultWithDiagnostics.Failure(ScriptDiagnostic("not implemented yet"))
    }
}


class JvmScriptCompiler<in CC: JvmCompilerConfiguration>(
        val compilerProxy: KJVMCompilerProxy,
        val cache: CompiledJvmScriptsCache
) : ScriptCompiler<CC> {

    override fun compile(configuration: CC): ResultWithDiagnostics<CompiledScript<*>> {
        val cached = cache[configuration.scriptSourceFragments]

        if (cached != null) return cached.asSuccess()

        return compilerProxy.compile(configuration)
    }
}

interface CompiledJvmScriptsCache {
    operator fun get(script: ScriptSourceFragments): JvmCompiledScript<*>?
}

interface KJVMCompilerProxy {
    fun compile(scriptCompilerConfiguration: JvmCompilerConfiguration): ResultWithDiagnostics<CompiledScript<*>>
}