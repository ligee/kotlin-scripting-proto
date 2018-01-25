package org.jetbrains.kotlin.script.examples.jvm.resolve.maven

import kotlin.script.host.ScriptDefinition
import kotlin.script.jvm.runners.BasicJvmScriptRunner

@ScriptDefinition("My script with maven dependencies resolving", MyConfigurationExtractor::class, MyConfigurationExtractor::class, BasicJvmScriptRunner::class)
abstract class MyScriptWithMavenDeps() {
//    abstract fun body(vararg args: String): Int
}
