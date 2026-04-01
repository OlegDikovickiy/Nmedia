package ru.netology.nmedia.entity

import ru.netology.nmedia.dto.Post

fun PostEntity.toDto(): Post = Post(
    id = id,
    author = author,
    authorAvatar = authorAvatar,
    content = content,
    published = published,
    likes = likes,
    likedByMe = likedByMe,
    shares = shares,
    views = views,
    video = video,
    attachment = null,
    pending = pending,
    pendingError = pendingError,
)

fun Post.toEntity(
    pending: Boolean = false,
    pendingError: Boolean = false,
): PostEntity = PostEntity(
    id = id,
    author = author,
    authorAvatar = authorAvatar,
    published = published,
    content = content,
    likedByMe = likedByMe,
    likes = likes,
    shares = shares,
    views = views,
    video = video,
    pending = pending,
    pendingError = pendingError,
)

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)

fun List<Post>.toEntity(
    pending: Boolean = false,
    pendingError: Boolean = false,
): List<PostEntity> = map { it.toEntity(pending = pending, pendingError = pendingError) }
