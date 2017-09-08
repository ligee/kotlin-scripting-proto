package kotlin.script.jvm

import kotlin.reflect.KClass
import kotlin.script.*

class JvmCompiledScript<ScriptBase: Any>(
        val compiledClass: KClass<ScriptBase>
) : CompiledScript<ScriptBase>


class JvmScriptCompiler<in SM: JvmScriptMetadata, in E: ScriptEvaluationEnvironment>(
        val compilerProxy: KJVMCompilerProxy,
        val cache: CompiledJvmScriptsCache
) : ScriptCompiler<SM, E> {

    override fun compile(script: ScriptSource, metadata: SM, environment: E): ResultWithDiagnostics<CompiledScript<*>> {
        val cached = cache[script]

        if (cached != null) return cached.asSuccess()

        return compilerProxy.compile(script, metadata, environment)
    }
}

interface CompiledJvmScriptsCache {
    operator fun get(script: ScriptSource): JvmCompiledScript<*>
}

interface KJVMCompilerProxy {
    fun compile(script: ScriptSource, metadata: JvmScriptMetadata, environment: ScriptEvaluationEnvironment): ResultWithDiagnostics<CompiledScript<*>>
}