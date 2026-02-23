package com.zhd.socionet.ui.admin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhd.socionet.fakes.FakeComplaintRepository
import com.zhd.socionet.ui.admin.complaints.AdminComplaintScreen
import com.zhd.socionet.ui.admin.complaints.AdminComplaintViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminComplaintScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun adminComplaintScreen_displaysTitle() {
        val fakeRepository = FakeComplaintRepository()
        val viewModel = AdminComplaintViewModel(fakeRepository)

        composeTestRule.setContent {
            AdminComplaintScreen(
                viewModel = viewModel,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Resident Help Desk").assertIsDisplayed()
    }

    @Test
    fun adminComplaintScreen_displaysComplaintsList() {
        val fakeRepository = FakeComplaintRepository()
        val viewModel = AdminComplaintViewModel(fakeRepository)

        composeTestRule.setContent {
            AdminComplaintScreen(
                viewModel = viewModel,
                onBack = {}
            )
        }

        // Title of the complaint from FakeComplaintRepository
        composeTestRule.onNodeWithText("Leaking Pipe").assertIsDisplayed()
        // Description
        composeTestRule.onNodeWithText("Water is leaking in the kitchen.").assertIsDisplayed()
    }
}
