package kotlin.script.host

import kotlin.script.*


interface ScriptRunner<in ScriptBase: Any> {

    fun run(scriptObject: ScriptBase, args: Iterable<Any?>): ResultWithDiagnostics<Any?>
}


abstract class BasicScriptingHost<ScriptBase: Any, out E: ScriptEvaluationEnvironment, in CS: CompiledScript<ScriptBase>> (
        val configurator: ScriptConfigurator,
        val compiler: ScriptCompiler,
        val runner: ScriptRunner<ScriptBase>
) {
    abstract val environment: E

    open fun eval(script: ScriptSource, providedDeclarations: ProvidedDeclarations, args: Iterable<Any?>): ResultWithDiagnostics<Any?> {
        val compiled = compiler.compile(script, configurator)
        return when (compiled) {
            is ResultWithDiagnostics.Failure -> compiled
            is ResultWithDiagnostics.Success -> {
                val obj = compiled.value!!.instantiate(environment)
                when (obj) {
                    is ResultWithDiagnostics.Failure -> compiled.reports + obj
                    is ResultWithDiagnostics.Success -> {
                        compiled.reports +
                                runner.run(obj.value!! as ScriptBase, args).also {
                                    updateEnvironment(compiled.value.configuration, obj.value as ScriptBase, it)
                                }
                    }
                }
            }
        }
    }

    open fun updateEnvironment(config: ScriptCompileConfiguration, scriptObject: ScriptBase, res: Any?) {}
}
