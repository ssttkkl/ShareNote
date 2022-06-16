package me.ssttkkl.sharenote.ui.noteedit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.ssttkkl.sharenote.databinding.ItemAddTagBinding
import me.ssttkkl.sharenote.databinding.ItemTagBinding
import me.ssttkkl.sharenote.ui.utils.itemModelDiffer

class TagAdapter(
    private val callback: Callback
) : ListAdapter<TagItemModel<*>, TagAdapter.ViewHolder>(itemModelDiffer()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TagItemModel.Tag -> 0
            is TagItemModel.AddTag -> 1
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return when (viewType) {
            0 -> {
                val binding =
                    ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TagViewHolder(binding)
            }
            1 -> {
                val binding =
                    ItemAddTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AddTagViewHolder(binding)
            }
            else -> error("invalid view type")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is TagViewHolder -> holder.item = item as? TagItemModel.Tag
            is AddTagViewHolder -> holder.item = item as? TagItemModel.AddTag
        }
    }

    interface Callback {
        fun onClickTag(tag: TagItemModel.Tag)
        fun onClickAddTag(addTag: TagItemModel.AddTag)
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class TagViewHolder(val binding: ItemTagBinding) : ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { item?.let { callback.onClickTag(it) } }
        }

        var item: TagItemModel.Tag?
            get() = binding.item
            set(value) {
                binding.item = value
                binding.executePendingBindings()
            }
    }

    inner class AddTagViewHolder(binding: ItemAddTagBinding) : ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { item?.let { callback.onClickAddTag(it) } }
        }

        var item: TagItemModel.AddTag? = null
    }
}