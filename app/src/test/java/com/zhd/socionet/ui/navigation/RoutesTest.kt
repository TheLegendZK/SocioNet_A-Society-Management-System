package com.zhd.socionet.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutesTest {

    @Test
    fun `buildResidentHome creates correct route`() {
        val flatId = "Flat_101"
        val userId = "User_123"
        val expected = "resident_home/Flat_101/User_123"

        val actual = Routes.buildResidentHome(flatId, userId)

        assertEquals(expected, actual)
    }

    @Test
    fun `buildResidentCreatePass creates correct route`() {
        val flatId = "A_101"
        val userId = "U_999"
        val expected = "resident_create_pass/A_101/U_999"

        val actual = Routes.buildResidentCreatePass(flatId, userId)

        assertEquals(expected, actual)
    }

    @Test
    fun `buildAdminPostNotice creates correct route with query param`() {
        val societyId = "Soc_1"
        val expected = "admin_post_notice?societyId=Soc_1"

        val actual = Routes.buildAdminPostNotice(societyId)

        assertEquals(expected, actual)
    }

    @Test
    fun `buildDirectory creates correct route for Admin`() {
        val societyId = "Soc_1"
        val expected = "society_directory/true/Soc_1?flatId=&userId="

        val actual = Routes.buildDirectory(isAdmin = true, societyId = societyId)

        assertEquals(expected, actual)
    }

    @Test
    fun `buildProfile creates correct route`() {
        val userId = "123"
        val expected = "profile_screen/123?type=resident&flatId="

        val actual = Routes.buildProfile(userId)

        assertEquals(expected, actual)
    }
}