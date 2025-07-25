package com.example.studyblocks.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyblocks.data.local.dao.UserDao
import com.example.studyblocks.data.model.User
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    
    private val _isFirstTimeLogin = MutableStateFlow(false)
    val isFirstTimeLogin: StateFlow<Boolean> = _isFirstTimeLogin.asStateFlow()
    
    init {
        println("DEBUG AuthViewModel: ViewModel initialized - Instance: ${this.hashCode()}")
        println("DEBUG AuthViewModel: Initial _authState = ${_authState.value}")
        println("DEBUG AuthViewModel: Initial _isFirstTimeLogin = ${_isFirstTimeLogin.value}")
        
        // Monitor state changes
        viewModelScope.launch {
            _authState.collect { state ->
                println("DEBUG AuthViewModel: _authState emitted: $state")
            }
        }
        
        viewModelScope.launch {
            _isFirstTimeLogin.collect { isFirstTime ->
                println("DEBUG AuthViewModel: _isFirstTimeLogin emitted: $isFirstTime")
            }
        }
        
        checkAuthState()
    }
    
    private fun checkAuthState() {
        val currentUser = authRepository.currentUser
        println("DEBUG: checkAuthState - currentUser = $currentUser")
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(currentUser)
            println("DEBUG: checkAuthState - setting AuthState.Authenticated")
            viewModelScope.launch {
                syncUserToDatabase(currentUser)
            }
        } else {
            _authState.value = AuthState.Unauthenticated
            println("DEBUG: checkAuthState - setting AuthState.Unauthenticated")
        }
    }
    
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            println("DEBUG: signInWithEmail - starting sign in - ViewModel instance: ${this@AuthViewModel.hashCode()}")
            when (val result = authRepository.signInWithEmail(email, password)) {
                is AuthResult.Success -> {
                    println("DEBUG: signInWithEmail - success")
                    result.user?.let { user ->
                        _authState.value = AuthState.Authenticated(user)
                        println("DEBUG: signInWithEmail - setting AuthState.Authenticated")
                        println("DEBUG: signInWithEmail - _authState.value = ${_authState.value}")
                        syncUserToDatabase(user)
                        println("DEBUG: signInWithEmail - after syncUserToDatabase, _isFirstTimeLogin.value = ${_isFirstTimeLogin.value}")
                    }
                }
                is AuthResult.Error -> {
                    println("DEBUG: signInWithEmail - error: ${result.message}")
                    _authState.value = AuthState.Error(result.message)
                }
            }
            _isLoading.value = false
            println("DEBUG: signInWithEmail - finished, _isLoading = false")
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
        
        // Check if this is a first-time user
        val existingUser = userDao.getUserById(firebaseUser.uid)
        val isFirstTime = existingUser == null
        val hasCompletedOnboarding = existingUser?.hasCompletedOnboarding ?: false
        
        println("DEBUG: syncUserToDatabase - existingUser = $existingUser")
        println("DEBUG: syncUserToDatabase - isFirstTime = $isFirstTime")
        println("DEBUG: syncUserToDatabase - hasCompletedOnboarding = $hasCompletedOnboarding")
        
        // Set first time login based on onboarding completion status
        // For new users or users who haven't completed onboarding, they need onboarding
        val needsOnboarding = !hasCompletedOnboarding
        
        println("DEBUG: syncUserToDatabase - needsOnboarding = $needsOnboarding")
        println("DEBUG: syncUserToDatabase - before setting _isFirstTimeLogin, current value = ${_isFirstTimeLogin.value}")
        
        // Ensure state update happens on main thread
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            _isFirstTimeLogin.value = needsOnboarding
        }
        
        println("DEBUG: syncUserToDatabase - after setting _isFirstTimeLogin to $needsOnboarding, actual value = ${_isFirstTimeLogin.value}")
        
        val user = User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = displayName,
            profilePictureUrl = firebaseUser.photoUrl?.toString(),
            isSignedIn = true,
            hasCompletedOnboarding = hasCompletedOnboarding,
            // Preserve other existing user data if user exists
            globalXp = existingUser?.globalXp ?: 0,
            globalLevel = existingUser?.globalLevel ?: 1,
            defaultBlockDuration = existingUser?.defaultBlockDuration ?: 25,
            preferredBlocksPerDay = existingUser?.preferredBlocksPerDay ?: 3,
            createdAt = existingUser?.createdAt ?: java.time.LocalDateTime.now()
        )
        userDao.signOutAllUsers() // Sign out other users first
        userDao.insertUser(user)
        
        println("DEBUG: syncUserToDatabase - created user: $user")
        
        // Add small delay to ensure state propagation
        kotlinx.coroutines.delay(100)
        println("DEBUG: syncUserToDatabase - completed with delay, _isFirstTimeLogin.value = ${_isFirstTimeLogin.value}")
    }
    
    fun markOnboardingCompleted() {
        viewModelScope.launch {
            try {
                println("DEBUG AuthViewModel: markOnboardingCompleted called")
                
                // First, update the state flow immediately
                _isFirstTimeLogin.value = false
                println("DEBUG AuthViewModel: Set _isFirstTimeLogin to false")
                
                // Then update user in database for persistence
                val currentUser = userDao.getCurrentUser()
                println("DEBUG AuthViewModel: Current user from database: $currentUser")
                currentUser?.let { user ->
                    val updatedUser = user.copy(hasCompletedOnboarding = true)
                    userDao.insertUser(updatedUser)
                    println("DEBUG AuthViewModel: Updated user in database: $updatedUser")
                    
                    // Verify the update worked
                    val verifyUser = userDao.getCurrentUser()
                    println("DEBUG AuthViewModel: Verified user after update: $verifyUser")
                }
                
                println("DEBUG AuthViewModel: Onboarding marked as completed - isFirstTimeLogin = false")
            } catch (e: Exception) {
                // Log error but don't revert state change
                println("DEBUG AuthViewModel: Failed to update user onboarding status: ${e.message}")
                e.printStackTrace()
                // Still ensure the state is updated to prevent UI issues
                _isFirstTimeLogin.value = false
            }
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
    object PasswordResetSent : AuthState()
}