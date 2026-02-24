package ru.netology.nmadia_hw.entity

import ru.netology.nmadia_hw.dto.Post

fun PostEntity.toDto() = Post(
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
)

fun Post.toEntity() = PostEntity(
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
)

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(Post::toEntity)
