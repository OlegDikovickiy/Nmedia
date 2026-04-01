package ru.netology.nmadia_hw.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    val authState = repository.authState

    fun login() {
        repository.login()
    }

    fun logout() {
        repository.logout()
    }
}