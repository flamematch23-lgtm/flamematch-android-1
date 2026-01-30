package com.flamematch.app.data.repository

import com.flamematch.app.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.*

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    val currentUserId: String? get() = auth.currentUser?.uid
    val isLoggedIn: Boolean get() = auth.currentUser != null
    
    // ==================== AUTH ====================
    
    suspend fun signInWithEmail(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Failed to create user")
            
            // Create user profile
            val user = User(
                id = uid,
                email = email,
                name = name,
                createdAt = Timestamp.now(),
                lastActive = Timestamp.now()
            )
            db.collection("users").document(uid).set(user.toMap()).await()
            
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Google sign in failed")
            
            // Check if user exists, if not create profile
            val userDoc = db.collection("users").document(user.uid).get().await()
            if (!userDoc.exists()) {
                val newUser = User(
                    id = user.uid,
                    email = user.email ?: "",
                    name = user.displayName ?: "",
                    profilePhoto = user.photoUrl?.toString() ?: "",
                    createdAt = Timestamp.now(),
                    lastActive = Timestamp.now()
                )
                db.collection("users").document(user.uid).set(newUser.toMap()).await()
            }
            
            Result.success(user.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun signOut() {
        auth.signOut()
    }
    
    // ==================== USER PROFILE ====================
    
    suspend fun getCurrentUser(): User? {
        val uid = currentUserId ?: return null
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.toObject(User::class.java)?.copy(id = uid)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getUser(userId: String): User? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(User::class.java)?.copy(id = userId)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun updateUser(updates: Map<String, Any>): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val updatesWithTimestamp = updates.toMutableMap()
            updatesWithTimestamp["lastActive"] = Timestamp.now()
            db.collection("users").document(uid).update(updatesWithTimestamp).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateLocation(latitude: Double, longitude: Double, city: String, country: String): Result<Unit> {
        return updateUser(mapOf(
            "location" to GeoPoint(latitude, longitude),
            "city" to city,
            "country" to country
        ))
    }
    
    // ==================== DISCOVER / SWIPE ====================
    
    suspend fun getDiscoverUsers(limit: Int = 20): List<User> {
        val uid = currentUserId ?: return emptyList()
        val currentUser = getCurrentUser() ?: return emptyList()
        
        return try {
            // Get users already interacted with
            val swipedIds = mutableSetOf(uid)
            
            // Get likes sent
            val likesQuery = db.collection("likes")
                .whereEqualTo("fromUserId", uid)
                .get().await()
            likesQuery.documents.forEach { doc ->
                doc.getString("toUserId")?.let { swipedIds.add(it) }
            }
            
            // Get passes
            val passesQuery = db.collection("passes")
                .whereEqualTo("fromUserId", uid)
                .get().await()
            passesQuery.documents.forEach { doc ->
                doc.getString("toUserId")?.let { swipedIds.add(it) }
            }
            
            // Query users
            val usersQuery = db.collection("users")
                .limit(limit.toLong() + swipedIds.size.toLong())
                .get().await()
            
            usersQuery.documents
                .mapNotNull { doc -> 
                    doc.toObject(User::class.java)?.copy(id = doc.id)
                }
                .filter { user -> 
                    user.id !in swipedIds &&
                    matchesPreferences(currentUser, user) &&
                    matchesPreferences(user, currentUser)
                }
                .take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun matchesPreferences(viewer: User, profile: User): Boolean {
        // Gender preference
        if (viewer.lookingFor != "everyone" && viewer.lookingFor != profile.gender) {
            return false
        }
        // Age preference
        if (profile.age < viewer.minAge || profile.age > viewer.maxAge) {
            return false
        }
        return true
    }
    
    // ==================== LIKES & MATCHING ====================
    
    suspend fun likeUser(targetUserId: String, isSuperLike: Boolean = false, message: String? = null): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        val currentUser = getCurrentUser() ?: return Result.failure(Exception("User not found"))
        
        return try {
            val likeId = "${uid}_${targetUserId}"
            val like = Like(
                id = likeId,
                fromUserId = uid,
                toUserId = targetUserId,
                fromUserName = currentUser.name,
                fromUserPhoto = currentUser.profilePhoto,
                fromUserAge = currentUser.age,
                isSuperLike = isSuperLike,
                message = message,
                createdAt = Timestamp.now()
            )
            
            db.collection("likes").document(likeId).set(like).await()
            
            // Update daily limits
            if (isSuperLike) {
                updateUser(mapOf("dailySuperLikesRemaining" to (currentUser.dailySuperLikesRemaining - 1)))
            } else if (!currentUser.isPremium) {
                updateUser(mapOf("dailyLikesRemaining" to (currentUser.dailyLikesRemaining - 1)))
            }
            
            // Check for mutual like (match)
            val mutualLikeId = "${targetUserId}_${uid}"
            val mutualLike = db.collection("likes").document(mutualLikeId).get().await()
            
            if (mutualLike.exists()) {
                // It's a match!
                createMatch(uid, targetUserId, currentUser)
                Result.success(true) // true = it's a match
            } else {
                Result.success(false) // false = no match yet
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun passUser(targetUserId: String): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val passId = "${uid}_${targetUserId}"
            db.collection("passes").document(passId).set(mapOf(
                "fromUserId" to uid,
                "toUserId" to targetUserId,
                "createdAt" to Timestamp.now()
            )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun createMatch(userId1: String, userId2: String, user1: User) {
        val user2 = getUser(userId2) ?: return
        
        val matchId = if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
        val match = Match(
            id = matchId,
            users = listOf(userId1, userId2),
            user1Id = userId1,
            user2Id = userId2,
            user1Name = user1.name,
            user2Name = user2.name,
            user1Photo = user1.profilePhoto,
            user2Photo = user2.profilePhoto,
            createdAt = Timestamp.now()
        )
        
        db.collection("matches").document(matchId).set(match).await()
        
        // Mark likes as matched
        db.collection("likes").document("${userId1}_${userId2}").update("isMatched", true)
        db.collection("likes").document("${userId2}_${userId1}").update("isMatched", true)
    }
    
    // ==================== MATCHES ====================
    
    fun getMatchesFlow(): Flow<List<Match>> = callbackFlow {
        val uid = currentUserId ?: throw Exception("Not logged in")
        
        val listener = db.collection("matches")
            .whereArrayContains("users", uid)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val matches = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Match::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(matches)
            }
        
        awaitClose { listener.remove() }
    }
    
    suspend fun getMatches(): List<Match> {
        val uid = currentUserId ?: return emptyList()
        return try {
            val snapshot = db.collection("matches")
                .whereArrayContains("users", uid)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .get().await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Match::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // ==================== LIKES RECEIVED ====================
    
    suspend fun getLikesReceived(): List<Like> {
        val uid = currentUserId ?: return emptyList()
        return try {
            val snapshot = db.collection("likes")
                .whereEqualTo("toUserId", uid)
                .whereEqualTo("isMatched", false)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Like::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // ==================== CHAT ====================
    
    fun getMessagesFlow(matchId: String): Flow<List<Message>> = callbackFlow {
        val listener = db.collection("matches").document(matchId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(messages)
            }
        
        awaitClose { listener.remove() }
    }
    
    suspend fun sendMessage(matchId: String, text: String, type: String = "text"): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        val currentUser = getCurrentUser() ?: return Result.failure(Exception("User not found"))
        
        return try {
            val messageRef = db.collection("matches").document(matchId)
                .collection("messages").document()
            
            val message = Message(
                id = messageRef.id,
                matchId = matchId,
                senderId = uid,
                senderName = currentUser.name,
                text = text,
                type = type,
                createdAt = Timestamp.now()
            )
            
            messageRef.set(message).await()
            
            // Update match with last message
            db.collection("matches").document(matchId).update(mapOf(
                "lastMessage" to text,
                "lastMessageTime" to Timestamp.now(),
                "lastMessageSenderId" to uid
            )).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== FCM TOKEN ====================
    
    suspend fun updateFcmToken(token: String) {
        currentUserId?.let { uid ->
            try {
                db.collection("users").document(uid)
                    .update("fcmToken", token).await()
            } catch (e: Exception) {
                // Ignore errors
            }
        }
    }
}
