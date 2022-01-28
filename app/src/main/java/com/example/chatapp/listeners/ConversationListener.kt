package com.example.chatapp.listeners

import com.example.chatapp.models.User

interface ConversationListener {
    fun onConversationClicked(user : User)
}