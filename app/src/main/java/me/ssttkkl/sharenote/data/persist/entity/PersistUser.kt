package me.ssttkkl.sharenote.data.persist.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.ssttkkl.sharenote.data.entity.User

@Entity(tableName = "user")
data class PersistUser(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val id: Int,
    val username: String,
    val nickname: String,
)

fun PersistUser.toUser() = User(
    this.id,
    this.username,
    this.nickname,
)

fun User.toPersistUser() = PersistUser(
    this.id,
    this.username,
    this.nickname,
)