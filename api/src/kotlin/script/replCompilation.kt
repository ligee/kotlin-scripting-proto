package kotlin.script

interface ScriptReplCompiler<CC: ScriptCompileConfiguration> {

    fun compile(mutableState: ReplStageState<*>,
                replStatement: ReplStatement,
                configurator: ScriptConfigurator<CC>): ResultWithDiagnostics<CompiledReplStatement<*>>
}

interface ScriptReplInteractiveCompiler<CC: ScriptCompileConfiguration> : ScriptReplCompiler<CC> {

    fun checkIsComplete(mutableState: ReplStageState<*>,
                        replStatement: ReplStatement,
                        configurator: ScriptConfigurator<CC>): ResultWithDiagnostics<Boolean>

    fun suggestAutocompletion(mutableState: ReplStageState<*>,
                              replStatement: ReplStatement,
                              atLocation: ScriptSource.Location,
                              configurator: ScriptConfigurator<CC>): ResultWithDiagnostics<List<String>>
}

interface CompiledReplStatement<out ScriptBase: Any> {

    fun instantiate(scriptEvaluationEnvironment: ScriptEvaluationEnvironment): ResultWithDiagnostics<ScriptBase>
}
