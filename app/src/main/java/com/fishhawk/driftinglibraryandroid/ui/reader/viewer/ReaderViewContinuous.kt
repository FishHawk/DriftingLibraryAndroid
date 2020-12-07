package com.fishhawk.driftinglibraryandroid.ui.reader.viewer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.databinding.ReaderViewContinuousBinding

class ReaderViewContinuous constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ReaderView(context, attrs, defStyleAttr) {

    private val binding = ReaderViewContinuousBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    val layoutManager = LinearLayoutManager(context)

    init {
        adapter.isContinuous = true

        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.content.layoutManager = layoutManager
        binding.content.adapter = adapter

        binding.content.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var isUserInput = false
            private var hasSetting = false
            private var reachStart = false
            private var reachEnd = false

            fun reset() {
                hasSetting = false
                isUserInput = false
                reachStart = false
                reachEnd = false
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, state: Int) {
                when (state) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        reachStart = reachStart && !canScrollBackward()
                        reachEnd = reachEnd && !canScrollForward()

                        if (isUserInput) {
                            // TODO: what if reachStart and reachEnd are both true
                            if (reachStart) onRequestPrevChapter?.invoke()
                            if (reachEnd) onRequestNextChapter?.invoke()
                        }
                        reset()
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        isUserInput = true
                        reachStart = !canScrollBackward()
                        reachEnd = !canScrollForward()
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        hasSetting = true
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                onScrolled?.invoke(getPage())
            }
        })
    }

    override fun getPage(): Int = layoutManager.findFirstVisibleItemPosition()
    override fun setPage(page: Int) = binding.content.scrollToPosition(page)

    override fun canScrollForward(): Boolean {
        val direction = if (layoutManager.reverseLayout) -1 else 1
        return when (layoutManager.orientation) {
            RecyclerView.HORIZONTAL -> binding.content.canScrollHorizontally(direction)
            RecyclerView.VERTICAL -> binding.content.canScrollVertically(direction)
            else -> false
        }
    }

    override fun canScrollBackward(): Boolean {
        val direction = if (layoutManager.reverseLayout) 1 else -1
        return when (layoutManager.orientation) {
            RecyclerView.HORIZONTAL -> binding.content.canScrollHorizontally(direction)
            RecyclerView.VERTICAL -> binding.content.canScrollVertically(direction)
            else -> false
        }
    }

    private val smoothScrollFactor = 0.8
    override fun toNext() {
        if (canScrollForward()) {
            val scrollDistanceH = (width * smoothScrollFactor).toInt()
            val scrollDistanceV = (height * smoothScrollFactor).toInt()
            binding.content.smoothScrollBy(scrollDistanceH, scrollDistanceV)
        } else onRequestNextChapter?.invoke()
    }

    override fun toPrev() {
        if (canScrollBackward()) {
            val scrollDistanceH = -(width * smoothScrollFactor).toInt()
            val scrollDistanceV = -(height * smoothScrollFactor).toInt()
            binding.content.smoothScrollBy(scrollDistanceH, scrollDistanceV)
        } else onRequestPrevChapter?.invoke()
    }
}