package com.zhd.socionet.ui.admin.masterdata

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.Flat
import com.zhd.socionet.data.model.Society
import com.zhd.socionet.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminSetupViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    // --- State ---
    var uiState by mutableStateOf(AdminSetupUiState())
        private set

    // --- Actions ---

    fun onSocietyDetailsChanged(name: String, address: String, regNo: String) {
        uiState = uiState.copy(societyName = name, societyAddress = address, registrationNo = regNo)
    }

    fun addWingConfiguration(wingName: String, floors: Int, flatsPerFloor: Int) {
        val currentConfigs = uiState.wingConfigurations.toMutableList()
        currentConfigs.add(WingConfig(wingName, floors, flatsPerFloor))
        uiState = uiState.copy(wingConfigurations = currentConfigs)
    }

    fun submitSocietySetup() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            // 1. Create Society Object
            val society = Society(
                name = uiState.societyName,
                address = uiState.societyAddress,
                registrationNumber = uiState.registrationNo,
                wings = uiState.wingConfigurations.map { it.wingName }
            )

            // 2. Save Society to Firestore
            val societyResult = repository.createSociety(society)

            societyResult.onSuccess { societyId ->
                // 3. Auto-Generate Flats based on configs
                val allFlats = generateFlatsLogic(societyId, uiState.wingConfigurations)

                // 4. Batch Save Flats
                val flatsResult = repository.batchCreateFlats(allFlats)

                if (flatsResult.isSuccess) {
                    uiState = uiState.copy(isLoading = false, isSuccess = true)
                } else {
                    uiState = uiState.copy(isLoading = false, errorMessage = "Failed to create flats.")
                }
            }.onFailure {
                uiState = uiState.copy(isLoading = false, errorMessage = it.message)
            }
        }
    }

    // --- The "Real World" Generator Logic ---
    private fun generateFlatsLogic(societyId: String, configs: List<WingConfig>): List<Flat> {
        val generatedFlats = mutableListOf<Flat>()

        configs.forEach { config ->
            for (floor in 1..config.floors) {
                for (unit in 1..config.flatsPerFloor) {
                    // Logic: Floor 1, Unit 1 -> "101". Floor 10, Unit 2 -> "1002"
                    val flatNumber = "${floor}${"%02d".format(unit)}"

                    // ID Format: socId_Wing_Floor_Flat (e.g., "royal_A_101")
                    val flatId = "${societyId}_${config.wingName}_${flatNumber}"

                    val flat = Flat(
                        id = flatId,
                        societyId = societyId,
                        wing = config.wingName,
                        floorNumber = floor,
                        flatNumber = flatNumber,
                        sqFtArea = 0 // Default, Admin can edit later
                    )
                    generatedFlats.add(flat)
                }
            }
        }
        return generatedFlats
    }
}

// --- Helper Data Classes for UI State ---
data class AdminSetupUiState(
    val societyName: String = "",
    val societyAddress: String = "",
    val registrationNo: String = "",
    val wingConfigurations: List<WingConfig> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class WingConfig(
    val wingName: String, // "A"
    val floors: Int,      // 10
    val flatsPerFloor: Int // 4
)