package com.example.mda_mobile_app.ui.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mda_mobile_app.data.model.crud.Medication
import com.example.mda_mobile_app.data.model.crud.MedicationLog
import com.example.mda_mobile_app.data.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// One state source for all three sections of the screen.
data class MedicationUiState(
    val medications: List<Medication> = emptyList(),
    val logs: List<MedicationLog> = emptyList(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val notice: String? = null
)

class MedicationViewModel(
    private val userId: Int,
    private val repository: MedicationRepository = MedicationRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(MedicationUiState())
    val state: StateFlow<MedicationUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val medications = repository.medications(userId)
                val logs = repository.logs(userId)
                _state.update { it.copy(medications = medications, logs = logs, loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Unable to load records") }
            }
        }
    }

    private fun mutate(successMessage: String, action: suspend () -> Unit) {
        if (_state.value.saving) return
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null, notice = null) }
            try {
                action()
                // Reload from server after every write, not from invented local records.
                val medications = repository.medications(userId)
                val logs = repository.logs(userId)
                _state.update {
                    it.copy(medications = medications, logs = logs, saving = false, notice = successMessage)
                }
            } catch (e: Exception) {
                _state.update { it.copy(saving = false, error = e.message ?: "Operation failed") }
            }
        }
    }

    fun addMedication(name: String, notes: String?) = mutate("Medication added") {
        repository.addMedication(userId, name.trim(), notes?.trim())
    }

    fun editMedication(item: Medication, name: String, notes: String?, active: Boolean) =
        mutate("Medication updated") {
            repository.editMedication(item.medicationId, name.trim(), notes?.trim(), active)
        }

    fun deleteMedication(item: Medication) = mutate("Medication deleted") {
        repository.deleteMedication(item.medicationId)
    }

    fun addLog(medicationId: Int, scheduledAt: String, status: String, notes: String?) =
        mutate("Intake recorded") {
            repository.addLog(medicationId, scheduledAt, status, notes?.trim())
        }

    fun editLog(item: MedicationLog, scheduledAt: String, status: String, notes: String?) =
        mutate("Intake record updated") {
            repository.editLog(item.logId, scheduledAt, status, notes?.trim())
        }

    fun deleteLog(item: MedicationLog) = mutate("Intake record deleted") {
        repository.deleteLog(item.logId)
    }

    companion object {
        fun factory(userId: Int): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(MedicationViewModel::class.java))
                return MedicationViewModel(userId) as T
            }
        }
    }
}
