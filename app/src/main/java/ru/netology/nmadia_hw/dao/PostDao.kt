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

    @Query("UPDATE posts SET shares = shares + 1 WHERE id = :id")
    suspend fun shareById(id: Long)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("UPDATE posts SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    @Query("SELECT likedByMe FROM posts WHERE id = :id")
    suspend fun getLikedByMe(id: Long): Boolean
}
