package kotlin.script

interface ScriptCompiler<CC: ScriptCompileConfiguration> {

    fun compile(script: ScriptSource, configurator: ScriptConfigurator<CC>): ResultWithDiagnostics<CompiledScript<*, CC>>
}


interface CompiledScript<out ScriptBase: Any, out CC: ScriptCompileConfiguration> {

    val configuration: CC

    fun instantiate(scriptEvaluationEnvironment: ScriptEvaluationEnvironment): ResultWithDiagnostics<ScriptBase>
}
