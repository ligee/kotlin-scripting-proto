package org.jetbrains.kotlin.script.examples.jvm.simple.test

import org.jetbrains.kotlin.script.examples.jvm.simple.main
import org.junit.Test

class SimpleTest {

    @Test
    fun test1() {
        main()
    }

    @Test
    fun test2() {
        main("hello.kts")
    }
}