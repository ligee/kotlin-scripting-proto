package kotlin.script


interface CompiledScript<ScriptBase: Any>


interface ScriptCompiler<in SM: ScriptMetadata, in E: ScriptEvaluationEnvironment> {

    fun compile(script: ScriptSource, metadata: SM, environment: E): ResultWithDiagnostics<CompiledScript<*>>

    // TODO: interfaces for generic metadata extraction, e.g annotations, lexing
}