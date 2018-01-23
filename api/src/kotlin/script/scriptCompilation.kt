package kotlin.script

interface ScriptCompiler {

    fun compile(script: ScriptSource, configurator: ScriptConfigurator): ResultWithDiagnostics<CompiledScript<*>>
}


interface CompiledScript<out ScriptBase: Any> {

    val configuration: ScriptCompileConfiguration

    fun instantiate(scriptEvaluationEnvironment: ScriptEvaluationEnvironment): ResultWithDiagnostics<ScriptBase>
}
