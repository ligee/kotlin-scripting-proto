package kotlin.script

interface ScriptReplCompiler<CC: ScriptCompileConfiguration> {

    fun compile(mutableState: ReplStageState<*>,
                replStatement: ReplStatement,
                configurator: ScriptConfigurator): ResultWithDiagnostics<CompiledReplStatement<*>>
}

interface ScriptReplInteractiveCompiler<CC: ScriptCompileConfiguration> : ScriptReplCompiler<CC> {

    fun checkIsComplete(mutableState: ReplStageState<*>,
                        replStatement: ReplStatement,
                        configurator: ScriptConfigurator): ResultWithDiagnostics<Boolean>

    fun suggestAutocompletion(mutableState: ReplStageState<*>,
                              replStatement: ReplStatement,
                              atLocation: ScriptSource.Location,
                              configurator: ScriptConfigurator): ResultWithDiagnostics<List<String>>
}

interface CompiledReplStatement<out ScriptBase: Any> {

    fun instantiate(scriptEvaluationEnvironment: ScriptEvaluationEnvironment): ResultWithDiagnostics<ScriptBase>
}
