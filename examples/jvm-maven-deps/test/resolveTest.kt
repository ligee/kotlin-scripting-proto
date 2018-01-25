import org.jetbrains.kotlin.script.examples.jvm.resolve.maven.main
import org.junit.Test

class ResolveTest {

    @Test
    fun test1() {
        main("hello-maven-resolve.kts")
    }
}