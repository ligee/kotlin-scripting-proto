package kotlin.script

interface ScriptResolveData<ScriptBase: Any>

interface ScriptResolverForIde<in CC: ScriptCompilerConfiguration> {

    fun resolve(config: CC): ResultWithDiagnostics<ScriptResolveData<*>>
}
