package org.jetbrains.kotlin.script.examples.jvm1

import kotlin.script.host.ScriptDefinition
import kotlin.script.jvm.runners.BasicJvmScriptRunner

@ScriptDefinition("My script", MyConfigurationExtractor::class, MyConfigurationExtractor::class, BasicJvmScriptRunner::class)
abstract class MyScript {
//    abstract fun body(vararg args: String): Int
}
