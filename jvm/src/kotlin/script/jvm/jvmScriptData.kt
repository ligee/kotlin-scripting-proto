package kotlin.script.jvm

import java.io.File
import kotlin.script.*

object JvmScriptCompileConfigurationParams {

    val javaHomeDir = TypedKey<File>("javaHomeDir")
}

class JvmDependency(val classpath: Iterable<File>): ScriptDependency

