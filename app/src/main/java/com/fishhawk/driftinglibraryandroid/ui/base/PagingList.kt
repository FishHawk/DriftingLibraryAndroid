package com.fishhawk.driftinglibraryandroid.ui.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.classic.common.MultipleStatusView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.Event
import com.fishhawk.driftinglibraryandroid.repository.EventObserver
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.hippo.refreshlayout.RefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class NetworkResourceState {
    object Loading : NetworkResourceState()
    object Content : NetworkResourceState()
    object Empty : NetworkResourceState()
    data class Error(val exception: Throwable) : NetworkResourceState()
}

fun <KEY, ITEM> ViewModel.pagingList(
    loadFunction: suspend (key: KEY?) -> Result<Pair<KEY?, List<ITEM>>>
) = object : PagingList<KEY, ITEM>(viewModelScope) {
    override suspend fun load(key: KEY?): Result<Pair<KEY?, List<ITEM>>> = loadFunction(key)
}

abstract class PagingList<KEY, ITEM>(private val scope: CoroutineScope) {
    val state: MediatorLiveData<NetworkResourceState> = MediatorLiveData()
    val data: MediatorLiveData<List<ITEM>> = MediatorLiveData()

    private val _refreshFinish: MutableLiveData<Event<Feedback>> = MutableLiveData()
    val refreshFinish: LiveData<Event<Feedback>> = _refreshFinish

    private val _fetchMoreFinish: MutableLiveData<Event<Feedback>> = MutableLiveData()
    val fetchMoreFinish: LiveData<Event<Feedback>> = _fetchMoreFinish

    private var nextKey: KEY? = null
    private var isFinished: Boolean = false

    protected abstract suspend fun load(key: KEY?): Result<Pair<KEY?, List<ITEM>>>

    fun reload() {
        nextKey = null
        isFinished = false

        state.value = NetworkResourceState.Loading
        data.value = emptyList()

        scope.launch {
            load(nextKey).onSuccess { (key, it) ->
                nextKey = key
                isFinished = it.isEmpty()

                state.value =
                    if (it.isEmpty()) NetworkResourceState.Empty
                    else NetworkResourceState.Content
                data.value = it
            }.onFailure {
                state.value = NetworkResourceState.Error(it)
                data.value = emptyList()
            }
        }
    }

    fun refresh() {
        scope.launch {
            load(null).onSuccess { (key, it) ->
                nextKey = key
                isFinished = it.isEmpty()

                state.value =
                    if (it.isEmpty()) NetworkResourceState.Empty
                    else NetworkResourceState.Content
                data.value = it

                _refreshFinish.value = Event(
                    if (it.isEmpty()) Feedback.Hint(R.string.error_hint_empty_refresh_result)
                    else Feedback.Silent
                )
            }.onFailure {
                _refreshFinish.value = Event(Feedback.Failure(it))
            }
        }
    }

    fun fetchMore() {
        if (isFinished)
            _fetchMoreFinish.value =
                Event(Feedback.Hint(R.string.error_hint_empty_fetch_more_result))

        scope.launch {
            load(nextKey).onSuccess { (key, it) ->
                nextKey = key
                isFinished = it.isEmpty()

                state.value =
                    if (it.isEmpty()) NetworkResourceState.Empty
                    else NetworkResourceState.Content
                data.value =
                    (data.value?.toMutableList() ?: mutableListOf())
                        .apply { addAll(it) }

                _fetchMoreFinish.value = Event(
                    if (it.isEmpty()) Feedback.Hint(R.string.error_hint_empty_fetch_more_result)
                    else Feedback.Silent
                )
            }.onFailure {
                _fetchMoreFinish.value = Event(Feedback.Failure(it))
            }
        }
    }
}

fun <Item> Fragment.bindToPagingList(
    multipleStatusView: MultipleStatusView,
    refreshLayout: RefreshLayout,
    component: PagingList<*, Item>,
    adapter: BaseAdapter<Item>
) {
    component.data.observe(viewLifecycleOwner) { adapter.setList(it) }

    component.state.observe(viewLifecycleOwner) {
        when (it) {
            is NetworkResourceState.Loading -> multipleStatusView.showLoading()
            is NetworkResourceState.Content -> multipleStatusView.showContent()
            is NetworkResourceState.Empty -> multipleStatusView.showEmpty()
            is NetworkResourceState.Error -> multipleStatusView.showError(it.exception.message)
        }
    }

    component.refreshFinish.observe(viewLifecycleOwner, EventObserver {
        refreshLayout.isHeaderRefreshing = false
        processFeedback(it)
    })

    component.fetchMoreFinish.observe(viewLifecycleOwner, EventObserver {
        refreshLayout.isFooterRefreshing = false
        processFeedback(it)
    })

    with(refreshLayout) {
        setHeaderColorSchemeResources(
            R.color.loading_indicator_red,
            R.color.loading_indicator_purple,
            R.color.loading_indicator_blue,
            R.color.loading_indicator_cyan,
            R.color.loading_indicator_green,
            R.color.loading_indicator_yellow
        )
        setFooterColorSchemeResources(
            R.color.loading_indicator_red,
            R.color.loading_indicator_blue,
            R.color.loading_indicator_green,
            R.color.loading_indicator_orange
        )

        setOnRefreshListener(
            object : RefreshLayout.OnRefreshListener {
                override fun onHeaderRefresh() = component.refresh()
                override fun onFooterRefresh() = component.fetchMore()
            }
        )
    }
}
