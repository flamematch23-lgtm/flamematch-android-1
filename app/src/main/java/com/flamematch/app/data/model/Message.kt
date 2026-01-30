package com.flamematch.app.data.model

import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val matchId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val voiceUrl: String? = null,
    val type: String = "text", // text, image, voice, gif, icebreaker
    val createdAt: Timestamp? = null,
    val readAt: Timestamp? = null,
    val isRead: Boolean = false,
    val icebreakerGame: String? = null,
    val icebreakerData: Map<String, Any>? = null
) {
    constructor() : this(id = "")
}
