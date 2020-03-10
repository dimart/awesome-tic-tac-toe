package sample

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Maybe it's a good idea to use Ensyme as here:
 * https://github.com/mkraynov/kfsad/blob/master/src/jsTest/kotlin/EnzymeTest.kt
 */

actual typealias JsName = kotlin.js.JsName

class SampleTestsJS {
    @Test
    fun testHello() {
        assertTrue(true)
    }
}