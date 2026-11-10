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
import com.example.mda_mobile_app.data.model.crud.UserRewards
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.mda_mobile_app.data.model.crud.Reminder
// One state source for all three sections of the screen.
data class MedicationUiState(
    val medications: List<Medication> = emptyList(),
    val logs: List<MedicationLog> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val notice: String? = null,
    val rewards: UserRewards? = null,
    val rewardsError: String? = null

)

class MedicationViewModel(
    private val userId: Int,
    private val repository: MedicationRepository = MedicationRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(MedicationUiState())
    val state: StateFlow<MedicationUiState> = _state.asStateFlow()

    init { refresh() }
    private fun calculateStreaks(logs: List<MedicationLog>): Pair<Int, Int> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Only successful intake logs count.
        val successfulDays = logs
            .filter { it.status.equals("taken", ignoreCase = true) }
            .mapNotNull { log ->
                try {
                    dateFormat.parse(log.scheduledAt)?.let { dayFormat.format(it) }
                } catch (e: Exception) {
                    null
                }
            }
            .toSet()

        if (successfulDays.isEmpty()) return Pair(0, 0)

        // Calculate the longest historical streak.
        val sortedDays = successfulDays.sorted()
        var longestStreak = 0
        var runningStreak = 0
        var previousDate: Date? = null

        for (day in sortedDays) {
            val currentDate = dayFormat.parse(day) ?: continue

            val consecutive = previousDate?.let {
                val calendar = Calendar.getInstance()
                calendar.time = it
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                dayFormat.format(calendar.time) == day
            } ?: false

            runningStreak = if (consecutive) runningStreak + 1 else 1
            longestStreak = maxOf(longestStreak, runningStreak)
            previousDate = currentDate
        }

        // Current streak must include today.
        var currentStreak = 0
        val calendar = Calendar.getInstance()

        while (true) {
            val day = dayFormat.format(calendar.time)

            if (day !in successfulDays) break

            currentStreak++
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }

        return Pair(currentStreak, longestStreak)
    }
    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val medications = repository.medications(userId)
                val logs = repository.logs(userId)
                val reminders = repository.reminders(userId)
                _state.update { it.copy(medications = medications, logs = logs,  reminders = reminders, loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Unable to load records") }
            }
        }
    }
    private suspend fun refreshRewards() {
        try {
            val rewards = repository.userRewards(userId)
                ?: repository.createUserRewards(userId)

            val logs = _state.value.logs

            // Calculate points and streaks from recorded intakes.
            val totalPoints = logs.count {
                it.status.equals("taken", ignoreCase = true)
            } * 5

            val (currentStreak, longestStreak) = calculateStreaks(logs)

            // Save the updated rewards to the database.
            val updatedRewards = repository.updateUserRewards(
                userId = userId,
                totalPoints = totalPoints,
                currentStreak = currentStreak,
                longestStreak = longestStreak
            )

            _state.update {
                it.copy(
                    rewards = updatedRewards,
                    rewardsError = null
                )
            }

        } catch (e: Exception) {
            _state.update {
                it.copy(
                    rewardsError = e.message ?: "Unable to update rewards"
                )
            }
        }
    }
    private fun mutate(successMessage: String, action: suspend () -> Unit) {
        if (_state.value.saving) return

        viewModelScope.launch {
            _state.update {
                it.copy(saving = true, error = null, notice = null)
            }

            try {
                action()

                // Reload the latest records from the API.
                val medications = repository.medications(userId)
                val logs = repository.logs(userId)

                _state.update {
                    it.copy(
                        medications = medications,
                        logs = logs,
                        saving = false,
                        notice = successMessage
                    )
                }

                // Recalculate points from actual medication logs.
                try {
                    val rewards = repository.userRewards(userId)
                        ?: repository.createUserRewards(userId)

                    val totalPoints = logs.count {
                        it.status.equals("taken", ignoreCase = true)
                    } * 5

                    val updatedRewards = repository.updateUserRewards(
                        userId = userId,
                        totalPoints = totalPoints,
                        currentStreak = rewards.currentStreak,
                        longestStreak = rewards.longestStreak
                    )

                    _state.update {
                        it.copy(
                            rewards = updatedRewards,
                            rewardsError = null
                        )
                    }
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            rewardsError = e.message ?: "Unable to update rewards"
                        )
                    }
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        saving = false,
                        error = e.message ?: "Operation failed"
                    )
                }
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
