package kotlin.script


interface ConfigurationExtractor<out CC: CompilerConfiguration> {

    fun extractCompilerConfiguration(script: ScriptSource, providedDeclarations: ProvidedDeclarations): ResultWithDiagnostics<CC>
}

interface ScriptCompiler<in CC: CompilerConfiguration> {

    fun compile(configuration: CC): ResultWithDiagnostics<CompiledScript<*>>

    // TODO: interfaces for generic metadata extraction, e.g annotations, lexing
}

interface CompiledScript<out ScriptBase: Any> {

    fun instantiate(scriptEvaluationEnvironment: ScriptEvaluationEnvironment): ResultWithDiagnostics<ScriptBase>
}
