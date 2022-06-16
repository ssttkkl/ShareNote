package me.ssttkkl.sharenote.ui.notepermission

import me.ssttkkl.sharenote.ui.utils.ItemModel

data class PermissionItemModel(
    override val id: Int,
    val user: String,
    val readonly: Boolean
) : ItemModel<Int>