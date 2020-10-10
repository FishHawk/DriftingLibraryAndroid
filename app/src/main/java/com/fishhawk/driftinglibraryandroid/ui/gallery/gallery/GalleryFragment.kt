package com.fishhawk.driftinglibraryandroid.ui.gallery.gallery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.databinding.GalleryFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.EventObserver
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.extension.*
import com.fishhawk.driftinglibraryandroid.ui.gallery.*
import com.fishhawk.driftinglibraryandroid.util.FileUtil
import kotlinx.android.synthetic.main.gallery_fragment.view.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class GalleryFragment : Fragment() {
    private val viewModel: GalleryViewModel by viewModels {
        val application = requireActivity().application as MainApplication
        GalleryViewModelFactory(application)
    }
    private lateinit var binding: GalleryFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GalleryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = requireActivity().intent.extras!!
        val id: String = arguments.getString("id")!!
        val title: String = arguments.getString("title")!!
        val providerId: String? = arguments.getString("providerId")
        val thumb: String = arguments.getString("thumb")!!

        if (providerId == null) viewModel.openMangaFromLibrary(id)
        else viewModel.openMangaFromProvider(providerId, id)

        binding.info =
            GalleryInfo(
                providerId,
                title
            )
        setupThumb(thumb)
        setupActionButton()

        binding.thumbCard.setOnLongClickListener {
            GalleryThumbSheet(
                requireContext(),
                { saveThumb() },
                {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, 1000)
                }
            ).show()
            true
        }

        val tagAdapter =
            TagGroupListAdapter(
                requireContext()
            )
        tagAdapter.onTagClicked = { key, value ->
            val keywords = if (key.isBlank()) value else "${key}:$value"
            requireActivity().navToMainActivity(keywords)
        }
        binding.tags.adapter = tagAdapter

        val contentAdapter =
            ContentAdapter(
                requireActivity(),
                id,
                providerId
            )
        binding.chapters.adapter = contentAdapter

        binding.displayModeButton.setOnClickListener {
            SettingsHelper.chapterDisplayMode.setNextValue()
        }
        binding.displayOrderButton.setOnClickListener {
            SettingsHelper.chapterDisplayOrder.setNextValue()
        }

        SettingsHelper.chapterDisplayMode.observe(viewLifecycleOwner, Observer {
            binding.displayModeButton.setIconResource(getChapterDisplayModeIcon())
            binding.chapters.viewMode = when (it) {
                SettingsHelper.ChapterDisplayMode.GRID -> ContentView.ViewMode.GRID
                SettingsHelper.ChapterDisplayMode.LINEAR -> ContentView.ViewMode.LINEAR
            }
        })

        SettingsHelper.chapterDisplayOrder.observe(viewLifecycleOwner, Observer {
            binding.chapters.viewOrder = when (it) {
                SettingsHelper.ChapterDisplayOrder.ASCEND -> ContentView.ViewOrder.ASCEND
                SettingsHelper.ChapterDisplayOrder.DESCEND -> ContentView.ViewOrder.DESCEND
            }
        })

        viewModel.notification.observe(viewLifecycleOwner, EventObserver {
            showErrorMessage(it)
        })

        viewModel.detail.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Success -> {
                    binding.multipleStatusView.showContent()

                    val detail = result.data
                    binding.info =
                        GalleryInfo(
                            detail
                        )
                    binding.description.setOnClickListener {
                        binding.description.maxLines =
                            if (binding.description.maxLines < Int.MAX_VALUE) Int.MAX_VALUE else 3
                    }
                    if (!detail.metadata.tags.isNullOrEmpty())
                        tagAdapter.setList(detail.metadata.tags)
                    if (detail.collections.isNotEmpty())
                        binding.chapters.collections = detail.collections
                }
                is Result.Error -> binding.multipleStatusView.showError(result.exception.message)
                is Result.Loading -> binding.multipleStatusView.showLoading()
            }
        })

        viewModel.history.observe(viewLifecycleOwner, Observer { history ->
            binding.contentView.chapters.markedPosition = history?.let {
                MarkedPosition(
                    it.collectionIndex,
                    it.chapterIndex,
                    it.pageIndex
                )
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            val uri = data?.data
            val content =
                uri?.let { requireContext().contentResolver.openInputStream(it)?.readBytes() }
            val type =
                uri?.let { requireContext().contentResolver.getType(uri)?.toMediaTypeOrNull() }
            if (content != null && type != null)
                viewModel.updateThumb(content.toRequestBody(type))
        }
    }

    private fun setupThumb(thumb: String) {
        Glide.with(this)
            .load(thumb)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(binding.thumb)

        Glide.with(this)
            .load(thumb)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(binding.backdrop)
    }

    private fun saveThumb() {
        val detail = (viewModel.detail.value as? Result.Success)?.data
        if (detail == null) {
            binding.root.makeToast("Manga not loaded")
        } else {
            val url = detail.thumb
            if (url == null) {
                binding.root.makeToast("No cover")
            } else {
                val filename = "${detail.id}-thumb"
                val uri = FileUtil.createImageInGallery(requireContext(), filename)
                if (uri == null) {
                    binding.root.makeToast("Image already exist")
                } else {
                    val activity = requireContext()
                    lifecycleScope.launch {
                        try {
                            FileUtil.saveImage(activity, url, uri)
                            binding.root.makeToast("Image saved")
                        } catch (e: Throwable) {
                            binding.root.makeToast(e.message ?: "Unknown error")
                        }
                    }
                }
            }
        }
    }

    private fun setupActionButton() {
        binding.readButton.setOnClickListener {
            (viewModel.detail.value as? Result.Success)?.let { result ->
                val detail = result.data
                viewModel.history.value?.let { history ->
                    requireActivity().navToReaderActivity(
                        detail.id,
                        detail.providerId,
                        history.collectionIndex,
                        history.chapterIndex,
                        history.pageIndex
                    )
                } ?: requireActivity().navToReaderActivity(detail.id, detail.providerId)
            }
        }
        binding.subscribeButton.setOnClickListener { viewModel.subscribe() }
        binding.downloadButton.setOnClickListener { viewModel.download() }
    }
}
