package kotlin.script

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

const val REPL_CODE_LINE_FIRST_NO = 1
const val REPL_CODE_LINE_FIRST_GEN = 1

data class ReplStatement(val no: Int, val generation: Int, val code: ScriptSource)

interface ReplStatementId : Comparable<ReplStatementId> {
    val no: Int
    val generation: Int
}

data class ReplHistoryRecord<out T> (val id: ReplStatementId, val item: T)

interface ReplStageHistory<T> : List<ReplHistoryRecord<T>> {

    fun peek(): ReplHistoryRecord<T>? = lock.read { lastOrNull() }

    fun push(id: ReplStatementId, item: T)

    fun pop(): ReplHistoryRecord<T>?

    fun verifiedPop(id: ReplStatementId): ReplHistoryRecord<T>? = lock.write {
        if (lastOrNull()?.id == id) pop()
        else null
    }

    fun reset(): Iterable<ReplStatementId>

    fun resetTo(id: ReplStatementId): Iterable<ReplStatementId>

    val lock: ReentrantReadWriteLock
}

interface ReplStageState<T> {
    val history: ReplStageHistory<T>

    val lock: ReentrantReadWriteLock

    val currentGeneration: Int

    fun getNextLineNo(): Int = history.peek()?.id?.no?.let { it + 1 } ?: REPL_CODE_LINE_FIRST_NO // TODO: it should be more robust downstream (e.g. use atomic)

    fun <StateT : ReplStageState<*>> asState(target: Class<out StateT>): StateT =
            if (target.isAssignableFrom(this::class.java)) this as StateT
            else throw IllegalArgumentException("$this is not an expected instance of IReplStageState")
}

