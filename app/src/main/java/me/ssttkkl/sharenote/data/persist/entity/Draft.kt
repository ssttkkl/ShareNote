package me.ssttkkl.sharenote.data.persist.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(tableName = "draft")
data class Draft(
    @PrimaryKey
    @ColumnInfo(name = "draft_id")
    val id: Int,
    val title: String,
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
    val content: String,
    @ColumnInfo(name = "created_at")
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
)