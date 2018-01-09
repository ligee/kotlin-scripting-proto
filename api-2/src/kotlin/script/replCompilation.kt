package kotlin.script

interface ScriptReplCompiler<CC: ScriptCompileConfiguration> {

    fun compile(mutableState: IReplStageState<*>,
                replStatement: ReplStatement,
                configurator: ScriptConfigurator<CC>): ResultWithDiagnostics<CompiledReplStatement<*>>
}

interface ScriptReplInteractiveCompiler<CC: ScriptCompileConfiguration> : ScriptReplCompiler<CC> {

    fun checkIsComplete(mutableState: IReplStageState<*>,
                        replStatement: ReplStatement,
                        configurator: ScriptConfigurator<CC>): ResultWithDiagnostics<Boolean>

    fun suggestAutocompletion(mutableState: IReplStageState<*>,
                              replStatement: ReplStatement,
                              atLocation: ScriptSource.Location,
                              configurator: ScriptConfigurator<CC>): ResultWithDiagnostics<List<String>>
}

interface CompiledReplStatement<ScriptBase: Any> {

    fun instantiate(scriptEvaluationEnvironment: ScriptEvaluationEnvironment): ResultWithDiagnostics<ScriptBase>
}
