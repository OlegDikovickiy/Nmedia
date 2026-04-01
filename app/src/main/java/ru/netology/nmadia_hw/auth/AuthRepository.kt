package ru.netology.nmadia_hw.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor() {

    private val _authState = MutableStateFlow(false)
    val authState: StateFlow<Boolean> = _authState

    fun login() {
        _authState.value = true
    }

    fun logout() {
        _authState.value = false
    }
}