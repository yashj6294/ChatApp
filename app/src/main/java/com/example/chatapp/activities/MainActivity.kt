package com.example.chatapp.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.chatapp.adapters.RecentConversationAdapter
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.listeners.ConversationListener
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.User
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

class MainActivity : BaseActivity(), ConversationListener{
    private val binding:ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val preferenceManager: PreferenceManager by lazy {
        PreferenceManager(applicationContext)
    }
    private val database :FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val recentConversations :MutableList<ChatMessage> by lazy {
        mutableListOf()
    }
    private val adapter :RecentConversationAdapter by lazy {
        RecentConversationAdapter(recentConversations,this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        loadUserDetails()
        getToken()
        setListeners()
        binding.rvRecentConversations.adapter = adapter
        listenConversations()
    }

    private fun listenConversations(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    private val eventListener = EventListener<QuerySnapshot>{ value, error ->
        if(error!=null) return@EventListener
        if(value!=null){
            for(docChange in value.documentChanges){
                if (docChange.type==DocumentChange.Type.ADDED){
                    val senderId = docChange.document.getString(Constants.KEY_SENDER_ID)
                    val receiverId = docChange.document.getString(Constants.KEY_RECEIVER_ID)
                    if(preferenceManager.getString(Constants.KEY_USER_ID) == senderId){
                        val convImg = docChange.document.getString(Constants.KEY_RECEIVER_IMAGE)
                        val convName =  docChange.document.getString(Constants.KEY_RECEIVER_NAME)
                        val msg = docChange.document.getString(Constants.KEY_LAST_MESSAGE)
                        val time = docChange.document.getDate(Constants.KEY_TIMESTAMP)
                        val conversation = ChatMessage(
                            senderId,
                            receiverId!!,
                            msg!!,
                            time!!,
                            receiverId,
                            convName,
                            convImg
                        )
                        recentConversations.add(conversation)
                    }else{
                        val convImg = docChange.document.getString(Constants.KEY_SENDER_IMAGE)
                        val convName =  docChange.document.getString(Constants.KEY_SENDER_NAME)
                        val msg = docChange.document.getString(Constants.KEY_LAST_MESSAGE)
                        val time = docChange.document.getDate(Constants.KEY_TIMESTAMP)
                        val conversation = ChatMessage(
                            senderId!!,
                            receiverId!!,
                            msg!!,
                            time!!,
                            senderId,
                            convName,
                            convImg
                        )
                        recentConversations.add(conversation)
                    }
                }else if(docChange.type==DocumentChange.Type.MODIFIED){
                    for(doc in recentConversations){
                        val senderId = docChange.document.getString(Constants.KEY_SENDER_ID)
                        val receiverId = docChange.document.getString(Constants.KEY_RECEIVER_ID)
                        if(doc.senderId==senderId && doc.receiverId==receiverId){
                            doc.message = docChange.document.getString(Constants.KEY_LAST_MESSAGE)!!
                            doc.dateTime = docChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                            break
                        }
                    }
                }
            }
            recentConversations.sortWith { t, t2 -> t2.dateTime.compareTo(t.dateTime) }
            adapter.notifyDataSetChanged()
            binding.rvRecentConversations.smoothScrollToPosition(0)
            binding.rvRecentConversations.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
    private fun showToast(message:String){
        Toast.makeText(this,message, Toast.LENGTH_LONG).show()
    }
    private fun getToken(){
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token-> updateToken(token) }
    }
    private fun loadUserDetails(){
        binding.tvName.text = preferenceManager.getString(Constants.KEY_NAME)
        val bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.size)
        binding.imgProfile.setImageBitmap(bitmap)
    }
    private fun updateToken(token :String){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token)
        val database = FirebaseFirestore.getInstance()
        val docRef = database.collection(Constants.KEY_COLLECTION_USER).document(
            preferenceManager.getString(Constants.KEY_USER_ID)
        )
        docRef.update(Constants.KEY_FCM_TOKEN,token)
            .addOnFailureListener { showToast("Unable to update token") }
    }
    private fun setListeners(){
        binding.icSignOut.setOnClickListener {
            signOut()
        }
        binding.fabNewChat.setOnClickListener{
            startActivity(Intent(this,UsersActivity::class.java))
        }
    }
    private fun signOut(){
        val database = FirebaseFirestore.getInstance()
        val docRef = database.collection(Constants.KEY_COLLECTION_USER).document(
            preferenceManager.getString(Constants.KEY_USER_ID)
        )
        docRef.update(Constants.KEY_FCM_TOKEN,FieldValue.delete())
            .addOnSuccessListener {
                showToast("Signing out")
                preferenceManager.clear()
                startActivity(Intent(this,SignInActivity::class.java))
                finish()
            }
            .addOnFailureListener { showToast("Unable to sign out") }
    }

    override fun onConversationClicked(user: User) {
        val intent = Intent(this,ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER,user)
        startActivity(intent)
    }
}