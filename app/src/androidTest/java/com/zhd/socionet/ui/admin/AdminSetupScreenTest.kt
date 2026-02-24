package com.zhd.socionet.ui.admin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhd.socionet.fakes.FakeAdminRepository
import com.zhd.socionet.ui.admin.masterdata.AdminSetupScreen
import com.zhd.socionet.ui.admin.masterdata.AdminSetupViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminSetupScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun adminSetupScreen_displaysTitle() {
        val fakeRepository = FakeAdminRepository()
        val viewModel = AdminSetupViewModel(fakeRepository)

        composeTestRule.setContent {
            AdminSetupScreen(
                viewModel = viewModel,
                onSetupComplete = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Setup Society").assertIsDisplayed()
    }

    @Test
    fun adminSetupScreen_displaysBasicInfoSection() {
        val fakeRepository = FakeAdminRepository()
        val viewModel = AdminSetupViewModel(fakeRepository)

        composeTestRule.setContent {
            AdminSetupScreen(
                viewModel = viewModel,
                onSetupComplete = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Basic Information").assertIsDisplayed()
        composeTestRule.onNodeWithText("Society Name").assertIsDisplayed()
    }

    @Test
    fun adminSetupScreen_displaysWingsSection() {
        val fakeRepository = FakeAdminRepository()
        val viewModel = AdminSetupViewModel(fakeRepository)

        composeTestRule.setContent {
            AdminSetupScreen(
                viewModel = viewModel,
                onSetupComplete = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Wings & Blocks").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Wing").assertIsDisplayed()
    }
}
