package com.fishhawk.driftinglibraryandroid.ui.globalsearch

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.data.Result
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import kotlinx.coroutines.launch

class GlobalSearchViewModel(
    private val remoteLibraryRepository: RemoteProviderRepository
) : ViewModel() {
    val keywords: MutableLiveData<String> = MutableLiveData("")

    private val providerList: LiveData<Result<List<ProviderInfo>>?> = liveData {
        emit(null)
        emit(remoteLibraryRepository.getProvidersInfo())
    }

    val searchGroupList: MediatorLiveData<List<SearchGroup>> = MediatorLiveData()

    init {
        searchGroupList.addSource(keywords) { search() }
        searchGroupList.addSource(providerList) { search() }
    }

    private fun search() =
        viewModelScope.launch {
            val keywords = keywords.value ?: return@launch
            val providerList = (providerList.value as? Result.Success)?.data ?: return@launch

            searchGroupList.value = providerList.map { info ->
                SearchGroup(info, null)
            }

            searchGroupList.value = providerList.map { info ->
                SearchGroup(info, remoteLibraryRepository.search(info.id, keywords, 1))
            }
        }
}