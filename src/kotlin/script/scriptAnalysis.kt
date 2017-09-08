package kotlin.script

interface ScriptAnalyzer<out SM: ScriptMetadata> {

    fun extractMetadata(script: ScriptSource): ResultWithDiagnostics<SM>
}
