package com.fishhawk.driftinglibraryandroid.reader

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.FragmentReaderBinding
import com.fishhawk.driftinglibraryandroid.gallery.GalleryViewModel
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.google.android.material.snackbar.Snackbar

class ReaderFragment : Fragment() {
    private lateinit var viewModel: GalleryViewModel
    private lateinit var binding: FragmentReaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run { ViewModelProvider(this)[GalleryViewModel::class.java] }
            ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner

        setupReaderLayout()
        setupMenuLayout()
        setupHorizontalReader()
        setupVerticalReader()

        viewModel.isHorizontalReaderEnable.observe(viewLifecycleOwner, Observer {
            viewModel.startPage = viewModel.chapterPosition.value!!
            if (it) {
                viewModel.horizontalReaderContent.value = viewModel.verticalReaderContent.value
            } else {
                viewModel.verticalReaderContent.value = viewModel.horizontalReaderContent.value
            }
        })

        viewModel.layoutDirection.observe(viewLifecycleOwner, Observer {
            binding.horizontalReader.apply {
                viewModel.layoutDirection.value?.let { layoutDirection = it }
                val temp = currentItem
                adapter = adapter
                gotoPage(temp)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        activity?.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onStop() {
        super.onStop()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
        activity?.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    private fun setupReaderLayout() {
        val gotoPrevPage = {
            if (viewModel.chapterPosition.value!! == 0) openPrevChapter()
            else gotoPage(viewModel.chapterPosition.value!! - 1)
        }
        val gotoNextPage = {
            if (viewModel.chapterPosition.value!! == viewModel.chapterSize.value!!) openNextChapter()
            else gotoPage(viewModel.chapterPosition.value!! + 1)
        }

        binding.readerLayout.apply {
            onClickLeftAreaListener = {
                when (viewModel.readingDirection.value) {
                    SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT -> gotoNextPage()
                    else -> gotoPrevPage()
                }
            }
            onClickRightAreaListener = {
                when (viewModel.readingDirection.value) {
                    SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT -> gotoPrevPage()
                    else -> gotoNextPage()
                }
            }
            onClickCenterAreaListener = { viewModel.isMenuVisible.value = true }
        }
    }

    private fun setupMenuLayout() {
        binding.menuLayout.setOnClickListener { viewModel.isMenuVisible.value = false }

        binding.radioGroupDirection.setOnCheckedChangeListener { _, checkedId ->
            val direction = when (checkedId) {
                R.id.radio_left_to_right -> SettingsHelper.READING_DIRECTION_LEFT_TO_RIGHT
                R.id.radio_right_to_left -> SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT
                R.id.radio_vertical -> SettingsHelper.READING_DIRECTION_VERTICAL
                else -> SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT
            }
            viewModel.readingDirection.value = direction
            SettingsHelper.setReadingDirection(direction)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seek: SeekBar) {}
            override fun onStopTrackingTouch(seek: SeekBar) {
                gotoPage(seek.progress)
            }
        })
    }

    private fun setupHorizontalReader() {
        binding.horizontalReader.apply {
            offscreenPageLimit = 5
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                private var isEdge: Boolean = true

                override fun onPageScrollStateChanged(state: Int) {
                    when (state) {
                        ViewPager2.SCROLL_STATE_IDLE -> {
                            if (!isEdge && !viewModel.isLoading.value!!) {
                                if (currentItem == 0) openPrevChapter()
                                else if (currentItem == adapter!!.itemCount - 1) openNextChapter()
                            }
                        }
                        ViewPager2.SCROLL_STATE_DRAGGING -> isEdge = false
                        ViewPager2.SCROLL_STATE_SETTLING -> isEdge = true
                    }
                }

                override fun onPageSelected(position: Int) {
                    viewModel.chapterPosition.value = position
                }
            })
        }

        viewModel.horizontalReaderContent.observe(viewLifecycleOwner, Observer { content ->
            binding.horizontalReader.apply {
                adapter = ImageHorizontalListAdapter(context, content)
                gotoPage(viewModel.startPage)

                viewModel.chapterPosition.value = currentItem
                viewModel.chapterSize.value = content.size
            }
        })
    }

    private fun setupVerticalReader() {
        binding.verticalReader.apply {
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    viewModel.chapterPosition.value =
                        (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                }
            })
        }

        viewModel.verticalReaderContent.observe(viewLifecycleOwner, Observer { content ->
            binding.verticalReader.apply {
                adapter = ImageVerticalListAdapter(context, content)
                gotoPage(viewModel.startPage)

                viewModel.chapterPosition.value =
                    (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                viewModel.chapterSize.value = content.size
            }
        })
    }

    private fun openPrevChapter() {
        if (!viewModel.openPrevChapter()) makeSnakeBar(getString(R.string.reader_no_prev_chapter_hint))
    }

    private fun openNextChapter() {
        if (!viewModel.openNextChapter()) makeSnakeBar(getString(R.string.reader_no_next_chapter_hint))
    }

    private fun gotoPage(position: Int) {
        if (viewModel.isHorizontalReaderEnable.value!!)
            binding.horizontalReader.setCurrentItem(position, false)
        else
            (binding.verticalReader.layoutManager as LinearLayoutManager)
                .scrollToPositionWithOffset(position, 0)
    }

    private fun makeSnakeBar(content: String) {
        view?.let { Snackbar.make(it, content, Snackbar.LENGTH_SHORT).show() }
    }
}