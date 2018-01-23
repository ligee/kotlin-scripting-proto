package kotlin.script

import java.net.URL
import kotlin.reflect.KClass
import kotlin.reflect.KType

interface ScriptSource {
    val location: URL?
    val text: String?

    data class Position(val line: Int, val col: Int, val absolutePos: Int? = null)
    data class Range(val start: Position, val end: Position)
    data class Location(val start: Position, val end: Position? = null)
}

open class ScriptSourceFragments(
    val originalSource: ScriptSource,
    val fragments: List<ScriptSource.Range>?)

open class ProvidedDeclarations(
        val implicitReceivers: List<KType> = emptyList(), // previous scripts, etc.
        val contextVariables: Map<String, KType> = emptyMap() // external variables
        // Q: do we need context constants and/or types here, e.g.
        // val contextConstants: Map<String, Any?> // or with KType as well
        // val contextTypes: List<KType> // additional (to the classpath) types provided by the environment
        // alternatively:
        // val contextDeclarations: List<Tuple<DeclarationKind, String?, KType, Any?> // kind, name, type, value
)

open class ScriptSignature(
        val scriptBase: KClass<*>,
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

object ScriptCompileConfigurationParams {

    val scriptSourceFragments = TypedKey<ScriptSourceFragments>("scriptSourceFragments")

    val scriptSignature = TypedKey<ScriptSignature>("scriptSignature")

    val importedPackages = TypedKey<Iterable<String>>("importedPackages")

    val restrictions = TypedKey<ResolvingRestrictions>("restrictions")

    val importedScripts = TypedKey<Iterable<ScriptSource>>("importedScripts")

    val dependencies = TypedKey<Iterable<ScriptDependency>>("dependencies")

    val compilerOptions = TypedKey<Iterable<String>>("compilerOptions") // Q: CommonCompilerOptions instead?
}

typealias ScriptCompileConfiguration = HeterogeneousMap

interface ParsedScriptData {
    val annotations: Iterable<Annotation>
}

open class ScriptEvaluationEnvironment(
    val implicitReceivers: List<Any>,
    val contextVariables: Map<String, Any?>, // external variables
    val constructorArgs: List<Any?>,
    val previousScriptEvaluationEnvironment: ScriptEvaluationEnvironment?
)
