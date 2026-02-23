package com.zhd.socionet.ui.security.scanner

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhd.socionet.fakes.FakeAuthRepository
import com.zhd.socionet.fakes.FakeProfileRepository
import com.zhd.socionet.fakes.FakeSecurityRepository
import com.zhd.socionet.ui.profile.ProfileViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityScannerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun securityScannerScreen_displaysTabs() {
        val fakeSecurityRepo = FakeSecurityRepository()
        val fakeAuthRepo = FakeAuthRepository()
        val fakeProfileRepo = FakeProfileRepository()
        
        val viewModel = SecurityScannerViewModel(fakeSecurityRepo, fakeAuthRepo)
        val profileViewModel = ProfileViewModel(fakeProfileRepo)

        composeTestRule.setContent {
            SecurityScannerScreen(
                viewModel = viewModel,
                profileViewModel = profileViewModel,
                onNavigateToManualEntry = {},
                onNavigateToStaffAttendance = {},
                onLogout = {}
            )
        }

        composeTestRule.onNodeWithText("Scan QR").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter OTP").assertIsDisplayed()
    }

    @Test
    fun securityScannerScreen_displaysScannerTitle() {
        val fakeSecurityRepo = FakeSecurityRepository()
        val fakeAuthRepo = FakeAuthRepository()
        val fakeProfileRepo = FakeProfileRepository()
        
        val viewModel = SecurityScannerViewModel(fakeSecurityRepo, fakeAuthRepo)
        val profileViewModel = ProfileViewModel(fakeProfileRepo)

        composeTestRule.setContent {
            SecurityScannerScreen(
                viewModel = viewModel,
                profileViewModel = profileViewModel,
                onNavigateToManualEntry = {},
                onNavigateToStaffAttendance = {},
                onLogout = {}
            )
        }

        composeTestRule.onNodeWithText("Gate Keeper").assertIsDisplayed()
    }
}
