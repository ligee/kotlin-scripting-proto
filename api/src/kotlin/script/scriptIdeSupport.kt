package kotlin.script

interface ScriptResolveData<ScriptBase: Any>

interface ScriptResolverForIde<in CC: CompilerConfiguration> {

    fun resolve(config: CC): ResultWithDiagnostics<ScriptResolveData<*>>
}
