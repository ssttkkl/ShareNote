package me.ssttkkl.sharenote.ui.notedetail

data class NoteViewState(
    val title: String = "",
    val content: String = "",
    val tags: String = "",
    val createdAt: String = "",
    val modifiedAt: String = "",
    val owner: String = "",
    val modifiedBy: String = "",
    val permission: String = "",
    val isOwner: Boolean = true,
    val isReadonly: Boolean = false,
    val isDetached: Boolean = false,
)