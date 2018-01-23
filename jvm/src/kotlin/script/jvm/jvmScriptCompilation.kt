package kotlin.script.jvm

import kotlin.script.*

open class JvmScriptCompiler<CC: JvmScriptCompileConfiguration>(
        val compilerProxy: KJVMCompilerProxy<CC>,
        val cache: CompiledJvmScriptsCache<CC>
) : ScriptCompiler<CC> {

    override fun compile(script: ScriptSource, configurator: ScriptConfigurator<CC>): ResultWithDiagnostics<CompiledScript<*, CC>> {
        val configuration = configurator.getCompilerConfiguration(script).let {
            when (it) {
                is ResultWithDiagnostics.Failure -> return it.convert()
                is ResultWithDiagnostics.Success -> it.value ?: return ResultWithDiagnostics.Failure("Null script compile configuration received".asErrorDiagnostics())
            }
        }
        val cached = cache[configuration.scriptSourceFragments]

        if (cached != null) return cached.asSuccess()

        return compilerProxy.compile(configuration, configurator).also {
            if (it is ResultWithDiagnostics.Success) {
                cache.store(it.value as CompiledScript<*, CC>)
            }
        }
    }
}

interface CompiledJvmScriptsCache<CC: JvmScriptCompileConfiguration> {
    operator fun get(script: ScriptSourceFragments): CompiledScript<*, CC>?
    fun store(compiledScript: CompiledScript<*, CC>): Unit
}

interface KJVMCompilerProxy<CC: JvmScriptCompileConfiguration> {
    fun compile(scriptCompilerConfiguration: JvmScriptCompileConfiguration, configurator: ScriptConfigurator<CC>): ResultWithDiagnostics<CompiledScript<*, CC>>
}

class DummyCompiledJvmScriptCache: CompiledJvmScriptsCache<JvmScriptCompileConfiguration> {
    override operator fun get(script: ScriptSourceFragments): CompiledScript<*, JvmScriptCompileConfiguration>? = null
    override fun store(compiledScript: CompiledScript<*, JvmScriptCompileConfiguration>): Unit {}
}

