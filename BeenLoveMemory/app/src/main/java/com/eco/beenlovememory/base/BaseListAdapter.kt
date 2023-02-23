package com.eco.beenlovememory.base

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseListAdapter<M, V : ViewDataBinding, VH : RecyclerView.ViewHolder>
    (context: Context, config: DiffUtil.ItemCallback<M>) :
    ListAdapter<M, VH>(config){
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    var activity: Activity? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return createViewHolder(parent)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        bindHolder(holder, position)
    }

    override fun submitList(list: MutableList<M>?) {
        val l = mutableListOf<M>()
        l.addAll(list ?: mutableListOf())
        super.submitList(list?.toMutableList())
        afterSubmitList(list!!.toMutableList())
    }

    override fun getItemCount(): Int {
        return if (currentList.toMutableList().isEmpty()) 0 else currentList.size
    }

    protected abstract fun itemLayout(): Int
    protected abstract fun afterSubmitList(list: MutableList<M>)
    protected abstract fun createViewHolder(parent: ViewGroup): VH
    protected abstract fun bindHolder(holder: VH, position: Int)

    open fun init(activity: Activity) {}
    open fun destroy() {}
    open fun next(model: M) {}
    open fun itemCache(): M? {
        return null
    }

    open fun createViewDataBinding(parent: ViewGroup): V {
        return DataBindingUtil.inflate(layoutInflater, itemLayout(), parent, false)
    }
}