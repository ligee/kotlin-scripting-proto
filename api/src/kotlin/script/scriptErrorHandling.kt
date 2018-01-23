package kotlin.script

data class ScriptDiagnostic(
        val message: String,
        val severity: Severity = Severity.ERROR,
        val location: ScriptSource.Location? = null,
        val exception: Throwable? = null
) {
    enum class Severity { ERROR, WARNING, INFO, DEBUG }
}

sealed class ResultWithDiagnostics<out R: Any?> {
    abstract val reports: List<ScriptDiagnostic>

    data class Success<out R: Any?>(
            val value: R?,
            override val reports: List<ScriptDiagnostic> = listOf()
    ) : ResultWithDiagnostics<R>()

    data class Failure<out R: Any?>(
            override val reports: List<ScriptDiagnostic>
    ) : ResultWithDiagnostics<R>() {
        constructor(vararg reports: ScriptDiagnostic) : this(reports.asList())

        fun<T> convert(): ResultWithDiagnostics.Failure<T> = ResultWithDiagnostics.Failure(reports)
    }
}

fun<R: Any> R.asSuccess(): ResultWithDiagnostics.Success<R> = ResultWithDiagnostics.Success(this)

fun Throwable.asDiagnostics(customMessage: String? = null, location: ScriptSource.Location? = null): ScriptDiagnostic =
        ScriptDiagnostic(customMessage ?: message ?: "$this", ScriptDiagnostic.Severity.ERROR, location, this)

fun String.asErrorDiagnostics(location: ScriptSource.Location? = null): ScriptDiagnostic =
        ScriptDiagnostic(this, ScriptDiagnostic.Severity.ERROR, location)
