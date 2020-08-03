package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.repository.data.Collection

class ContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    private val linearLayoutManager = LinearLayoutManager(context)
    private val gridLayoutManager = GridLayoutManager(context, 3).apply {
        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter?.getItemViewType(position)) {
                    ContentAdapter.ViewType.CHAPTER.value -> 1
                    ContentAdapter.ViewType.CHAPTER_MARKED.value -> 1
                    else -> 3
                }
            }
        }
    }

    init {
        layoutManager = gridLayoutManager
    }

    enum class ViewMode { GRID, LINEAR }
    enum class ViewOrder { ASCEND, DESCEND }

    var viewMode = ViewMode.GRID
        set(value) {
            field = value
            adapter?.viewMode = when (value) {
                ViewMode.GRID -> ContentAdapter.ViewMode.GRID
                ViewMode.LINEAR -> ContentAdapter.ViewMode.LINEAR
            }
            layoutManager = when (value) {
                ViewMode.GRID -> gridLayoutManager
                ViewMode.LINEAR -> linearLayoutManager
            }
            adapter = adapter
        }

    var viewOrder = ViewOrder.ASCEND
        set(value) {
            field = value
            adapter = adapter
        }

    var adapter: ContentAdapter? = null
        set(value) {
            field = value
            updateAdapterContent()
            super.setAdapter(value)
        }

    var collections: List<Collection>? = null
        set(value) {
            field = value
            adapter = adapter
        }

    private fun updateAdapterContent() {
        if (adapter == null || collections == null) return

        val items = mutableListOf<ContentItem>()

        for ((collectionIndex, collection) in collections!!.withIndex()) {
            if (viewMode == ViewMode.GRID && collection.title.isNotEmpty())
                items.add(ContentItem.CollectionHeader(collection.title))

            val it = when (viewOrder) {
                ViewOrder.ASCEND -> collection.chapters.withIndex()
                ViewOrder.DESCEND -> collection.chapters.asReversed().withIndex()
            }
            for ((chapterIndex, chapter) in it) {
                items.add(
                    ContentItem.Chapter(chapter.name, chapter.title, collectionIndex, chapterIndex)
                )
            }
        }
        adapter!!.changeList(items)
    }
}
