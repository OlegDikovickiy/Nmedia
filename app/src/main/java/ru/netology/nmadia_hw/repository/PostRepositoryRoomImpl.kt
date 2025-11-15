package ru.netology.nmadia_hw.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmadia_hw.dao.PostDao
import ru.netology.nmadia_hw.dto.Post
import ru.netology.nmadia_hw.entity.PostEntity
import kotlin.concurrent.thread

class PostRepositoryRoomImpl(
    private val dao: PostDao
) : PostRepository {

    override fun getAll(): LiveData<List<Post>> =
        dao.getAll().map { entities ->
            entities.map(PostEntity::toDto)
        }

    override fun likeById(id: Long) {
        thread {
            dao.likeById(id)
        }
    }

    override fun shareById(id: Long) {
        thread {
            dao.shareById(id)
        }
    }

    override fun removeById(id: Long) {
        thread {
            dao.removeById(id)
        }
    }

    override fun save(post: Post) {
        thread {
            if (post.id == 0L) {
                val entity = PostEntity.fromDto(
                    post.copy(
                        author = if (post.author.isBlank()) "Me" else post.author,
                        published = if (post.published.isBlank()) "now" else post.published
                    )
                )
                dao.insert(entity)
            } else {
                dao.updateContentById(post.id, post.content)
            }
        }
    }
}
