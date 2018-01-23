package kotlin.script

fun ScriptSourceFragments.isWholeFile(): Boolean = fragments?.isEmpty() ?: true

fun ScriptSource.getScriptText(): String = when {
    text != null -> text!!
    location != null ->
        location!!.openStream().bufferedReader().readText()
    else -> throw RuntimeException("unable to get text from null script")
}

fun ScriptSourceFragments.getMergedScriptText(): String {
    val originalScriptText = originalSource.getScriptText()
    return if (isWholeFile()) {
        originalScriptText
    } else {
        val sb = StringBuilder(originalScriptText.length)
        var prevFragment: ScriptSource.Range? = null
        for (fragment in fragments!!) {
            val fragmentStartPos = fragment.start.absolutePos
            val fragmentEndPos = fragment.end.absolutePos
            if (fragmentStartPos == null || fragmentEndPos == null)
                throw RuntimeException("Script fragments require absolute positions (received: $fragment)")
            val curPos = if (prevFragment == null) 0 else prevFragment.end.absolutePos!!
            if (prevFragment != null && prevFragment.end.absolutePos!! > fragmentStartPos) throw RuntimeException("Unsorted or overlapping fragments: previous: $prevFragment, current: $fragment")
            if (curPos < fragmentStartPos) {
                sb.append(originalScriptText.subSequence(curPos, fragmentStartPos).map { if (it == '\r' || it == '\n') it else ' ' }) // preserving lines layout
            }
            sb.append(originalScriptText.subSequence(fragmentStartPos, fragmentEndPos))
            prevFragment = fragment
        }
        sb.toString()
    }
}

data class TypedKey<T>(val name: String)

class HeterogeneousMap(val data: MutableMap<TypedKey<*>, Any?> = hashMapOf()) {
    constructor(vararg pairs: Pair<TypedKey<*>, Any?>) : this(hashMapOf(*pairs))
}

operator fun<T> HeterogeneousMap.get(key: TypedKey<T>): T = data[key] as T

fun<T> HeterogeneousMap.getOptional(key: TypedKey<T>): T? = data[key] as T?

fun<T> HeterogeneousMap.put(key: TypedKey<T>, value: T?) = data.put(key, value)
