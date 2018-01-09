package kotlin.script.jvm

import java.io.File
import kotlin.script.*

open class JvmScriptCompilerConfiguration(
        override val scriptSourceFragments: ScriptSourceFragments,
        override val scriptSignature: ScriptSignature,
        override val importedPackages: Iterable<String>,
        override val restrictions: ResolvingRestrictions,
        override val importedScripts: Iterable<ScriptSource>,
        override val dependencies: Iterable<JvmDependency>,
        override val compilerOptions: Iterable<String>,
        val javaHomeDir: File
): ScriptCompilerConfiguration
{
    class JvmDependency(val classpath: Iterable<File>): ScriptDependency
}

