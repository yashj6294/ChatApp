package com.example.chatapp.models

import java.util.*

data class ChatMessage(
    val senderId:String,
    val receiverId :String,
    var message :String,
    var dateTime :Date,
    val conversationId :String?,
    val conversationName :String?,
    val conversationImage :String?,
)
