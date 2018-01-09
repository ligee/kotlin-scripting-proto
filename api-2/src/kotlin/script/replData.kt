package kotlin.script

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

const val REPL_CODE_LINE_FIRST_NO = 1
const val REPL_CODE_LINE_FIRST_GEN = 1

data class ReplStatement(val no: Int, val generation: Int, val code: ScriptSource)

interface ILineId : Comparable<ILineId> {
    val no: Int
    val generation: Int
}

data class ReplHistoryRecord<out T> (val id: ILineId, val item: T)

interface IReplStageHistory<T> : List<ReplHistoryRecord<T>> {

    fun peek(): ReplHistoryRecord<T>? = lock.read { lastOrNull() }

    fun push(id: ILineId, item: T)

    fun pop(): ReplHistoryRecord<T>?

    fun verifiedPop(id: ILineId): ReplHistoryRecord<T>? = lock.write {
        if (lastOrNull()?.id == id) pop()
        else null
    }

    fun reset(): Iterable<ILineId>

    fun resetTo(id: ILineId): Iterable<ILineId>

    val lock: ReentrantReadWriteLock
}

interface IReplStageState<T> {
    val history: IReplStageHistory<T>

    val lock: ReentrantReadWriteLock

    val currentGeneration: Int

    fun getNextLineNo(): Int = history.peek()?.id?.no?.let { it + 1 } ?: REPL_CODE_LINE_FIRST_NO // TODO: it should be more robust downstream (e.g. use atomic)

    fun <StateT : IReplStageState<*>> asState(target: Class<out StateT>): StateT =
            if (target.isAssignableFrom(this::class.java)) this as StateT
            else throw IllegalArgumentException("$this is not an expected instance of IReplStageState")
}

