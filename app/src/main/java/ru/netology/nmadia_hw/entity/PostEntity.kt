package ru.netology.nmadia_hw.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: Long,
    val author: String,
    val authorAvatar: String? = null,
    val published: String,
    val content: String,
    val likedByMe: Boolean,
    val likes: Int,
    val shares: Int,
    val views: Int,
    val video: String? = null,

    // статус синхронизации
    val pending: Boolean = false,        // true = ещё не сохранён на сервере
    val pendingError: Boolean = false,   // true = последняя попытка save в API упала
)
