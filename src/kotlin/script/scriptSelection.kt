package kotlin.script

interface ScriptSelector {

    val fileExtension: String // for preliminary selection by file type, e.g. in ide

    fun isKnownScript(script: ScriptSource): Boolean
}
