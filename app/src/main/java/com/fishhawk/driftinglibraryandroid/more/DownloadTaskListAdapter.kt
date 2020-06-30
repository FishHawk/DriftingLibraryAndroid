package com.fishhawk.driftinglibraryandroid.more

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.databinding.DownloadTaskCardBinding
import com.fishhawk.driftinglibraryandroid.repository.data.Order
import com.fishhawk.driftinglibraryandroid.repository.data.OrderStatus

class DownloadTaskListAdapter(
    private val activity: Activity,
    private var data: List<Order>
) : RecyclerView.Adapter<DownloadTaskListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DownloadTaskListAdapter.ViewHolder {
        return ViewHolder(
            DownloadTaskCardBinding.inflate(
                LayoutInflater.from(activity),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: DownloadTaskListAdapter.ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(private val binding: DownloadTaskCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Order) {
            binding.taskId.text = item.id.toString()
            binding.source.text = item.source
            binding.sourceMangaId.text = item.sourceMangaId
            binding.targetMangaId.text = item.targetMangaId

            when (item.status) {
                OrderStatus.COMPLETED -> {
                    binding.startButton.visibility = View.GONE
                    binding.pauseButton.visibility = View.GONE
                }
                OrderStatus.ERROR, OrderStatus.PAUSED -> {
                    binding.pauseButton.visibility = View.GONE
                }
                OrderStatus.WAITING, OrderStatus.PROCESSING -> {
                    binding.startButton.visibility = View.GONE
                }
            }
        }
    }
}