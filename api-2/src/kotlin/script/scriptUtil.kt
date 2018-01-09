package kotlin.script

fun ScriptSourceFragments.isWholeFile(): Boolean = fragments?.isEmpty() ?: false

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

