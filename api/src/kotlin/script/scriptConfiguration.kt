package kotlin.script

interface ScriptConfigurator {

    fun getCompilerConfiguration(script: ScriptSource): ResultWithDiagnostics<ScriptCompileConfiguration>

    fun updateCompilerConfigurationFromParsed(config: ScriptCompileConfiguration, parsedScriptData: ParsedScriptData): ResultWithDiagnostics<ScriptCompileConfiguration>
}

