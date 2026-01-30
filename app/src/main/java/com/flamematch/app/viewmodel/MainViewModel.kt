package com.flamematch.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flamematch.app.data.model.*
import com.flamematch.app.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val firebaseRepo = FirebaseRepository()
    private val cloudinaryRepo = CloudinaryRepository()
    private val locationRepo = LocationRepository(application)
    
    // Auth State
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // Current User
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    // Discover Users
    private val _discoverUsers = MutableStateFlow<List<User>>(emptyList())
    val discoverUsers: StateFlow<List<User>> = _discoverUsers.asStateFlow()
    
    // Current discover index
    private val _currentDiscoverIndex = MutableStateFlow(0)
    val currentDiscoverIndex: StateFlow<Int> = _currentDiscoverIndex.asStateFlow()
    
    // Matches
    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()
    
    // Likes Received
    private val _likesReceived = MutableStateFlow<List<Like>>(emptyList())
    val likesReceived: StateFlow<List<Like>> = _likesReceived.asStateFlow()
    
    // Messages for current chat
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Match popup
    private val _showMatchPopup = MutableStateFlow<User?>(null)
    val showMatchPopup: StateFlow<User?> = _showMatchPopup.asStateFlow()
    
    // Error messages
    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()
    
    // Success messages
    private val _success = MutableSharedFlow<String>()
    val success: SharedFlow<String> = _success.asSharedFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            if (firebaseRepo.isLoggedIn) {
                loadCurrentUser()
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.NotAuthenticated
            }
        }
    }
    
    // ==================== AUTH ====================
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            firebaseRepo.signInWithEmail(email, password)
                .onSuccess {
                    loadCurrentUser()
                    _authState.value = AuthState.Authenticated
                }
                .onFailure {
                    _error.emit(it.message ?: "Login failed")
                }
            _isLoading.value = false
        }
    }
    
    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            firebaseRepo.signUpWithEmail(email, password, name)
                .onSuccess {
                    loadCurrentUser()
                    _authState.value = AuthState.Authenticated
                }
                .onFailure {
                    _error.emit(it.message ?: "Registration failed")
                }
            _isLoading.value = false
        }
    }
    
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            firebaseRepo.signInWithGoogle(idToken)
                .onSuccess {
                    loadCurrentUser()
                    _authState.value = AuthState.Authenticated
                }
                .onFailure {
                    _error.emit(it.message ?: "Google sign in failed")
                }
            _isLoading.value = false
        }
    }
    
    fun signOut() {
        firebaseRepo.signOut()
        _currentUser.value = null
        _authState.value = AuthState.NotAuthenticated
    }
    
    // ==================== USER PROFILE ====================
    
    private suspend fun loadCurrentUser() {
        _currentUser.value = firebaseRepo.getCurrentUser()
    }
    
    fun updateProfile(updates: Map<String, Any>) {
        viewModelScope.launch {
            _isLoading.value = true
            firebaseRepo.updateUser(updates)
                .onSuccess {
                    loadCurrentUser()
                    _success.emit("Profile updated!")
                }
                .onFailure {
                    _error.emit(it.message ?: "Update failed")
                }
            _isLoading.value = false
        }
    }
    
    fun uploadPhoto(uri: Uri, isProfilePhoto: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            cloudinaryRepo.uploadImage(getApplication(), uri)
                .onSuccess { url ->
                    val currentPhotos = _currentUser.value?.photos?.toMutableList() ?: mutableListOf()
                    currentPhotos.add(url)
                    
                    val updates = mutableMapOf<String, Any>("photos" to currentPhotos)
                    if (isProfilePhoto || currentPhotos.size == 1) {
                        updates["profilePhoto"] = url
                    }
                    
                    updateProfile(updates)
                }
                .onFailure {
                    _error.emit("Photo upload failed: ${it.message}")
                }
            _isLoading.value = false
        }
    }
    
    fun uploadVoiceVibe(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            cloudinaryRepo.uploadVoice(getApplication(), uri)
                .onSuccess { url ->
                    updateProfile(mapOf("voiceVibe" to url))
                }
                .onFailure {
                    _error.emit("Voice upload failed: ${it.message}")
                }
            _isLoading.value = false
        }
    }
    
    // ==================== LOCATION ====================
    
    fun updateLocation() {
        viewModelScope.launch {
            locationRepo.getCurrentLocation()
                .onSuccess { location ->
                    firebaseRepo.updateLocation(
                        location.latitude,
                        location.longitude,
                        location.city,
                        location.country
                    )
                    loadCurrentUser()
                }
                .onFailure {
                    _error.emit("Could not get location: ${it.message}")
                }
        }
    }
    
    // ==================== DISCOVER ====================
    
    fun loadDiscoverUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            val users = firebaseRepo.getDiscoverUsers()
            _discoverUsers.value = users
            _currentDiscoverIndex.value = 0
            _isLoading.value = false
        }
    }
    
    fun likeCurrentUser(isSuperLike: Boolean = false) {
        viewModelScope.launch {
            val users = _discoverUsers.value
            val index = _currentDiscoverIndex.value
            if (index >= users.size) return@launch
            
            val targetUser = users[index]
            
            firebaseRepo.likeUser(targetUser.id, isSuperLike)
                .onSuccess { isMatch ->
                    if (isMatch) {
                        _showMatchPopup.value = targetUser
                        loadMatches()
                    }
                    nextUser()
                }
                .onFailure {
                    _error.emit(it.message ?: "Like failed")
                }
        }
    }
    
    fun passCurrentUser() {
        viewModelScope.launch {
            val users = _discoverUsers.value
            val index = _currentDiscoverIndex.value
            if (index >= users.size) return@launch
            
            val targetUser = users[index]
            
            firebaseRepo.passUser(targetUser.id)
                .onSuccess { nextUser() }
                .onFailure { nextUser() } // Continue anyway
        }
    }
    
    private fun nextUser() {
        val newIndex = _currentDiscoverIndex.value + 1
        if (newIndex >= _discoverUsers.value.size) {
            // Load more users
            loadDiscoverUsers()
        } else {
            _currentDiscoverIndex.value = newIndex
        }
    }
    
    fun dismissMatchPopup() {
        _showMatchPopup.value = null
    }
    
    // ==================== MATCHES ====================
    
    fun loadMatches() {
        viewModelScope.launch {
            _matches.value = firebaseRepo.getMatches()
        }
    }
    
    fun observeMatches() {
        viewModelScope.launch {
            firebaseRepo.getMatchesFlow().collect { matchList ->
                _matches.value = matchList
            }
        }
    }
    
    // ==================== LIKES ====================
    
    fun loadLikesReceived() {
        viewModelScope.launch {
            _likesReceived.value = firebaseRepo.getLikesReceived()
        }
    }
    
    fun acceptLike(like: Like) {
        viewModelScope.launch {
            firebaseRepo.likeUser(like.fromUserId)
                .onSuccess { isMatch ->
                    if (isMatch) {
                        firebaseRepo.getUser(like.fromUserId)?.let { user ->
                            _showMatchPopup.value = user
                        }
                        loadMatches()
                    }
                    loadLikesReceived()
                }
        }
    }
    
    fun rejectLike(like: Like) {
        viewModelScope.launch {
            firebaseRepo.passUser(like.fromUserId)
            loadLikesReceived()
        }
    }
    
    // ==================== CHAT ====================
    
    private var currentMatchId: String? = null
    
    fun loadMessages(matchId: String) {
        currentMatchId = matchId
        viewModelScope.launch {
            firebaseRepo.getMessagesFlow(matchId).collect { messageList ->
                _messages.value = messageList
            }
        }
    }
    
    fun sendMessage(text: String) {
        val matchId = currentMatchId ?: return
        viewModelScope.launch {
            firebaseRepo.sendMessage(matchId, text)
                .onFailure {
                    _error.emit("Failed to send message")
                }
        }
    }
    
    // ==================== FCM ====================
    
    fun updateFcmToken(token: String) {
        viewModelScope.launch {
            firebaseRepo.updateFcmToken(token)
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object NotAuthenticated : AuthState()
    object Authenticated : AuthState()
}
