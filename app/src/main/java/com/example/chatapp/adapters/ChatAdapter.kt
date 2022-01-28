package com.example.chatapp.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ReceivedMessageItemBinding
import com.example.chatapp.databinding.SentMessageItemBinding
import com.example.chatapp.models.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val messages : MutableList<ChatMessage>,private var receiverProfileImage:
Bitmap?,private val senderId:String) : RecyclerView
.Adapter<RecyclerView.ViewHolder>
    () {
    companion object{
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 0
    }
    fun setReceiverProfileImage(bitmap: Bitmap?){
        receiverProfileImage = bitmap
    }
    inner class SentMessageViewHolder(private val binding: SentMessageItemBinding) : RecyclerView
    .ViewHolder(binding.root){
        fun setData(message :ChatMessage){
            binding.tvMessage.text = message.message
            binding.dateTime.text = getReadableDate(message.dateTime)
        }
    }
    inner class ReceiverMessageViewHolder(private val binding :ReceivedMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root){
        fun setData(message :ChatMessage,receiverProfileImage :Bitmap?){
            binding.tvMessage.text = message.message
            binding.dateTime.text = getReadableDate(message.dateTime)
            if(receiverProfileImage!=null){ binding.imgProfile.setImageBitmap(receiverProfileImage) }
        }
    }

    private fun getReadableDate(date : Date) :String{
        return SimpleDateFormat("MMMM dd, yyyy  -  hh:mm a",Locale.getDefault()).format(date)
    }

    override fun getItemViewType(position: Int): Int {
        return if(messages[position].senderId==senderId){
            VIEW_TYPE_SENT
        }else VIEW_TYPE_RECEIVED
    }



    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            (holder as SentMessageViewHolder).setData(messages[position])
        }else{
            (holder as ReceiverMessageViewHolder).setData(messages[position],receiverProfileImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType== VIEW_TYPE_SENT){
            return SentMessageViewHolder(SentMessageItemBinding.inflate(LayoutInflater.from
                (parent.context),parent,false))
        }
        return ReceiverMessageViewHolder(ReceivedMessageItemBinding.inflate(LayoutInflater.from
            (parent.context),parent,false))
    }
}