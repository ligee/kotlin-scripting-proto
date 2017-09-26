package kotlin.script

import java.net.URL
import kotlin.reflect.KType

interface ScriptSource {
    val location: URL?
    val text: String?

    data class Position(val line: Int, val col: Int)
    data class Range(val start: Position, val end: Position)
    data class Location(val start: Position, val end: Position? = null)
}

interface ScriptSourceFragments {
    val originalSource: ScriptSource

    val fragments: Iterable<ScriptSource.Range>?
}

open class ProvidedDeclarations(
        val implicitReceivers: List<KType>, // previous scripts, etc.
        val globals: Map<String, KType> // external variables
)

open class ScriptSignature(
        val scriptBase: KType,
        val providedDeclarations: ProvidedDeclarations
)

open class ResolvingRestrictions {
    data class Rule(
            val allow: Boolean,
            val pattern: String // TODO: elaborate
    )

    val rules: Iterable<Rule> = arrayListOf()
}

interface ScriptDependency {
    // anything generic here?
}

interface CompilerConfiguration {

    val scriptSourceFragments: ScriptSourceFragments

    val scriptSignature: ScriptSignature

    val importedPackages: Iterable<String>

    val restrictions: ResolvingRestrictions

    val importedScripts: Iterable<ScriptSource>

    val dependencies: Iterable<ScriptDependency>

    val compilerOptions: Iterable<String> // CommonCompilerOptions?
}

open class ScriptEvaluationEnvironment(
    val implicitReceivers: List<Any>, // previous scripts, etc.
    val globals: Map<String, Any?>, // external variables
    val constructorArgs: List<Any?>
)

