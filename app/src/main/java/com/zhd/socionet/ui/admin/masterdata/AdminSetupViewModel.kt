package com.zhd.socionet.ui.admin.masterdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.Flat
import com.zhd.socionet.data.model.Society
import com.zhd.socionet.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminSetupViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSetupUiState())
    val uiState: StateFlow<AdminSetupUiState> = _uiState.asStateFlow()

    fun onSocietyDetailsChanged(name: String, address: String, regNo: String) {
        _uiState.update { it.copy(societyName = name, societyAddress = address, registrationNo = regNo) }
    }

    fun addWingConfiguration(wingName: String, floors: Int, flatsPerFloor: Int) {
        _uiState.update { state ->
            val currentConfigs = state.wingConfigurations.toMutableList()
            currentConfigs.add(WingConfig(wingName, floors, flatsPerFloor))
            state.copy(wingConfigurations = currentConfigs)
        }
    }

    fun submitSocietySetup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val society = Society(
                name = _uiState.value.societyName,
                address = _uiState.value.societyAddress,
                registrationNumber = _uiState.value.registrationNo,
                wings = _uiState.value.wingConfigurations.map { it.wingName }
            )

            val societyResult = repository.createSociety(society)

            societyResult.onSuccess { societyId ->
                val allFlats = generateFlatsLogic(societyId, _uiState.value.wingConfigurations)

                val flatsResult = repository.batchCreateFlats(allFlats)

                if (flatsResult.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to create flats.") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun generateFlatsLogic(societyId: String, configs: List<WingConfig>): List<Flat> {
        val generatedFlats = mutableListOf<Flat>()

        configs.forEach { config ->
            for (floor in 1..config.floors) {
                for (unit in 1..config.flatsPerFloor) {
                    val flatNumber = "${floor}${"%02d".format(unit)}"
                    val flatId = "${societyId}_${config.wingName}_${flatNumber}"

                    val flat = Flat(
                        id = flatId,
                        societyId = societyId,
                        wing = config.wingName,
                        floorNumber = floor,
                        flatNumber = flatNumber,
                        sqFtArea = 0
                    )
                    generatedFlats.add(flat)
                }
            }
        }
        return generatedFlats
    }
}

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
    val wingName: String,
    val floors: Int,
    val flatsPerFloor: Int
)