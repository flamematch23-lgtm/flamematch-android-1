package com.flamematch.app.data.model

import com.google.firebase.Timestamp

data class Match(
    val id: String = "",
    val users: List<String> = emptyList(),
    val user1Id: String = "",
    val user2Id: String = "",
    val user1Name: String = "",
    val user2Name: String = "",
    val user1Photo: String = "",
    val user2Photo: String = "",
    val createdAt: Timestamp? = null,
    val lastMessage: String? = null,
    val lastMessageTime: Timestamp? = null,
    val lastMessageSenderId: String? = null,
    val unreadCount: Map<String, Int> = emptyMap(),
    val isActive: Boolean = true
) {
    constructor() : this(id = "")
    
    fun getOtherUserId(currentUserId: String): String {
        return if (user1Id == currentUserId) user2Id else user1Id
    }
    
    fun getOtherUserName(currentUserId: String): String {
        return if (user1Id == currentUserId) user2Name else user1Name
    }
    
    fun getOtherUserPhoto(currentUserId: String): String {
        return if (user1Id == currentUserId) user2Photo else user1Photo
    }
}
