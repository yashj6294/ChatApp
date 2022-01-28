package com.example.chatapp.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.Layout
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.UserItemBinding
import com.example.chatapp.listeners.UserListener
import com.example.chatapp.models.User

class UserAdapter(private val users :List<User>,private val listener :UserListener) : RecyclerView
.Adapter<UserAdapter.UserViewHolder>
    () {
    private fun getUserImage(encodedImage : String) :Bitmap{
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes,0,bytes.size)
    }

    inner class UserViewHolder(private val binding: UserItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setUserData(user:User){
            binding.tvName.text = user.name!!
            binding.tvEmail.text = user.email!!
            binding.imgProfile.setImageBitmap(getUserImage(user.image!!))
            binding.root.setOnClickListener{
                listener.onUserClicked(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = UserItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUserData(users[position])
    }

    override fun getItemCount(): Int {
        return users.size
    }
}