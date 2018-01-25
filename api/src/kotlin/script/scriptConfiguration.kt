package kotlin.script

interface ScriptConfigurator {

    fun getInitialConfiguration(script: ScriptSource): ResultWithDiagnostics<ScriptCompileConfiguration>

    fun getUpdatedConfiguration(config: ScriptCompileConfiguration, processedScriptData: ProcessedScriptData): ResultWithDiagnostics<ScriptCompileConfiguration>
}

