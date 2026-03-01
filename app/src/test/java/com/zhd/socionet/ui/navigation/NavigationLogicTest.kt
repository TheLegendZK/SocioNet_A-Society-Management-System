package com.zhd.socionet.ui.navigation

import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationLogicTest {

    @Test
    fun `getHomeDestination returns Admin Dashboard for Super Admin`() {
        val profile = UserProfile(
            id = "admin_1",
            roles = listOf(UserRole.SUPER_ADMIN),
            isVerified = true
        )

        val destination = NavigationRouter.getHomeDestination(profile)

        assertEquals(Routes.ADMIN_DASHBOARD, destination)
    }

    @Test
    fun `getHomeDestination returns Security Dashboard for Guard`() {
        val profile = UserProfile(
            id = "guard_1",
            roles = listOf(UserRole.SECURITY_GUARD),
            isVerified = true
        )

        val destination = NavigationRouter.getHomeDestination(profile)

        assertEquals(Routes.SECURITY_DASHBOARD, destination)
    }

    @Test
    fun `getHomeDestination returns Pending Approval if not verified`() {
        val profile = UserProfile(
            id = "user_1",
            roles = listOf(UserRole.OWNER),
            isVerified = false
        )

        val destination = NavigationRouter.getHomeDestination(profile)

        assertEquals(Routes.PENDING_APPROVAL, destination)
    }

    @Test
    fun `getHomeDestination returns Resident Home for Verified Owner`() {
        val profile = UserProfile(
            id = "user_1",
            roles = listOf(UserRole.OWNER),
            flatId = "Flat_101",
            isVerified = true
        )

        val destination = NavigationRouter.getHomeDestination(profile)

        // Expected: resident_home/Flat_101/user_1
        val expected = Routes.buildResidentHome("Flat_101", "user_1")
        assertEquals(expected, destination)
    }
}