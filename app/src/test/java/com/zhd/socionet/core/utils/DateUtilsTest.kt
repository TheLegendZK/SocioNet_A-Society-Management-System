package com.zhd.socionet.core.utils

import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateUtilsTest {

    @Test
    fun `getCurrentMonthName returns valid month and year format`() {
        val result = DateUtils.getCurrentMonthName()

        // Regex to match "MonthName Year" (e.g., "January 2026", "May 2025")
        // \w+ matches the month, \d{4} matches the year
        val regex = "^[A-Za-z]+ \\d{4}$".toRegex()

        assertTrue("Result '$result' should match 'Month Year' format", result.matches(regex))
    }

    @Test
    fun `getCurrentMonthName matches current system time`() {
        // This test ensures the utility isn't returning hardcoded values
        val result = DateUtils.getCurrentMonthName()

        val expected = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

        assertTrue("Result should be current month", result == expected)
    }
}