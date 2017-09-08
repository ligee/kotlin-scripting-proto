package kotlin.script

import java.net.URL
import kotlin.reflect.KClass

interface ScriptSource {
    val location: URL?
    val text: String?

    data class Position(val line: Int, val col: Int)
    data class Range(val start: Position, val end: Position)
    data class Location(val start: Position, val end: Position? = null)
}


interface ScriptMetadata {

    interface Dependency

    open class Restrictions {
        data class Rule(
            val allow: Boolean,
            val pattern: String // TODO: elaborate
        )

        val rules: Iterable<Rule> = arrayListOf()
    }

    val sourceRanges: Iterable<ScriptSource.Range>?

    val scriptBase: KClass<*>

    val annotations: Iterable<Annotation>

    val importedPackages: Iterable<String>

    val restrictions: Restrictions

    val importedScripts: Iterable<ScriptSource>

    val dependencies: Iterable<Dependency>

    val compilerOptions: Iterable<String> // CommonCompilerOptions?
}


open class ScriptEvaluationEnvironment(
    val implicitReceivers: List<KClass<*>>, // previous scripts, etc.
    val bindings: LinkedHashMap<String, Any?>, // external variables
    val args: List<Any?>
)
