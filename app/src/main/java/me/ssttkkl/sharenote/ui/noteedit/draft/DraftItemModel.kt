package me.ssttkkl.sharenote.ui.noteedit.draft

import me.ssttkkl.sharenote.data.persist.entity.Draft
import me.ssttkkl.sharenote.ui.utils.ItemModel
import me.ssttkkl.sharenote.ui.utils.toDisplayText

class DraftItemModel(
    val draft: Draft
) : ItemModel<Int> {

    override val id: Int
        get() = draft.id

    val title get() = draft.title
    val content get() = draft.content
    val createdAt get() = draft.createdAt

    val createdAtText: String
        get() = createdAt.toDisplayText()
}