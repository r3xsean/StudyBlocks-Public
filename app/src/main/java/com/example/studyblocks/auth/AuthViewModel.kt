package com.example.studyblocks.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyblocks.data.local.dao.UserDao
import com.example.studyblocks.data.model.User
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userDao: UserDao
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        val currentUser = authRepository.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(currentUser)
            viewModelScope.launch {
                syncUserToDatabase(currentUser)
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.signInWithEmail(email, password)) {
                is AuthResult.Success -> {
                    result.user?.let { user ->
                        _authState.value = AuthState.Authenticated(user)
                        syncUserToDatabase(user)
                    }
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
            _isLoading.value = false
        }
    }
    
    fun signUpWithEmail(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.signUpWithEmail(email, password)) {
                is AuthResult.Success -> {
                    result.user?.let { user ->
                        _authState.value = AuthState.Authenticated(user)
                        syncUserToDatabase(user, displayName)
                    }
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
            _isLoading.value = false
        }
    }
    
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is AuthResult.Success -> {
                    result.user?.let { user ->
                        _authState.value = AuthState.Authenticated(user)
                        syncUserToDatabase(user)
                    }
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
            _isLoading.value = false
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            userDao.signOutAllUsers()
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is AuthResult.Success -> {
                    _authState.value = AuthState.PasswordResetSent
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            checkAuthState()
        }
    }
    
    private suspend fun syncUserToDatabase(firebaseUser: FirebaseUser, customDisplayName: String? = null) {
        val displayName = when {
            !customDisplayName.isNullOrBlank() -> customDisplayName
            !firebaseUser.displayName.isNullOrBlank() -> firebaseUser.displayName!!
            !firebaseUser.email.isNullOrBlank() -> firebaseUser.email!!.substringBefore("@")
            else -> "User"
        }
        
        val user = User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = displayName,
            profilePictureUrl = firebaseUser.photoUrl?.toString(),
            isSignedIn = true
        )
        userDao.signOutAllUsers() // Sign out other users first
        userDao.insertUser(user)
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
    object PasswordResetSent : AuthState()
}