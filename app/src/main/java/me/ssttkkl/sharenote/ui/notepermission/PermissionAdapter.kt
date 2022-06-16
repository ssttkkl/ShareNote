package me.ssttkkl.sharenote.ui.notepermission

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import me.ssttkkl.sharenote.R
import me.ssttkkl.sharenote.databinding.ItemNotePermissionBinding
import me.ssttkkl.sharenote.ui.utils.itemModelDiffer

class PermissionAdapter(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val callback: Callback
) : PagingDataAdapter<PermissionItemModel, PermissionAdapter.ViewHolder>(
    itemModelDiffer()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotePermissionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = getItem(position)
    }

    interface Callback {
        fun onClickSetReadonly(item: PermissionItemModel)
        fun onClickSetReadwrite(item: PermissionItemModel)
        fun onClickRemove(item: PermissionItemModel)
    }

    inner class ViewHolder(
        private val binding: ItemNotePermissionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.lifecycleOwner = lifecycleOwner
            binding.root.setOnClickListener { view -> item?.let { showPopup(view, it) } }
        }

        var item: PermissionItemModel?
            get() = binding.permission
            set(value) {
                binding.permission = value
                binding.executePendingBindings()
            }

        private fun showPopup(v: View, item: PermissionItemModel) {
            PopupMenu(context, v).apply {
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.set_readonly -> callback.onClickSetReadonly(item)
                        R.id.set_readwrite -> callback.onClickSetReadwrite(item)
                        R.id.remove -> callback.onClickRemove(item)
                        else -> return@setOnMenuItemClickListener false
                    }
                    return@setOnMenuItemClickListener true
                }

                menuInflater.inflate(R.menu.note_permission_options, menu)

                menu.findItem(R.id.set_readonly).isVisible = !item.readonly
                menu.findItem(R.id.set_readwrite).isVisible = item.readonly

                show()
            }
        }
    }
}