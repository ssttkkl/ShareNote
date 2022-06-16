package me.ssttkkl.sharenote.ui.utils

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

interface ItemModel<out ID> {
    val id: ID
}

inline fun <reified ID, reified IM : ItemModel<ID>> itemModelDiffer() =
    object : DiffUtil.ItemCallback<IM>() {
        override fun areItemsTheSame(oldItem: IM, newItem: IM): Boolean =
            oldItem::class == newItem::class && oldItem.id == newItem.id

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: IM, newItem: IM): Boolean =
            oldItem::class == newItem::class && oldItem == newItem
    }