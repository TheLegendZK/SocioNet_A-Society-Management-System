package com.zhd.socionet.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhd.socionet.fakes.FakeAuthRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysWelcomeText() {
        val fakeRepository = FakeAuthRepository()
        val viewModel = LoginViewModel(fakeRepository)

        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {},
                onLoginSuccess = {}
            )
        }

        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
    }

    @Test
    fun loginScreen_displaysEmailAndPasswordFields() {
        val fakeRepository = FakeAuthRepository()
        val viewModel = LoginViewModel(fakeRepository)

        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {},
                onLoginSuccess = {}
            )
        }

        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
    }
}
