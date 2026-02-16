package com.zhd.socionet.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SocioNetTypeConvertersTest {

    private val converters = SocioNetTypeConverters()

    @Test
    fun `fromStringList converts list to json string correctly`() {
        val inputList = listOf("Wing A", "Wing B", "Wing C")
        val result = converters.fromStringList(inputList)

        // Simple JSON check
        assertTrue(result.contains("Wing A"))
        assertTrue(result.contains("Wing B"))
        assertTrue(result.startsWith("[") && result.endsWith("]"))
    }

    @Test
    fun `toStringList converts json string back to list correctly`() {
        val json = "[\"Admin\",\"Resident\"]"
        val result = converters.toStringList(json)

        assertEquals(2, result.size)
        assertEquals("Admin", result[0])
        assertEquals("Resident", result[1])
    }

    @Test
    fun `toStringList returns empty list for malformed json`() {
        val invalidJson = "{ invalid }"
        val result = converters.toStringList(invalidJson)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `fromStringList returns null string representation for null input`() {
        val result = converters.fromStringList(null)
        assertEquals("null", result)
    }
}