package me.ssttkkl.sharenote.data.payload

data class Sort(val by: String, val order: Order, val next: Sort? = null) {
    override fun toString(): String {
        return "$by,$order" + if (next != null) ",$next" else ""
    }

    companion object {
        val idAsc = Sort("id", Order.ASC)
    }
}

enum class Order {
    ASC, DESC
}