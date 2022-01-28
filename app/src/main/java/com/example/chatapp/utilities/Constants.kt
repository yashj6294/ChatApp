package com.example.chatapp.utilities

object Constants {
    const val KEY_COLLECTION_USER = "users"
    const val KEY_NAME = "name"
    const val KEY_EMAIL = "email"
    const val KEY_PASSWORD = "password"
    const val KEY_PREFERENCE = "chatAppPreference"
    const val KEY_IS_SIGNED_IN = "isSignedIn"
    const val KEY_USER_ID="userId"
    const val KEY_IMAGE = "image"
    const val KEY_FCM_TOKEN = "fcmToken"
    const val KEY_USER = "user"
    const val KEY_COLLECTION_CHAT = "chat"
    const val KEY_SENDER_ID = "senderId"
    const val KEY_RECEIVER_ID = "receiverId"
    const val KEY_TIMESTAMP = "timestamp"
    const val KEY_MESSAGE = "message"
    const val KEY_SENDER_NAME = "senderName"
    const val KEY_RECEIVER_NAME = "receiverName"
    const val KEY_COLLECTION_CONVERSATIONS = "conversations"
    const val KEY_SENDER_IMAGE = "senderImage"
    const val KEY_RECEIVER_IMAGE = "receiverImage"
    const val KEY_LAST_MESSAGE = "lastMessage"
    const val KEY_AVAILABILITY = "availability"
    const val KEY_REMOTE_MESSAGE_AUTHORIZATION = "Authorization"
    const val KEY_REMOTE_MESSAGE_CONTENT_TYPE = "Content-Type"
    const val KEY_REMOTE_MESSAGE_DATA = "data"
    const val KEY_REMOTE_MESSAGE_REGISTRATION_IDS = "registration_ids"
    private var remoteMsgHeaders :Map<String,String>? = null
    fun getRemoteMsgHeaders() :Map<String,String>{
        if (remoteMsgHeaders==null){
            remoteMsgHeaders = mapOf(
                KEY_REMOTE_MESSAGE_AUTHORIZATION to "key=AAAAnEgLLnc:APA91bGhFBRMj3yJ3yycA8CKC8jAm8mGfPIPP0s233SVFB21z1L5tOyQdxVWwkgSUMFjjoEDKJgwBUsXGRYw4va8fqWFZTPOiFN3KpS75BN7WgRZjhNWY7XRwXkZVxtg1q4brxWNk1Ja",
                KEY_REMOTE_MESSAGE_CONTENT_TYPE to "application/json"
            )
        }
        return remoteMsgHeaders!!
    }
}