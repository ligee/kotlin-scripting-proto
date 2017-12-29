package kotlin.script

data class ScriptDiagnostic(
        val message: String,
        val severity: Severity = Severity.ERROR,
        val location: ScriptSource.Location? = null
) {
    enum class Severity { ERROR, WARNING, INFO, DEBUG }
}

sealed class ResultWithDiagnostics<out R: Any?> {
    abstract val value: R?
    abstract val reports: List<ScriptDiagnostic>

    data class Success<out R: Any?>(
            override val value: R?,
            override val reports: List<ScriptDiagnostic> = listOf()
    ) : ResultWithDiagnostics<R>()

    data class Failure<out R: Any?>(
            override val reports: List<ScriptDiagnostic>
    ) : ResultWithDiagnostics<R>() {
        constructor(vararg reports: ScriptDiagnostic) : this(reports.asList())

        override val value: R? get() = null
    }
}

fun<R: Any> R.asSuccess(): ResultWithDiagnostics.Success<R> = ResultWithDiagnostics.Success(this)
