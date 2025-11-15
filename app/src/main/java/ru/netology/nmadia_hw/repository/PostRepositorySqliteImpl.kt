package ru.netology.nmadia_hw.repository

import android.content.ContentValues
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmadia_hw.db.PostDbHelper
import ru.netology.nmadia_hw.db.PostsTable
import ru.netology.nmadia_hw.dto.Post
import kotlin.concurrent.thread

class PostRepositorySqliteImpl(
    context: Context
) : PostRepository {

    private val dbHelper = PostDbHelper(context)
    private val data = MutableLiveData<List<Post>>(emptyList())

    init {
        loadPosts()
    }

    override fun getAll(): LiveData<List<Post>> = data

    private fun loadPosts() {
        thread {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                PostsTable.TABLE,
                null,
                null,
                null,
                null,
                null,
                "${PostsTable.COLUMN_ID} DESC"
            )

            val posts = mutableListOf<Post>()

            cursor.use {
                val idIndex = cursor.getColumnIndexOrThrow(PostsTable.COLUMN_ID)
                val authorIndex = cursor.getColumnIndexOrThrow(PostsTable.COLUMN_AUTHOR)
                val publishedIndex = cursor.getColumnIndexOrThrow(PostsTable.COLUMN_PUBLISHED)
                val contentIndex = cursor.getColumnIndexOrThrow(PostsTable.COLUMN_CONTENT)
                val likedIndex = cursor.getColumnIndexOrThrow(PostsTable.COLUMN_LIKED_BY_ME)
                val likesIndex = cursor.getColumnIndexOrThrow(PostsTable.COLUMN_LIKES)
                val sharesIndex = cursor.getColumnIndexOrThrow(PostsTable.COLUMN_SHARES)
                val viewsIndex = cursor.getColumnIndexOrThrow(PostsTable.COLUMN_VIEWS)
                val videoIndex = cursor.getColumnIndexOrThrow(PostsTable.COLUMN_VIDEO)

                while (cursor.moveToNext()) {
                    posts += Post(
                        id = cursor.getLong(idIndex),
                        author = cursor.getString(authorIndex),
                        published = cursor.getString(publishedIndex),
                        content = cursor.getString(contentIndex),
                        likedByMe = cursor.getInt(likedIndex) != 0,
                        likes = cursor.getInt(likesIndex),
                        shares = cursor.getInt(sharesIndex),
                        views = cursor.getInt(viewsIndex),
                        video = cursor.getString(videoIndex),
                    )
                }
            }

            data.postValue(posts)
        }
    }

    override fun likeById(id: Long) {
        thread {
            val db = dbHelper.writableDatabase

            // сначала читаем текущие значения
            val cursor = db.query(
                PostsTable.TABLE,
                arrayOf(
                    PostsTable.COLUMN_LIKED_BY_ME,
                    PostsTable.COLUMN_LIKES,
                ),
                "${PostsTable.COLUMN_ID} = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
            )

            var likedByMe = false
            var likes = 0

            cursor.use {
                if (cursor.moveToFirst()) {
                    likedByMe = cursor.getInt(cursor.getColumnIndexOrThrow(PostsTable.COLUMN_LIKED_BY_ME)) != 0
                    likes = cursor.getInt(cursor.getColumnIndexOrThrow(PostsTable.COLUMN_LIKES))
                } else return@thread
            }

            val newLiked = !likedByMe
            val newLikes = if (newLiked) likes + 1 else likes - 1

            val values = ContentValues().apply {
                put(PostsTable.COLUMN_LIKED_BY_ME, if (newLiked) 1 else 0)
                put(PostsTable.COLUMN_LIKES, newLikes)
            }

            db.update(
                PostsTable.TABLE,
                values,
                "${PostsTable.COLUMN_ID} = ?",
                arrayOf(id.toString())
            )

            // перечитываем список
            loadPosts()
        }
    }

    override fun shareById(id: Long) {
        thread {
            val db = dbHelper.writableDatabase

            db.execSQL(
                """
                UPDATE ${PostsTable.TABLE}
                SET ${PostsTable.COLUMN_SHARES} = ${PostsTable.COLUMN_SHARES} + 1
                WHERE ${PostsTable.COLUMN_ID} = ?
                """.trimIndent(),
                arrayOf(id)
            )

            loadPosts()
        }
    }

    override fun removeById(id: Long) {
        thread {
            val db = dbHelper.writableDatabase
            db.delete(
                PostsTable.TABLE,
                "${PostsTable.COLUMN_ID} = ?",
                arrayOf(id.toString())
            )
            loadPosts()
        }
    }

    override fun save(post: Post) {
        thread {
            val db = dbHelper.writableDatabase

            if (post.id == 0L) {
                // новый пост
                val values = ContentValues().apply {
                    put(PostsTable.COLUMN_AUTHOR, if (post.author.isBlank()) "Me" else post.author)
                    put(PostsTable.COLUMN_PUBLISHED, if (post.published.isBlank()) "now" else post.published)
                    put(PostsTable.COLUMN_CONTENT, post.content)
                    put(PostsTable.COLUMN_LIKED_BY_ME, if (post.likedByMe) 1 else 0)
                    put(PostsTable.COLUMN_LIKES, post.likes)
                    put(PostsTable.COLUMN_SHARES, post.shares)
                    put(PostsTable.COLUMN_VIEWS, post.views)
                    put(PostsTable.COLUMN_VIDEO, post.video)
                }

                db.insert(PostsTable.TABLE, null, values)
            } else {
                // редактирование содержимого
                val values = ContentValues().apply {
                    put(PostsTable.COLUMN_CONTENT, post.content)
                }

                db.update(
                    PostsTable.TABLE,
                    values,
                    "${PostsTable.COLUMN_ID} = ?",
                    arrayOf(post.id.toString())
                )
            }

            loadPosts()
        }
    }
}
