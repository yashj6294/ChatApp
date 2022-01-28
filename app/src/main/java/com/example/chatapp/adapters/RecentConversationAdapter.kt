package com.example.chatapp.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.chatapp.databinding.RecentChatUserItemBinding
import com.example.chatapp.listeners.ConversationListener
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.User

class RecentConversationAdapter(private val chatMessages :MutableList<ChatMessage>,private val listener :ConversationListener) : RecyclerView
.Adapter<RecentConversationAdapter
.RecentConversationViewHolder>() {


    private fun getRecentChatUserImage(encodedImage : String) : Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes,0,bytes.size)
    }

    inner class RecentConversationViewHolder(private val binding: RecentChatUserItemBinding) : ViewHolder(binding.root){
            fun setData(chatMessage: ChatMessage){
                binding.imgProfile.setImageBitmap(getRecentChatUserImage(chatMessage.conversationImage!!))
                binding.tvName.text = chatMessage.conversationName
                binding.tvRecentMessage.text = chatMessage.message
                val user = User(
                    chatMessage.conversationName,
                    chatMessage.conversationImage,
                    null,null,
                    chatMessage.conversationId
                )
                binding.root.setOnClickListener { listener.onConversationClicked(user) }
            }
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentConversationViewHolder {
        return RecentConversationViewHolder(RecentChatUserItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: RecentConversationViewHolder, position: Int) {
        holder.setData(chatMessages[position])
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }
}