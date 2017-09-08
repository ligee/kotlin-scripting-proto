package kotlin.script.jvm

import kotlin.reflect.KClass
import kotlin.script.ScriptMetadata
import kotlin.script.ScriptSource
import java.io.File

open class JvmScriptMetadata(
        override val sourceRanges: Iterable<ScriptSource.Range>?,
        override val scriptBase: KClass<*>,
        override val annotations: Iterable<Annotation>,
        override val importedPackages: Iterable<String>,
        override val restrictions: ScriptMetadata.Restrictions,
        override val importedScripts: Iterable<ScriptSource>,
        override val dependencies: Iterable<JvmDependency>,
        override val compilerOptions: Iterable<String>
): ScriptMetadata
{
    class JvmDependency(val classpath: Iterable<File>): ScriptMetadata.Dependency
}

