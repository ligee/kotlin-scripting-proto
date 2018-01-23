package kotlin.script.host

import kotlin.reflect.KClass
import kotlin.script.ScriptConfigurator

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScriptDefinition(
        val name: String,
        val selector: KClass<out ScriptSelector>,
        val configurator: KClass<out ScriptConfigurator<*>>,
        val runner: KClass<out ScriptRunner<*>>)
