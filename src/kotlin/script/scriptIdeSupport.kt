package kotlin.script

interface ScriptResolveData<ScriptBase: Any>

interface ScriptResolverForIde<in SM: ScriptMetadata, in E: ScriptEvaluationEnvironment> {

    fun resolve(script: ScriptSource, metadata: SM, environment: E): ScriptResolveData<*>
}
