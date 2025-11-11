package ru.netology.nmadia_hw.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import ru.netology.nmadia_hw.dto.Post
import java.io.File
import java.io.IOException

class PostRepositoryFileImpl(private val context: Context) : PostRepository {

    private val gson = Gson()
    private val file: File by lazy { File(context.filesDir, FILENAME) }

    private var nextId: Long = 1L
    private var posts: List<Post> = emptyList()

    private val data = MutableLiveData<List<Post>>()

    companion object {
        private const val FILENAME = "posts.json"
    }

    init {
        posts = readFromDiskOrInit()
        nextId = (posts.maxOfOrNull { it.id } ?: 0L) + 1L
        data.value = posts
    }

    override fun getAll(): LiveData<List<Post>> = data

    override fun likeById(id: Long) {
        posts = posts.map { post ->
            if (post.id != id) post
            else {
                val newLiked = !post.likedByMe
                val newLikes = if (newLiked) post.likes + 1 else post.likes - 1
                post.copy(likedByMe = newLiked, likes = newLikes)
            }
        }
        persistAndDispatch()
    }

    override fun shareById(id: Long) {
        posts = posts.map { post ->
            if (post.id != id) post else post.copy(shares = post.shares + 1)
        }
        persistAndDispatch()
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        persistAndDispatch()
    }

    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            val newPost = post.copy(
                id = nextId++,
                author = if (post.author.isBlank()) "Me" else post.author,
                published = if (post.published.isBlank()) "now" else post.published
            )
            listOf(newPost) + posts
        } else {
            posts.map { if (it.id != post.id) it else it.copy(content = post.content) }
        }
        persistAndDispatch()
    }

    private fun readFromDiskOrInit(): List<Post> {
        if (!file.exists()) {
            val initial = defaultSeed()
            writeSafe(initial)
            return initial
        }
        return readSafe()
    }

    private fun persistAndDispatch() {
        writeSafe(posts)
        data.value = posts.toList()
    }

    private fun writeSafe(list: List<Post>) {
        try {
            file.writeText(gson.toJson(list))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun readSafe(): List<Post> {
        return try {
            val json = file.readText()
            val type = object : TypeToken<List<Post>>() {}.type
            gson.fromJson<List<Post>>(json, type) ?: emptyList()
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun defaultSeed(): List<Post> {
        val seed = listOf(
            Post(
                id = 1,
                author = "Нетология",
                content = "Привет, это новая Нетология! …",
                published = "21 мая в 18:36",
                likedByMe = false,
                likes = 10,
                shares = 0,
                views = 0,
                video = null
            ),
            Post(
                id = 2,
                author = "Demo",
                content = "Пост с видео!",
                published = "Сегодня",
                likedByMe = false,
                likes = 1,
                shares = 0,
                views = 0,
                video = "https://rutube.ru/video/6550a91e7e523f9503bed47e4c46d0cb"
            )
        )
        return seed.reversed()
    }
}
