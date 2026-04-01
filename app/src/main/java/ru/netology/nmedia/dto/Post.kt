package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String? = null,
    val content: String,
    val published: String,
    val likes: Int,
    val likedByMe: Boolean,
    val shares: Int,
    val views: Int,
    val video: String? = null,
    val attachment: Attachment? = null,

    // статус локального save
    val pending: Boolean = false,
    val pendingError: Boolean = false,
)
