package me.ssttkkl.sharenote.ui.noteedit

import me.ssttkkl.sharenote.ui.utils.ItemModel


sealed class TagItemModel<T : TagItemModel<T>> : ItemModel<String> {

    data class Tag(
        val name: String = "",
        val showRemove: Boolean = false,
    ) : TagItemModel<Tag>() {
        override val id: String
            get() = name
    }

    object AddTag : TagItemModel<AddTag>() {
        override val id: String
            get() = ""
    }
}