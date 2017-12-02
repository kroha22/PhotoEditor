package com.kroha22.photoEditor.ui.listAdapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import java.util.*

/**
 * Created by Olga
 * on 10.11.2017.
 */

/*
 Простой адаптер списка RecyclerView
 */
abstract class CollectionRecycleAdapter<T>(val context: Context) : RecyclerView.Adapter<RecycleViewHolder<T>>() {

    private val list: MutableList<T>

    val collection: List<T>
        get() = list

    init {
        list = ArrayList()
    }

    override fun onBindViewHolder(holder: RecycleViewHolder<T>, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setItems(collection: Collection<T>) {
        list.clear()
        list.addAll(collection)
        notifyDataSetChanged()
    }

    private fun getItem(position: Int): T {
        return list[position]
    }

}

abstract class RecycleViewHolder<in T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val root: View
        get() = itemView

    init {
        create(itemView)
    }

    protected abstract fun create(rootView: View)

    abstract fun bind(model: T)

}
