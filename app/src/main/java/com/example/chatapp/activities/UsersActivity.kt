package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.chatapp.adapters.UserAdapter
import com.example.chatapp.databinding.ActivityUsersBinding
import com.example.chatapp.listeners.UserListener
import com.example.chatapp.models.User
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class UsersActivity : BaseActivity(),UserListener{
    private val binding:ActivityUsersBinding by lazy {
        ActivityUsersBinding.inflate(layoutInflater)
    }
    private val preferenceManager:PreferenceManager by lazy {
        PreferenceManager(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setListeners()
        getUsers()
    }
    private fun setListeners(){
        binding.imgBack.setOnClickListener { onBackPressed() }
    }
    private fun getUsers(){
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER)
            .get()
            .addOnCompleteListener{
                loading(false)
                val currentUserId = preferenceManager.getString(Constants.KEY_USER_ID)
                if(it.isSuccessful && it.result!=null){
                    val users :MutableList<User> = mutableListOf()
                    for(snapshot in it.result!!){
                        if(currentUserId==snapshot.id) continue
                        val user = User(
                            snapshot.getString(Constants.KEY_NAME).toString(),
                            snapshot.getString(Constants.KEY_IMAGE).toString(),
                            snapshot.getString(Constants.KEY_FCM_TOKEN).toString(),
                            snapshot.getString(Constants.KEY_EMAIL).toString(),
                            snapshot.id,
                        )
                        users.add(user)
                    }
                    if(users.size>0){
                        val adapter = UserAdapter(users,this)
                        binding.usersRecyclerView.adapter = adapter
                        binding.usersRecyclerView.visibility = View.VISIBLE
                    }else{
                        showErrorMessage()
                    }
                }else{
                    showErrorMessage()
                }
            }
    }
    private fun showErrorMessage(){
        binding.tvError.text = String.format("%s","No user available")
        binding.tvError.visibility = View.VISIBLE
    }
    private fun loading(isLoading:Boolean){
        if(isLoading){
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onUserClicked(user: User) {
        val intent = Intent(applicationContext,ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER,user)
        startActivity(intent)
        finish()
    }
}