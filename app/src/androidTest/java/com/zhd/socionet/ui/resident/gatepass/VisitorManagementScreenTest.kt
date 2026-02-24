package com.zhd.socionet.ui.resident.gatepass

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhd.socionet.fakes.FakeGatePassRepository
import com.zhd.socionet.fakes.FakeOtpGenerator
import com.zhd.socionet.fakes.FakeStaffRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VisitorManagementScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun visitorManagementScreen_displaysTitle() {
        val fakeGatePassRepo = FakeGatePassRepository()
        val fakeStaffRepo = FakeStaffRepository()
        val fakeOtpGenerator = FakeOtpGenerator()
        val viewModel = GatePassViewModel(fakeGatePassRepo, fakeStaffRepo, fakeOtpGenerator)

        composeTestRule.setContent {
            VisitorManagementScreen(
                viewModel = viewModel,
                hostFlatId = "SOC1_A_101",
                hostUserId = "user123",
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("Visitor Hub").assertIsDisplayed()
    }

    @Test
    fun visitorManagementScreen_displaysTabs() {
        val fakeGatePassRepo = FakeGatePassRepository()
        val fakeStaffRepo = FakeStaffRepository()
        val fakeOtpGenerator = FakeOtpGenerator()
        val viewModel = GatePassViewModel(fakeGatePassRepo, fakeStaffRepo, fakeOtpGenerator)

        composeTestRule.setContent {
            VisitorManagementScreen(
                viewModel = viewModel,
                hostFlatId = "SOC1_A_101",
                hostUserId = "user123",
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("Create Pass").assertIsDisplayed()
        composeTestRule.onNodeWithText("Approvals").assertIsDisplayed()
        composeTestRule.onNodeWithText("History").assertIsDisplayed()
    }

    @Test
    fun visitorManagementScreen_displaysPendingRequestsInApprovalsTab() {
        val fakeGatePassRepo = FakeGatePassRepository()
        val fakeStaffRepo = FakeStaffRepository()
        val fakeOtpGenerator = FakeOtpGenerator()
        val viewModel = GatePassViewModel(fakeGatePassRepo, fakeStaffRepo, fakeOtpGenerator)

        composeTestRule.setContent {
            VisitorManagementScreen(
                viewModel = viewModel,
                hostFlatId = "SOC1_A_101",
                hostUserId = "user123",
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("Approvals").performClick()
        composeTestRule.onNodeWithText("Guest User").assertIsDisplayed()
    }

    @Test
    fun visitorManagementScreen_displaysHistoryInHistoryTab() {
        val fakeGatePassRepo = FakeGatePassRepository()
        val fakeStaffRepo = FakeStaffRepository()
        val fakeOtpGenerator = FakeOtpGenerator()
        val viewModel = GatePassViewModel(fakeGatePassRepo, fakeStaffRepo, fakeOtpGenerator)

        composeTestRule.setContent {
            VisitorManagementScreen(
                viewModel = viewModel,
                hostFlatId = "SOC1_A_101",
                hostUserId = "user123",
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("History").performClick()
        composeTestRule.onNodeWithText("Delivery Guy").assertIsDisplayed()
    }
}
