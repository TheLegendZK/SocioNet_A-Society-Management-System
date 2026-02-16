package com.zhd.socionet.ui.security.scanner

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.zhd.socionet.data.model.EntryStatus
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.repository.SecurityRepository
import com.zhd.socionet.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SecurityScannerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: SecurityRepository = mockk()
    private val auth: FirebaseAuth = mockk()
    private val mockUser: FirebaseUser = mockk()
    private lateinit var viewModel: SecurityScannerViewModel

    @Before
    fun setup() {
        // Mock Auth to return a specific guard ID
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns "guard_007"

        viewModel = SecurityScannerViewModel(repository, auth)
    }

    @Test
    fun `onOtpEntered success verifies and checks in`() = runTest {
        // Given
        val otp = "123456"
        val entry = GateEntry(id = "entry_1", status = EntryStatus.APPROVED)

        // 1. Verify OTP returns the entry
        coEvery { repository.verifyEntryByOtp(otp) } returns Result.success(entry)
        // 2. Mark Check-In returns success
        coEvery { repository.markEntryAsCheckedIn("entry_1", "guard_007") } returns Result.success(Unit)

        // When
        viewModel.onOtpEntered(otp)

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.scanResult is ScanResult.Success)
        assertEquals(entry, (state.scanResult as ScanResult.Success).entry)

        // Verify Guard ID was passed to repository
        coVerify { repository.markEntryAsCheckedIn("entry_1", "guard_007") }
    }

    @Test
    fun `onOtpEntered invalid OTP sets error`() = runTest {
        // Given
        val otp = "000000"
        val errorMsg = "Invalid OTP"
        coEvery { repository.verifyEntryByOtp(otp) } returns Result.failure(Exception(errorMsg))

        // When
        viewModel.onOtpEntered(otp)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.scanResult is ScanResult.Error)
        assertEquals(errorMsg, (state.scanResult as ScanResult.Error).message)

        // Should NOT try to check in if verification failed
        coVerify(exactly = 0) { repository.markEntryAsCheckedIn(any(), any()) }
    }

    @Test
    fun `onQrScanned success verifies and checks in`() = runTest {
        // Given
        val passId = "pass_123"
        val entry = GateEntry(id = passId, status = EntryStatus.APPROVED)

        coEvery { repository.verifyEntryByQr(passId) } returns Result.success(entry)
        coEvery { repository.markEntryAsCheckedIn(passId, "guard_007") } returns Result.success(Unit)

        // When
        viewModel.onQrScanned(passId)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.scanResult is ScanResult.Success)
    }
}