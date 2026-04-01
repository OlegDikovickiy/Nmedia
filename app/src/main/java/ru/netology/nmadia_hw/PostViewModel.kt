package ru.netology.nmadia_hw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import ru.netology.nmadia_hw.dto.Post
import ru.netology.nmadia_hw.error.AppError
import ru.netology.nmadia_hw.repository.PostRepository
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(Unit)

    val data: Flow<PagingData<Post>> = refreshTrigger
        .flatMapLatest { repository.data() }
        .cachedIn(viewModelScope)

    private val _newerCount = MutableStateFlow(0)
    val newerCount: StateFlow<Int> = _newerCount.asStateFlow()

    private var latestKnownId: Long? = null

    init {
        viewModelScope.launch {
            try {
                val latest = repository.getLatestOnce(1)
                latestKnownId = latest.firstOrNull()?.id
            } catch (_: AppError) {
            }
        }
    }

    fun checkNewer() {
        val currentId = latestKnownId ?: return
        viewModelScope.launch {
            try {
                val newer = repository.getNewer(currentId)
                _newerCount.value = newer.size
            } catch (_: AppError) {
            }
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            try {
                val latest = repository.getLatestOnce(1)
                latestKnownId = latest.firstOrNull()?.id
            } catch (_: AppError) {
            }
            _newerCount.value = 0
            refreshTrigger.value = Unit
        }
    }

    fun showEmptyShareError() = Unit
}