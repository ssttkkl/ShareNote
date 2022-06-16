package me.ssttkkl.sharenote.ui.home

import me.ssttkkl.sharenote.ui.utils.ItemModel

data class NoteItemModel(
    override val id: Int,
    val title: String,
    val content: String,
    val modifiedAt: String,
    val isOwner: Boolean,
    val isReadonly: Boolean,
    val isDetached: Boolean,
) : ItemModel<Int>