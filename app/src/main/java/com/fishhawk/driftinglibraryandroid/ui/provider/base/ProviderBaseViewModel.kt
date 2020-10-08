package com.fishhawk.driftinglibraryandroid.ui.provider.base

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.ui.base.DownloadCreatedNotification
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableListViewModelWithFetchMore
import com.fishhawk.driftinglibraryandroid.ui.base.SubscriptionCreatedNotification

abstract class ProviderBaseViewModel(
    private val providerId: String,
    private val remoteDownloadRepository: RemoteDownloadRepository,
    private val remoteSubscriptionRepository: RemoteSubscriptionRepository
) : RefreshableListViewModelWithFetchMore<MangaOutline>() {
    protected var page = 1
    protected val option: MutableMap<String, Int> = mutableMapOf()

    fun selectOption(optionType: String, optionIndex: Int) {
        option[optionType] = optionIndex
    }

    override fun onRefreshSuccess(data: List<MangaOutline>) {
        page = 1
    }

    override fun onFetchMoreSuccess(data: List<MangaOutline>) {
        if (data.isNotEmpty()) page += 1
    }

    fun download(id: String, title: String) =
        viewModelScope.launch(Dispatchers.Main) {
            val result = remoteDownloadRepository.postDownloadTask(providerId, id, title)
            resultWarp(result) { notify(DownloadCreatedNotification()) }
        }

    fun subscribe(id: String, title: String) =
        viewModelScope.launch(Dispatchers.Main) {
            val result = remoteSubscriptionRepository.postSubscription(providerId, id, title)
            resultWarp(result) { notify(SubscriptionCreatedNotification()) }
        }
}