package examples.e1

import kotlin.script.ScriptDefinition
import kotlin.script.jvm.runners.BasicJvmScriptRunner

@ScriptDefinition("My script", MyScriptAnalyzer::class, BasicJvmScriptRunner::class)
abstract class MyScript {
    abstract fun body(vararg args: String): Int
}
