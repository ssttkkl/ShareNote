package me.ssttkkl.sharenote.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class Page<T>(
    val content: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalElements: Int
) {

    val isLastPage
        get() = page + 1 >= totalPages
}