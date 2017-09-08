package kotlin.script

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScriptDefinition(
        val name: String,
        val selector: KClass<out ScriptSelector>,
        val analyzer: KClass<out ScriptAnalyzer<*>>,
        val runner: KClass<out ScriptRunner<*,*,*,*>>)
