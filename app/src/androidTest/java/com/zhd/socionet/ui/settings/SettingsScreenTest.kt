package com.zhd.socionet.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhd.socionet.fakes.FakeAuthRepository
import com.zhd.socionet.fakes.FakeSettingsRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_displaysTitle() {
        val fakeSettingsRepo = FakeSettingsRepository()
        val fakeAuthRepo = FakeAuthRepository()
        val viewModel = SettingsViewModel(fakeAuthRepo, fakeSettingsRepo)

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("App Settings").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysAppearanceSection() {
        val fakeSettingsRepo = FakeSettingsRepository()
        val fakeAuthRepo = FakeAuthRepository()
        val viewModel = SettingsViewModel(fakeAuthRepo, fakeSettingsRepo)

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysNotificationsSection() {
        val fakeSettingsRepo = FakeSettingsRepository()
        val fakeAuthRepo = FakeAuthRepository()
        val viewModel = SettingsViewModel(fakeAuthRepo, fakeSettingsRepo)

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Notifications").assertIsDisplayed()
        composeTestRule.onNodeWithText("Master Toggle").assertIsDisplayed()
    }
}
