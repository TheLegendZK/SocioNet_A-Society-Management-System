package com.zhd.socionet.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhd.socionet.fakes.FakeAuthRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun registerScreen_displaysTitle() {
        val fakeRepository = FakeAuthRepository()
        val viewModel = RegisterViewModel(fakeRepository)

        composeTestRule.setContent {
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = {}
            )
        }

        composeTestRule.onNodeWithText("Join Society").assertIsDisplayed()
    }

    @Test
    fun registerScreen_displaysPersonalDetailsSection() {
        val fakeRepository = FakeAuthRepository()
        val viewModel = RegisterViewModel(fakeRepository)

        composeTestRule.setContent {
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = {}
            )
        }

        composeTestRule.onNodeWithText("Personal Details").assertIsDisplayed()
        composeTestRule.onNodeWithText("Full Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email Address").assertIsDisplayed()
    }

    @Test
    fun registerScreen_displaysResidenceDetailsSection() {
        val fakeRepository = FakeAuthRepository()
        val viewModel = RegisterViewModel(fakeRepository)

        composeTestRule.setContent {
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = {}
            )
        }

        // Scroll to "Residence Details" section first to ensure it's in view
        composeTestRule.onNodeWithText("Residence Details").performScrollTo().assertIsDisplayed()
        
        // "Select Society" is the label of a text field within this section
        composeTestRule.onNodeWithText("Select Society").performScrollTo().assertIsDisplayed()
    }
}
