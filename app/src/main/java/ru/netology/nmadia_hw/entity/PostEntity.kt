package ru.netology.nmadia_hw.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmadia_hw.dto.Post

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val author: String,
    val published: String,
    val content: String,
    val likedByMe: Boolean,
    val likes: Int,
    val shares: Int,
    val views: Int,
    val video: String? = null,
) {
    fun toDto() = Post(
        id = id,
        author = author,
        published = published,
        content = content,
        likedByMe = likedByMe,
        likes = likes,
        shares = shares,
        views = views,
        video = video,
    )

    companion object {
        fun fromDto(dto: Post) = PostEntity(
            id = dto.id,
            author = dto.author,
            published = dto.published,
            content = dto.content,
            likedByMe = dto.likedByMe,
            likes = dto.likes,
            shares = dto.shares,
            views = dto.views,
            video = dto.video,
        )
    }
}
