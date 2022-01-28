package com.example.chatapp.models

import java.io.Serializable

data class User(
    val name: String?,
    var image: String?,
    var token: String?,
    val email: String?,
    val id: String?
):Serializable
