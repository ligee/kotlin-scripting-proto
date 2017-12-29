package kotlin.script.host

import kotlin.reflect.KClass
import kotlin.script.ConfigurationExtractor

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScriptDefinition(
        val name: String,
        val selector: KClass<out ScriptSelector>,
        val extractor: KClass<out ConfigurationExtractor<*>>,
        val runner: KClass<out ScriptRunner<*>>)
