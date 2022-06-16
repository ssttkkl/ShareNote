package me.ssttkkl.sharenote.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.databinding.ItemNoteBinding
import me.ssttkkl.sharenote.ui.utils.itemModelDiffer

class NoteAdapter(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val callback: Callback
) : PagingDataAdapter<NoteItemModel, NoteAdapter.ViewHolder>(itemModelDiffer()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = getItem(position)
    }

    interface Callback {
        fun onClick(item: NoteItemModel)
        fun onClickCreateInvite(item: NoteItemModel)
        fun onClickCreateReadonlyInvite(item: NoteItemModel)
        fun onClickEdit(item: NoteItemModel)
        fun onClickRemove(item: NoteItemModel)
        fun onClickPermissionManage(item: NoteItemModel)
    }

    inner class ViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.lifecycleOwner = lifecycleOwner
            binding.root.setOnClickListener { item?.let { item -> callback.onClick(item) } }
            binding.root.setOnLongClickListener { showPopup(it); true }
        }

        var item: NoteItemModel?
            get() = binding.note
            set(value) {
                binding.note = value
                binding.executePendingBindings()
            }

        private fun showPopup(v: View) {
            PopupMenu(context, v).apply {
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.edit -> item?.let { item -> callback.onClickEdit(item) }
                        R.id.create_invite -> item?.let { item -> callback.onClickCreateInvite(item) }
                        R.id.create_invite_readonly -> item?.let { item ->
                            callback.onClickCreateReadonlyInvite(item)
                        }
                        R.id.permission_manage -> item?.let { item ->
                            callback.onClickPermissionManage(item)
                        }
                        R.id.remove -> item?.let { item -> callback.onClickRemove(item) }
                        else -> return@setOnMenuItemClickListener false
                    }
                    return@setOnMenuItemClickListener true
                }

                menuInflater.inflate(R.menu.note_options, menu)

                menu.findItem(R.id.edit).isVisible = item?.isReadonly == false
                menu.findItem(R.id.create_invite).isVisible = item?.isOwner == true
                menu.findItem(R.id.create_invite_readonly).isVisible = item?.isOwner == true
                menu.findItem(R.id.permission_manage).isVisible = item?.isOwner == true

                show()
            }
        }
    }
}