package me.ssttkkl.sharenote.data.payload

data class Pageable(
    val page: Int,
    val size: Int = 20,
) {
    companion object {
        val default = Pageable(0, 20)
    }
}