package com.fishhawk.driftinglibraryandroid.gallery

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.FragmentGalleryBinding
import com.fishhawk.driftinglibraryandroid.repository.data.TagGroup
import com.fishhawk.driftinglibraryandroid.repository.data.Collection
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.google.android.flexbox.FlexboxLayout

class GalleryFragment : Fragment() {
    private lateinit var viewModel: GalleryViewModel
    private lateinit var binding: FragmentGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run { ViewModelProvider(this)[GalleryViewModel::class.java] }
            ?: throw Exception("Invalid Activity")
        viewModel.openManga(arguments?.getString("id")!!)

        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        sharedElementReturnTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        activity?.supportPostponeEnterTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id: String? = arguments?.getString("id")
        val title: String? = arguments?.getString("title")
        val thumb: String? = arguments?.getString("thumb")

        (activity as? AppCompatActivity)?.supportActionBar?.title = title

        binding.title.text = title
        binding.thumb.apply {
            binding.thumb.transitionName = id
            Glide.with(this).load(thumb)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(300, 400)
                .apply(RequestOptions().dontTransform())
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        activity?.supportStartPostponedEnterTransition()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        activity?.supportStartPostponedEnterTransition()
                        return false
                    }
                })
                .into(this)
        }

        viewModel.mangaDetail.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Success -> {
                    bindTags(result.data.tags, binding.tags)
                    binding.content.removeAllViews()
                    for ((index, collection) in result.data.collections.withIndex()) {
                        bindCollection(collection, index, binding.content)
                    }
                }
                is Result.Error -> println()
                is Result.Loading -> println()
            }
        })
    }

    private fun bindCollection(
        collection: Collection,
        collectionIndex: Int,
        contentLayout: LinearLayout
    ) {
        val collectionLayout = layoutInflater.inflate(
            R.layout.item_collection, contentLayout, false
        ) as LinearLayout
        contentLayout.addView(collectionLayout)

        val titleView = collectionLayout.findViewById<TextView>(R.id.title)
        val chaptersLayout = collectionLayout.findViewById<GridLayout>(R.id.chapters)
        val noChaptersView = collectionLayout.findViewById<TextView>(R.id.no_chapters)

        if (collection.chapters.isEmpty()) {
            titleView.visibility = View.GONE
            return
        } else {
            noChaptersView.visibility = View.GONE
        }

        if (collection.title == "") {
            titleView.visibility = View.GONE
        } else {
            titleView.text = collection.title
        }

        for ((index, chapter) in collection.chapters.withIndex()) {
            val button = layoutInflater.inflate(
                R.layout.item_chapter_button, chaptersLayout, false
            ) as Button
            chaptersLayout.addView(button)

            button.text = chapter
            button.setOnClickListener {
                viewModel.openChapter(collectionIndex, index)
                button.findNavController().navigate(R.id.action_gallery_to_reader, null)
            }
        }
        for (i in 1..(3 - collection.chapters.size)) {
            val button = layoutInflater.inflate(
                R.layout.item_chapter_button, chaptersLayout, false
            ) as Button
            button.visibility = View.INVISIBLE
            chaptersLayout.addView(button)
        }
    }

    private fun bindTags(
        tags: List<TagGroup>?,
        tagsLayout: LinearLayout
    ) {
        val noTagsView = tagsLayout.findViewById<TextView>(R.id.no_tags)

        tagsLayout.removeViews(1, tagsLayout.childCount - 1)
        if (tags == null || tags.isEmpty()) {
            noTagsView.visibility = View.VISIBLE
            return
        } else {
            noTagsView.visibility = View.GONE
        }

        for (tagGroup in tags) {
            val tagGroupLayout = layoutInflater.inflate(
                R.layout.item_tag_group, tagsLayout, false
            ) as LinearLayout
            tagsLayout.addView(tagGroupLayout)

            // bind tag group key
            val tagGroupKeyView = tagGroupLayout.findViewById<TextView>(R.id.key)
            tagGroupKeyView.text = tagGroup.key

            // bind tag group value
            val tagGroupValueLayout: FlexboxLayout = tagGroupLayout.findViewById(R.id.value)
            for (value in tagGroup.value) {
                val tagGroupValueView = layoutInflater.inflate(
                    R.layout.item_tag, tagGroupValueLayout, false
                ) as TextView
                tagGroupValueLayout.addView(tagGroupValueView)
                tagGroupValueView.text = value
            }
        }
    }
}
