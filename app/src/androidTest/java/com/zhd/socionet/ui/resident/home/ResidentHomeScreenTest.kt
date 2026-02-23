package com.zhd.socionet.ui.resident.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhd.socionet.fakes.FakeBillingRepository
import com.zhd.socionet.fakes.FakeNoticeRepository
import com.zhd.socionet.fakes.FakeProfileRepository
import com.zhd.socionet.fakes.FakeSosRepository
import com.zhd.socionet.ui.billing.BillingViewModel
import com.zhd.socionet.ui.profile.ProfileViewModel
import com.zhd.socionet.ui.resident.notices.NoticeBoardViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResidentHomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun residentHomeScreen_displaysWelcomeMessage() {
        val fakeProfileRepo = FakeProfileRepository()
        val fakeBillingRepo = FakeBillingRepository()
        val fakeNoticeRepo = FakeNoticeRepository()
        val fakeSosRepo = FakeSosRepository()

        val profileViewModel = ProfileViewModel(fakeProfileRepo)
        val billingViewModel = BillingViewModel(fakeBillingRepo)
        val noticeViewModel = NoticeBoardViewModel(fakeNoticeRepo)
        val sosViewModel = SosViewModel(fakeSosRepo)

        composeTestRule.setContent {
            val navController = rememberNavController()
            ResidentHomeScreen(
                navController = navController,
                flatId = "SOC1_A_101",
                userId = "user123",
                profileViewModel = profileViewModel,
                billingViewModel = billingViewModel,
                noticeViewModel = noticeViewModel,
                sosViewModel = sosViewModel
            )
        }

        // Expected format from strings.xml and code: "Hello, [Name]!"
        // string name="hello" is "Hello,"
        composeTestRule.onNodeWithText("Hello, Admin User!").assertIsDisplayed()
    }

    @Test
    fun residentHomeScreen_displaysQuickActions() {
        val fakeProfileRepo = FakeProfileRepository()
        val fakeBillingRepo = FakeBillingRepository()
        val fakeNoticeRepo = FakeNoticeRepository()
        val fakeSosRepo = FakeSosRepository()

        val profileViewModel = ProfileViewModel(fakeProfileRepo)
        val billingViewModel = BillingViewModel(fakeBillingRepo)
        val noticeViewModel = NoticeBoardViewModel(fakeNoticeRepo)
        val sosViewModel = SosViewModel(fakeSosRepo)

        composeTestRule.setContent {
            val navController = rememberNavController()
            ResidentHomeScreen(
                navController = navController,
                flatId = "SOC1_A_101",
                userId = "user123",
                profileViewModel = profileViewModel,
                billingViewModel = billingViewModel,
                noticeViewModel = noticeViewModel,
                sosViewModel = sosViewModel
            )
        }

        // Check for "Complaint" (singular) as defined in strings.xml for R.string.complaints
        composeTestRule.onNodeWithText("Complaint").assertIsDisplayed()
        composeTestRule.onNodeWithText("SOS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gate Pass").assertIsDisplayed()
        composeTestRule.onNodeWithText("Directory").assertIsDisplayed()
    }
}
