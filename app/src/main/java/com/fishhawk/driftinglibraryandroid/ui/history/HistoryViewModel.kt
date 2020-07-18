package com.fishhawk.driftinglibraryandroid.ui.history

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModel() {
    private val readingHistoryList: LiveData<List<ReadingHistory>> =
        readingHistoryRepository.observeAllReadingHistory()

    val filteredReadingHistoryList: MediatorLiveData<List<ReadingHistory>> = MediatorLiveData()

    init {
        filteredReadingHistoryList.addSource(readingHistoryList) { list ->
            val filter = SettingsHelper.historyFilter.getValueDirectly()
            filteredReadingHistoryList.value = filterList(list, filter)
        }
        filteredReadingHistoryList.addSource(SettingsHelper.historyFilter) { filter ->
            val list = readingHistoryList.value
            if (list != null) filteredReadingHistoryList.value = filterList(list, filter)
        }
    }

    private fun filterList(list: List<ReadingHistory>, filter: String): List<ReadingHistory> {
        return when (filter) {
            SettingsHelper.HISTORY_FILTER_ALL -> list
            SettingsHelper.HISTORY_FILTER_FROM_LIBRARY -> list.filter { it.source == null }
            SettingsHelper.HISTORY_FILTER_FROM_SOURCES -> list.filter { it.source != null }
            else -> throw InternalError("Shouldn't reach here")
        }
    }

    fun clearReadingHistory() {
        viewModelScope.launch {
            readingHistoryRepository.clearReadingHistory()
        }
    }
}

@Suppress("UNCHECKED_CAST")
class HistoryViewModelFactory(
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(readingHistoryRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}