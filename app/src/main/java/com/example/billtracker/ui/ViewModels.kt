package com.example.billtracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.billtracker.data.local.AppDatabase
import com.example.billtracker.data.local.Bill
import com.example.billtracker.data.repository.BillRepository
import com.example.billtracker.reminders.ReminderDates
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

// ---------- Shared UI state (Loading / Success / Error) ----------
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

// ---------- Bill List ----------
class BillListViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = BillRepository(AppDatabase.getInstance(app).billDao())

    // Room's Flow re-emits every time the table changes, so the list updates by itself.
    val state: StateFlow<UiState<List<Bill>>> = repo.allBills
        .map<List<Bill>, UiState<List<Bill>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Could not load bills")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)
}

// ---------- Bill Details + Delete ----------
@Suppress("unused")
class BillDetailsViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle, // filled automatically from the nav argument "billId"
) : AndroidViewModel(app) {
    private val repo = BillRepository(AppDatabase.getInstance(app).billDao())
    private val billId: Int = checkNotNull(savedStateHandle["billId"])

    val state: StateFlow<UiState<Bill?>> = repo.getBill(billId)
        .map<Bill?, UiState<Bill?>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Could not load bill")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    fun deleteBill(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repo.getBill(billId).first()?.let { repo.deleteBill(it) }
            onDeleted()
        }
    }
}

// ---------- Dashboard ----------
data class DashboardData(
    val totalDue: Double,
    val totalPaid: Double,
    val totalUnpaid: Double,
    val dueSoon: List<Bill>,
)

class DashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = BillRepository(AppDatabase.getInstance(app).billDao())

    val state: StateFlow<UiState<DashboardData>> = repo.allBills
        .map<List<Bill>, UiState<DashboardData>> { UiState.Success(buildDashboard(it)) }
        .catch { emit(UiState.Error(it.message ?: "Could not load dashboard")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private fun buildDashboard(all: List<Bill>): DashboardData {
        val cal = Calendar.getInstance()
        val thisMonth = cal[Calendar.MONTH]
        val thisYear = cal[Calendar.YEAR]

        // Bills whose due date falls in the current month
        val monthBills = all.filter { bill ->
            val dueMillis = ReminderDates.parseDueDate(bill.dueDate) ?: return@filter false
            val c = Calendar.getInstance().apply { timeInMillis = dueMillis }
            (c[Calendar.MONTH] == thisMonth) && (c[Calendar.YEAR] == thisYear)
        }

        val nowMillis = System.currentTimeMillis()
        val dueSoon = all
            .asSequence()
            .filter { bill ->
                if (bill.isPaid) return@filter false
                val days = ReminderDates.daysUntil(nowMillis, bill.dueDate)
                ((days != null) && (days in 0..7))
            }
            .sortedBy { bill ->
                ReminderDates.parseDueDate(bill.dueDate) ?: Long.MAX_VALUE
            }
            .toList()

        return DashboardData(
            totalDue = monthBills.sumOf { it.amount },
            totalPaid = monthBills.asSequence().filter { it.isPaid }.sumOf { it.amount },
            totalUnpaid = monthBills.asSequence().filter { !it.isPaid }.sumOf { it.amount },
            dueSoon = dueSoon,
        )
    }
}
