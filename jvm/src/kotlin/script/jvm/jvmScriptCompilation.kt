package kotlin.script.jvm

import kotlin.script.*

open class JvmScriptCompiler(
        val compilerProxy: KJVMCompilerProxy,
        val cache: CompiledJvmScriptsCache
) : ScriptCompiler {

    override fun compile(script: ScriptSource, configurator: ScriptConfigurator): ResultWithDiagnostics<CompiledScript<*>> {
        val configuration = configurator.getInitialConfiguration(script).let {
            when (it) {
                is ResultWithDiagnostics.Failure -> return it.convert()
                is ResultWithDiagnostics.Success -> it.value ?: return ResultWithDiagnostics.Failure("Null script compile configuration received".asErrorDiagnostics())
            }
        }
        val cached = cache[configuration[ScriptCompileConfigurationParams.scriptSourceFragments]]

        if (cached != null) return cached.asSuccess()

        return compilerProxy.compile(configuration, configurator).also {
            if (it is ResultWithDiagnostics.Success) {
                cache.store(it.value as CompiledScript<*>)
            }
        }
    }
}

interface CompiledJvmScriptsCache {
    operator fun get(script: ScriptSourceFragments): CompiledScript<*>?
    fun store(compiledScript: CompiledScript<*>): Unit
}

interface KJVMCompilerProxy {
    fun compile(scriptCompilerConfiguration: ScriptCompileConfiguration, configurator: ScriptConfigurator): ResultWithDiagnostics<CompiledScript<*>>
}

class DummyCompiledJvmScriptCache: CompiledJvmScriptsCache {
    override operator fun get(script: ScriptSourceFragments): CompiledScript<*>? = null
    override fun store(compiledScript: CompiledScript<*>): Unit {}
}

