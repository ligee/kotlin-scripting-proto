package kotlin.script


interface ScriptRunner<ScriptBase: Any, in SM: ScriptMetadata, in E: ScriptEvaluationEnvironment, in CS: CompiledScript<ScriptBase>> {

    fun instantiate(compiledScript: CS, meta: SM, environment: E): ResultWithDiagnostics<ScriptBase>
    fun run(scriptObject: ScriptBase, meta: SM, environment: E): ResultWithDiagnostics<Any>
    fun eval(compiledScript: CS, meta: SM, environment: E): ResultWithDiagnostics<Any> {
        val obj = instantiate(compiledScript, meta, environment)
        if (obj is ResultWithDiagnostics.Failure) return obj
        return run(obj.value!!, meta, environment)
    }
}


abstract class ScriptingHost<ScriptBase: Any, SM: ScriptMetadata, E: ScriptEvaluationEnvironment, in CS: CompiledScript<ScriptBase>>(
        val analyser: ScriptAnalyzer<SM>,
        val compiler: ScriptCompiler<SM, E>,
        val runner: ScriptRunner<ScriptBase, SM, E, CS>
) {
    abstract val environment: E

    open fun eval(script: ScriptSource): ResultWithDiagnostics<Any> {
        val meta = analyser.extractMetadata(script)
        if (meta is ResultWithDiagnostics.Failure) return meta
        val compiled = compiler.compile(script, meta.value!!, environment)
        if (compiled is ResultWithDiagnostics.Failure) return compiled
        val res = runner.eval(compiled.value as CS, meta.value!!, environment)
        updateEnvironment(script, meta.value!!, compiled.value as CS, res)
        return res
    }

    open fun updateEnvironment(script: ScriptSource, meta: SM, compiled: CS, res: Any) {}
}
