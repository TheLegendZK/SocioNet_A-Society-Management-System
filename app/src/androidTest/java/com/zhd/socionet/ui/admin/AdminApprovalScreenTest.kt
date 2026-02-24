package com.zhd.socionet.ui.admin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhd.socionet.fakes.FakeAdminUserRepository
import com.zhd.socionet.ui.admin.approval.AdminApprovalScreen
import com.zhd.socionet.ui.admin.approval.AdminApprovalViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminApprovalScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun adminApprovalScreen_displaysTitle() {
        val fakeRepository = FakeAdminUserRepository()
        val viewModel = AdminApprovalViewModel(fakeRepository)

        composeTestRule.setContent {
            AdminApprovalScreen(
                viewModel = viewModel,
                societyId = "SOC1",
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Approvals").assertIsDisplayed()
    }

    @Test
    fun adminApprovalScreen_displaysPendingUsers() {
        val fakeRepository = FakeAdminUserRepository()
        val viewModel = AdminApprovalViewModel(fakeRepository)

        composeTestRule.setContent {
            AdminApprovalScreen(
                viewModel = viewModel,
                societyId = "SOC1",
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("SOC1_A_101", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Approve").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reject").assertIsDisplayed()
    }
}
