package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ReaderChapterImageHorizontalBinding

class ImageHorizontalListAdapter(
    private val context: Context,
    private var data: List<String>
) : RecyclerView.Adapter<ImageHorizontalListAdapter.ViewHolder>() {
    var onCardLongClicked: ((Int, String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ReaderChapterImageHorizontalBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position], position)
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(private val binding: ReaderChapterImageHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String, position: Int) {
            binding.content.setImageResource(android.R.color.transparent)
            binding.background.visibility = View.VISIBLE
            binding.progress.visibility = View.VISIBLE
            binding.errorHint.visibility = View.GONE

            binding.number.text = (position + 1).toString()
            binding.content.setZoomable(true)
            binding.content.setOnLongClickListener {
                onCardLongClicked?.invoke(position, item)
                true
            }
            Glide.with(context)
                .asBitmap()
                .load(item)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .listener(object : RequestListener<Bitmap> {
                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.background.visibility = View.GONE
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progress.visibility = View.GONE
                        binding.errorHint.visibility = View.VISIBLE
                        if (e != null) binding.errorHint.text = e.message
                        else binding.errorHint.setText(R.string.image_unknown_error_hint)
                        return false
                    }
                })
                .into(binding.content)
        }
    }
}