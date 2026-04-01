package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val error: Boolean = false,
    val errorMessage: String? = null,
    val empty: Boolean = false,
)
