package com.zhd.socionet.ui.directory

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhd.socionet.fakes.FakeSocietyRepository
import com.zhd.socionet.fakes.FakeStaffRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SocietyDirectoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun societyDirectoryScreen_displaysResidentsTabByDefault() {
        val fakeSocietyRepo = FakeSocietyRepository()
        val fakeStaffRepo = FakeStaffRepository()
        val viewModel = SocietyDirectoryViewModel(fakeSocietyRepo, fakeStaffRepo)

        composeTestRule.setContent {
            SocietyDirectoryScreen(
                viewModel = viewModel,
                isAdmin = false,
                societyId = "SOC1",
                flatId = "SOC1_A_101",
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Residents").assertIsDisplayed()
        composeTestRule.onNodeWithText("Resident One").assertIsDisplayed()
    }

    @Test
    fun societyDirectoryScreen_switchesToStaffTab() {
        val fakeSocietyRepo = FakeSocietyRepository()
        val fakeStaffRepo = FakeStaffRepository()
        val viewModel = SocietyDirectoryViewModel(fakeSocietyRepo, fakeStaffRepo)

        composeTestRule.setContent {
            SocietyDirectoryScreen(
                viewModel = viewModel,
                isAdmin = false,
                societyId = "SOC1",
                flatId = "SOC1_A_101",
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Staff").performClick()
        composeTestRule.onNodeWithText("Staff One").assertIsDisplayed()
    }

    @Test
    fun societyDirectoryScreen_displaysSearchField() {
        val fakeSocietyRepo = FakeSocietyRepository()
        val fakeStaffRepo = FakeStaffRepository()
        val viewModel = SocietyDirectoryViewModel(fakeSocietyRepo, fakeStaffRepo)

        composeTestRule.setContent {
            SocietyDirectoryScreen(
                viewModel = viewModel,
                isAdmin = false,
                societyId = "SOC1",
                flatId = "SOC1_A_101",
                onBack = {}
            )
        }

        // Matching exact case as defined in strings.xml: "Search by Name or Role"
        composeTestRule.onNodeWithText("Search by Name or Role").assertIsDisplayed()
    }
}
