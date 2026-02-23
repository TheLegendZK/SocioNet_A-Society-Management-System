package com.zhd.socionet.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhd.socionet.fakes.FakeProfileRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun profileScreen_displaysUserNameAndEmail() {
        val fakeRepository = FakeProfileRepository()
        val viewModel = ProfileViewModel(fakeRepository)

        composeTestRule.setContent {
            ProfileScreen(
                viewModel = viewModel,
                userId = "admin_user_id",
                onBack = {},
                onLogout = {}
            )
        }

        // Use onAllNodesWithText and pick the first one since name appears in header and details
        composeTestRule.onAllNodesWithText("Admin User")[0].assertIsDisplayed()
        composeTestRule.onNodeWithText("admin@socionet.com").assertIsDisplayed()
    }

    @Test
    fun profileScreen_displaysActionButtons() {
        val fakeRepository = FakeProfileRepository()
        val viewModel = ProfileViewModel(fakeRepository)

        composeTestRule.setContent {
            ProfileScreen(
                viewModel = viewModel,
                userId = "admin_user_id",
                onBack = {},
                onLogout = {}
            )
        }

        composeTestRule.onNodeWithText("Edit Profile Info").assertIsDisplayed()
        composeTestRule.onNodeWithText("Change Password").assertIsDisplayed()
    }
}
