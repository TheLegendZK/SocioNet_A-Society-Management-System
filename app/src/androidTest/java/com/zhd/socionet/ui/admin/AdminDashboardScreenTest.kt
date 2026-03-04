package com.zhd.socionet.ui.admin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhd.socionet.fakes.FakeAdminUserRepository
import com.zhd.socionet.fakes.FakeProfileRepository
import com.zhd.socionet.ui.admin.approval.AdminApprovalViewModel
import com.zhd.socionet.ui.admin.dashboard.AdminDashboardScreen
import com.zhd.socionet.ui.profile.ProfileViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminDashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun adminDashboard_displaysTitle() {
        val fakeApprovalRepo = FakeAdminUserRepository()
        val fakeProfileRepo = FakeProfileRepository()
        val approvalViewModel = AdminApprovalViewModel(fakeApprovalRepo)
        val profileViewModel = ProfileViewModel(fakeProfileRepo)

        composeTestRule.setContent {
            val navController = rememberNavController()
            
            AdminDashboardScreen(
                navController = navController,
                approvalViewModel = approvalViewModel,
                profileViewModel = profileViewModel
            )
        }

        composeTestRule.onNodeWithText("Admin Console").assertIsDisplayed()
    }

    @Test
    fun adminDashboard_displaysActionCards() {
        val fakeApprovalRepo = FakeAdminUserRepository()
        val fakeProfileRepo = FakeProfileRepository()
        val approvalViewModel = AdminApprovalViewModel(fakeApprovalRepo)
        val profileViewModel = ProfileViewModel(fakeProfileRepo)

        composeTestRule.setContent {
            val navController = rememberNavController()
            
            AdminDashboardScreen(
                navController = navController,
                approvalViewModel = approvalViewModel,
                profileViewModel = profileViewModel
            )
        }

        composeTestRule.onNodeWithText("Setup Society").assertIsDisplayed()
        composeTestRule.onNodeWithText("Accounts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Post Notice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Directory").assertIsDisplayed()
    }
}
