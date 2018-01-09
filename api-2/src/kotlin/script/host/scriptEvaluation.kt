package kotlin.script.host

import kotlin.script.*


interface ScriptRunner<in ScriptBase: Any> {

    fun run(scriptObject: ScriptBase, args: Iterable<Any?>): ResultWithDiagnostics<Any?>
}


abstract class BasicScriptingHost<ScriptBase: Any, CC: ScriptCompileConfiguration, out E: ScriptEvaluationEnvironment, in CS: CompiledScript<ScriptBase, CC>>(
        val configurator: ScriptConfigurator<CC>,
        val compiler: ScriptCompiler<CC>,
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
                    is ResultWithDiagnostics.Failure -> obj
                    is ResultWithDiagnostics.Success -> {
                        runner.run(obj.value!! as ScriptBase, args).also {
                                    updateEnvironment(compiled.value.configuration, obj.value as ScriptBase, it)
                                }
                    }
                }
            }
        }
    }

    open fun updateEnvironment(config: CC, scriptObject: ScriptBase, res: Any?) {}
}
