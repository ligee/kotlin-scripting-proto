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
        val contextVariables: Map<String, KType> // external variables
        // Q: do we need context constants and/or types here, e.g.
        // val contextConstants: Map<String, Any?> // or with KType as well
        // val contextTypes: List<KType> // additional (to the classpath) types provided by the environment
        // alternatively:
        // val contextDeclarations: List<Tuple<DeclarationKind, String?, KType, Any?> // kind, name, type, value
)

open class ScriptSignature(
        val scriptBase: KType,
        val providedDeclarations: ProvidedDeclarations
)

open class ResolvingRestrictions {
    data class Rule(
            val allow: Boolean,
            val pattern: String // FQN wildcard
    )

    val rules: Iterable<Rule> = arrayListOf()
}

interface ScriptDependency {
    // Q: anything generic here?
}

interface CompilerConfiguration {

    val scriptSourceFragments: ScriptSourceFragments

    val scriptSignature: ScriptSignature

    val importedPackages: Iterable<String>

    val restrictions: ResolvingRestrictions

    val importedScripts: Iterable<ScriptSource>

    val dependencies: Iterable<ScriptDependency>

    val compilerOptions: Iterable<String> // Q: CommonCompilerOptions instead?
}

open class ScriptEvaluationEnvironment(
    val implicitReceivers: List<Any>, // previous scripts, etc.
    val contextVariables: Map<String, Any?>, // external variables
    val constructorArgs: List<Any?>
)
