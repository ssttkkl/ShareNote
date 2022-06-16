package me.ssttkkl.sharenote.ui.noteedit.draft

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.databinding.ItemDraftBinding
import me.ssttkkl.sharenote.ui.utils.itemModelDiffer

class DraftAdapter(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val callback: Callback
) : PagingDataAdapter<DraftItemModel, DraftAdapter.ViewHolder>(itemModelDiffer()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftAdapter.ViewHolder {
        val binding = ItemDraftBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DraftAdapter.ViewHolder, position: Int) {
        holder.item = getItem(position)
    }

    interface Callback {
        fun onClick(item: DraftItemModel)
        fun onClickRemove(item: DraftItemModel)
    }

    inner class ViewHolder(
        private val binding: ItemDraftBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.lifecycleOwner = lifecycleOwner
            binding.root.setOnClickListener { item?.let { item -> callback.onClick(item) } }
            binding.root.setOnLongClickListener { showPopup(it); true }
        }

        var item: DraftItemModel?
            get() = binding.draft
            set(value) {
                binding.draft = value
                binding.executePendingBindings()
            }

        private fun showPopup(v: View) {
            PopupMenu(context, v).apply {
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.remove -> item?.let { item -> callback.onClickRemove(item) }
                        else -> return@setOnMenuItemClickListener false
                    }
                    return@setOnMenuItemClickListener true
                }

                menuInflater.inflate(R.menu.note_options, menu)
                show()
            }
        }
    }
}