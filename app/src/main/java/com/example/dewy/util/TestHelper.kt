package com.example.dewy.util

/* Used to help the app speed up tests and avoid doing unnecessary work when it is just running a test. */
object TestHelper {
    fun isRunningTest(): Boolean {
        return try {
            Class.forName("org.junit.Test")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
