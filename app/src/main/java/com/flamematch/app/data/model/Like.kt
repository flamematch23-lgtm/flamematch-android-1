package com.flamematch.app.data.model

import com.google.firebase.Timestamp

data class Like(
    val id: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val fromUserName: String = "",
    val fromUserPhoto: String = "",
    val fromUserAge: Int = 0,
    val isSuperLike: Boolean = false,
    val message: String? = null,
    val createdAt: Timestamp? = null,
    val isMatched: Boolean = false
) {
    constructor() : this(id = "")
}
