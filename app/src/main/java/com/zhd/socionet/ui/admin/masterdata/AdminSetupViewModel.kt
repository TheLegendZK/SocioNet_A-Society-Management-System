package com.zhd.socionet.ui.admin.masterdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.Flat
import com.zhd.socionet.data.model.Society
import com.zhd.socionet.data.model.WingConfigData
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

    init {
        loadSocieties()
    }

    private fun loadSocieties() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getAllSocieties().onSuccess { societies ->
                _uiState.update { it.copy(societies = societies, isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun onSocietyDetailsChanged(name: String, address: String, regNo: String) {
        _uiState.update { it.copy(societyName = name, societyAddress = address, registrationNo = regNo) }
    }

    fun addWingConfiguration(wingName: String, floors: Int, flatsPerFloor: Int) {
        _uiState.update { state ->
            val currentConfigs = state.wingConfigurations.toMutableList()
            currentConfigs.add(WingConfigData(wingName, floors, flatsPerFloor))
            state.copy(wingConfigurations = currentConfigs)
        }
    }

    fun removeWingConfiguration(config: WingConfigData) {
        _uiState.update { state ->
            val currentConfigs = state.wingConfigurations.toMutableList()
            currentConfigs.remove(config)
            state.copy(wingConfigurations = currentConfigs)
        }
    }

    fun selectSocietyForEdit(society: Society) {
        _uiState.update { 
            it.copy(
                editingSocietyId = society.id,
                societyName = society.name,
                societyAddress = society.address,
                registrationNo = society.registrationNumber,
                wingConfigurations = society.wingConfigs
            )
        }
    }

    fun resetForm() {
        _uiState.update { 
            it.copy(
                editingSocietyId = null,
                societyName = "",
                societyAddress = "",
                registrationNo = "",
                wingConfigurations = emptyList(),
                isSuccess = false
            )
        }
    }

    fun deleteSociety(societyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.deleteSociety(societyId).onSuccess {
                repository.deleteFlatsForSociety(societyId)
                loadSocieties()
                resetForm()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun submitSocietySetup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val editingId = _uiState.value.editingSocietyId
            
            val society = Society(
                id = editingId ?: "",
                name = _uiState.value.societyName,
                address = _uiState.value.societyAddress,
                registrationNumber = _uiState.value.registrationNo,
                wings = _uiState.value.wingConfigurations.map { it.wingName },
                wingConfigs = _uiState.value.wingConfigurations
            )

            val societyResult = if (editingId != null) {
                repository.updateSociety(society).map { editingId }
            } else {
                repository.createSociety(society)
            }

            societyResult.onSuccess { societyId ->
                // If editing, we might want to clear old flats and regenerate. 
                // For simplicity and to "correct" floors/flats, we'll re-generate.
                // NOTE: In a real app, you'd want to be careful about existing resident data.
                if (editingId != null) {
                    repository.deleteFlatsForSociety(societyId)
                }

                val allFlats = generateFlatsLogic(societyId, _uiState.value.wingConfigurations)
                val flatsResult = repository.batchCreateFlats(allFlats)

                if (flatsResult.isSuccess) {
                    loadSocieties()
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to handle flats.") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun generateFlatsLogic(societyId: String, configs: List<WingConfigData>): List<Flat> {
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
    val societies: List<Society> = emptyList(),
    val editingSocietyId: String? = null,
    val societyName: String = "",
    val societyAddress: String = "",
    val registrationNo: String = "",
    val wingConfigurations: List<WingConfigData> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
