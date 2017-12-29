package kotlin.script.host

import kotlin.script.*


interface ScriptRunner<in ScriptBase: Any> {

    fun run(scriptObject: ScriptBase, args: Iterable<Any?>): ResultWithDiagnostics<Any?>
}


abstract class BasicScriptingHost<ScriptBase: Any, CC: CompilerConfiguration, out E: ScriptEvaluationEnvironment, in CS: CompiledScript<ScriptBase>>(
        val configurationExtractor: ConfigurationExtractor<CC>,
        val compiler: ScriptCompiler<CC>,
        val runner: ScriptRunner<ScriptBase>
) {
    abstract val environment: E

    open fun eval(script: ScriptSource, providedDeclarations: ProvidedDeclarations, args: Iterable<Any?>): ResultWithDiagnostics<Any?> {
        val config = configurationExtractor.extractCompilerConfiguration(script, providedDeclarations)
        if (config is ResultWithDiagnostics.Failure) return config

        val compiled = compiler.compile(config.value!!)
        if (compiled is ResultWithDiagnostics.Failure) return compiled

        val obj = compiled.value!!.instantiate(environment)
        if (obj is ResultWithDiagnostics.Failure) return obj

        val res = runner.run(obj.value!! as ScriptBase, args)

        updateEnvironment(config.value!!, obj.value as ScriptBase, res)

        return res
    }

    open fun updateEnvironment(config: CC, scriptObject: ScriptBase, res: Any?) {}
}
