package com.fishhawk.driftinglibraryandroid.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerViewAdapter<ITEM, VH : BaseRecyclerViewAdapter.ViewHolder<ITEM>>(
    private val list: MutableList<ITEM>
) :
    RecyclerView.Adapter<VH>() {

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    fun changeList(newList: MutableList<ITEM>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    abstract class ViewHolder<ITEM>(binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(item: ITEM)
    }
}
