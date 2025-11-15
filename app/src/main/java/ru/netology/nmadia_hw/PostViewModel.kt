package ru.netology.nmadia_hw

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmadia_hw.dto.Post
import ru.netology.nmadia_hw.repository.PostRepository
import ru.netology.nmadia_hw.repository.PostRepositorySqliteImpl

private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = "",
    likes = 0,
    likedByMe = false,
    shares = 0,
    views = 0,
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositorySqliteImpl(application)

    val data: LiveData<List<Post>> = repository.getAll()
    val edited = MutableLiveData(empty)

    private val _isEditing = MutableLiveData(false)
    val isEditing: LiveData<Boolean> = _isEditing

    private val _emptyShareError = MutableLiveData(false)
    val emptyShareError: LiveData<Boolean> = _emptyShareError

    fun like(id: Long) = repository.likeById(id)
    fun share(id: Long) = repository.shareById(id)
    fun remove(id: Long) = repository.removeById(id)

    fun save(text: String) {
        edited.value?.let {
            val content = text.trim()
            if (content.isNotBlank() && content != it.content) {
                repository.save(it.copy(content = content))
            }
        }
        edited.value = empty
        _isEditing.value = false
    }

    fun edit(post: Post) {
        edited.value = post
        _isEditing.value = true
    }

    fun cancelEdit() {
        edited.value = empty
        _isEditing.value = false
    }

    fun showEmptyShareError() {
        _emptyShareError.value = true
    }

    fun clearEmptyShareError() {
        _emptyShareError.value = false
    }
}
