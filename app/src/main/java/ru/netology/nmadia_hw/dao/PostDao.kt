package ru.netology.nmadia_hw.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmadia_hw.entity.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("""
        UPDATE posts SET
            likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END,
            likes = CASE WHEN likedByMe THEN likes - 1 ELSE likes + 1 END
        WHERE id = :id
    """)
    suspend fun likeById(id: Long)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("UPDATE posts SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    @Query("SELECT likedByMe FROM posts WHERE id = :id")
    suspend fun getLikedByMe(id: Long): Boolean

    @Query("SELECT pending FROM posts WHERE id = :id")
    suspend fun isPending(id: Long): Boolean

    @Query("SELECT * FROM posts WHERE pending = 1 ORDER BY id DESC")
    suspend fun getPending(): List<PostEntity>

    @Query("UPDATE posts SET pending = :pending, pendingError = :pendingError WHERE id = :id")
    suspend fun setPendingState(id: Long, pending: Boolean, pendingError: Boolean)

    @Query("UPDATE posts SET pendingError = :pendingError WHERE id = :id")
    suspend fun setPendingError(id: Long, pendingError: Boolean)

    // замена временного id на серверный (и одновременно сброс pending-флагов)
    @Query("""
        UPDATE posts SET
            id = :serverId,
            pending = 0,
            pendingError = 0
        WHERE id = :localId
    """)
    suspend fun replaceId(localId: Long, serverId: Long)
}