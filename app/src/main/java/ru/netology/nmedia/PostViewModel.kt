package ru.netology.nmedia

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Post(
    id = 0,
    author = "",
    authorAvatar = null,
    content = "",
    published = "",
    likes = 0,
    likedByMe = false,
    shares = 0,
    views = 0,
    video = null,
    attachment = null,
    pending = false,
    pendingError = false,
)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
) : ViewModel() {

    val data: LiveData<List<Post>> = repository.data

    private val _dataState = MutableLiveData(FeedModel())
    val dataState: LiveData<FeedModel> = _dataState

    val edited = MutableLiveData(empty)

    private val _isEditing = MutableLiveData(false)
    val isEditing: LiveData<Boolean> = _isEditing

    val emptyShareErrorEvent = SingleLiveEvent<Unit>()

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModel(loading = true, error = false, errorMessage = null)
            repository.retryPendingSaves()
            repository.getAll()
            _dataState.value = FeedModel()
        } catch (e: AppError) {
            _dataState.value = FeedModel(error = true, errorMessage = e.message)
        }
    }

    fun retry() = loadPosts()

    fun retryPending() = viewModelScope.launch {
        try {
            _dataState.value = _dataState.value?.copy(error = false, errorMessage = null) ?: FeedModel()
            repository.retryPendingSaves()
        } catch (e: AppError) {
            _dataState.value = FeedModel(error = true, errorMessage = e.message)
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            _dataState.value = FeedModel(refreshing = true, error = false, errorMessage = null)
            repository.retryPendingSaves()
            repository.refresh()
            _dataState.value = FeedModel()
        } catch (e: AppError) {
            _dataState.value = FeedModel(error = true, errorMessage = e.message)
        }
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            repository.likeById(id)
        } catch (e: AppError) {
            _dataState.value = FeedModel(error = true, errorMessage = e.message)
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (e: AppError) {
            _dataState.value = FeedModel(error = true, errorMessage = e.message)
        }
    }

    fun like(id: Long) = likeById(id)
    fun remove(id: Long) = removeById(id)

    fun share(id: Long) = viewModelScope.launch {
        try {
            repository.shareById(id)
        } catch (e: AppError) {
            _dataState.value = FeedModel(error = true, errorMessage = e.message)
        }
    }

    fun save(text: String) {
        edited.value?.let {
            val content = text.trim()
            if (content.isNotBlank() && content != it.content) {
                val draft = it.copy(content = content)
                viewModelScope.launch {
                    try {
                        repository.save(draft)
                    } catch (e: AppError) {
                        _dataState.value = FeedModel(error = true, errorMessage = e.message)
                    }
                }
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
        emptyShareErrorEvent.value = Unit
    }
}