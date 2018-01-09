package kotlin.script.jvm

import kotlin.reflect.KClass
import kotlin.script.*

class JvmCompiledScript<ScriptBase: Any, out CC: JvmScriptCompileConfiguration>(
        val compiledClass: KClass<ScriptBase>,
        override val configuration: CC
) : CompiledScript<ScriptBase, CC> {
    override fun instantiate(scriptEvaluationEnvironment: ScriptEvaluationEnvironment): ResultWithDiagnostics<ScriptBase> {
        // construct class
        // return res
        return ResultWithDiagnostics.Failure(ScriptDiagnostic("not implemented yet"))
    }
}


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
                cache.store(it.value as JvmCompiledScript<*, CC>)
            }
        }
    }
}

interface CompiledJvmScriptsCache<CC: JvmScriptCompileConfiguration> {
    operator fun get(script: ScriptSourceFragments): JvmCompiledScript<*, CC>?
    fun store(compiledScript: JvmCompiledScript<*, CC>): Unit
}

interface KJVMCompilerProxy<CC: JvmScriptCompileConfiguration> {
    fun compile(scriptCompilerConfiguration: JvmScriptCompileConfiguration, configurator: ScriptConfigurator<CC>): ResultWithDiagnostics<CompiledScript<*, CC>>
}

class DummyCompiledJvmScriptCache: CompiledJvmScriptsCache<JvmScriptCompileConfiguration> {
    override operator fun get(script: ScriptSourceFragments): JvmCompiledScript<*, JvmScriptCompileConfiguration>? = null
    override fun store(compiledScript: JvmCompiledScript<*, JvmScriptCompileConfiguration>): Unit {}
}

