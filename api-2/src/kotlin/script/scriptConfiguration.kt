package kotlin.script

interface ScriptConfigurator<CC: ScriptCompileConfiguration> {

    fun getCompilerConfiguration(script: ScriptSource): ResultWithDiagnostics<CC>
}


interface ScriptConfiguratorExt<CC: ScriptCompileConfiguration>: ScriptConfigurator<CC> {

    fun updateCompilerConfigurationFromParsed(config: CC, parsedScriptData: ParsedScriptData): ResultWithDiagnostics<CC>
}
