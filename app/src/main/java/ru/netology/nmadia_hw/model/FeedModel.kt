package ru.netology.nmadia_hw.model

import ru.netology.nmadia_hw.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val error: Boolean = false,
    val empty: Boolean = false,
)
