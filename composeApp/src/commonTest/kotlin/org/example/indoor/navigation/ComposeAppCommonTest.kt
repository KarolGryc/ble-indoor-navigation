package org.example.indoor.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeAppCommonTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun anotherExample() {
        val now = nowMillis()
        assertEquals(false,         hasPassedMillis(now, 500))
    }
}