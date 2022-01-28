package com.example.chatapp.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.chatapp.adapters.ChatAdapter
import com.example.chatapp.databinding.ActivityChatBinding
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.User
import com.example.chatapp.network.ApiClient
import com.example.chatapp.network.ApiService
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.*

class ChatActivity : BaseActivity() {
    private val preferenceManager: PreferenceManager by lazy {
        PreferenceManager(applicationContext)
    }
    private var isReceiverAvailable = false
    private val binding: ActivityChatBinding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }
    private var conversationId:String? = null
    private val database: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val chatMessages: MutableList<ChatMessage> by lazy {
        mutableListOf()
    }
    private val adapter: ChatAdapter by lazy {
        ChatAdapter(
            chatMessages, getUserImage(receiverUser.image), preferenceManager.getString
                (Constants.KEY_USER_ID)
        )
    }
    private lateinit var receiverUser: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setListeners()
        loadReceiverUser()
        binding.rvChat.adapter = adapter
        listenMessages()
    }

    private fun listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(
                Constants.KEY_SENDER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(
                Constants.KEY_SENDER_ID, receiverUser.id
            )
            .whereEqualTo(
                Constants.KEY_RECEIVER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .addSnapshotListener(eventListener)
    }

    private val eventListener =
        EventListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
            if (error != null) return@EventListener
            if (value != null) {
                val count = chatMessages.size
                for (docChange in value.documentChanges) {
                    if (docChange.type == DocumentChange.Type.ADDED) {
                        val chatMessage = ChatMessage(
                            docChange.document.getString(Constants.KEY_SENDER_ID)!!,
                            docChange.document.getString(Constants.KEY_RECEIVER_ID)!!,
                            docChange.document.getString(Constants.KEY_MESSAGE)!!,
                            docChange.document.getDate(Constants.KEY_TIMESTAMP)!!,
                            null,null,null
                        )
                        chatMessages.add(chatMessage)
                    }
                }
                chatMessages.sortWith { t, t2 ->
                    t.dateTime
                        .compareTo(t2.dateTime)
                }
                if (count == 0) {
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
                    binding.rvChat.smoothScrollToPosition(chatMessages.size - 1)
                }
                binding.rvChat.visibility = View.VISIBLE
            }
            binding.progressBar.visibility = View.GONE
            if (conversationId==null){
                checkForConversation()
            }
        }

    private fun addConversation(conversation :Map<String,Any>){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .add(conversation)
            .addOnSuccessListener {
                conversationId = it.id
            }
    }

    private fun updateConversation(message :String){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId!!)
            .update(mapOf(
                Constants.KEY_LAST_MESSAGE to message,
                Constants.KEY_TIMESTAMP to Date()
            ))
    }

    private fun checkForConversation(){
        if (chatMessages.size!=0){
            checkForConversationRemotely(preferenceManager.getString(Constants.KEY_USER_ID), receiverUser.id!! )
            checkForConversationRemotely(receiverUser.id!!,preferenceManager.getString(Constants.KEY_USER_ID))
        }
    }
    private fun checkForConversationRemotely(senderId :String,receiverId :String){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
            .get()
            .addOnCompleteListener(conversationOnCompleteListener)
    }
    private val conversationOnCompleteListener = OnCompleteListener<QuerySnapshot>{task ->
        if (task.isSuccessful && task.result!=null && task.result!!.documents.size>0){
            val docSnapshot = task.result!!.documents[0]
            conversationId = docSnapshot.id
        }
    }

    private fun sendMessage() {
        val chat: Map<String, Any> = mapOf(
            Constants.KEY_SENDER_ID to preferenceManager.getString
                (Constants.KEY_USER_ID),
            Constants.KEY_RECEIVER_ID to receiverUser.id!!,
            Constants.KEY_MESSAGE to binding.etMsg.text.toString(),
            Constants.KEY_TIMESTAMP to Date()
        )
        database.collection(Constants.KEY_COLLECTION_CHAT).add(chat)
        if (conversationId==null){
            val conversation :Map<String,Any> = mapOf(
                Constants.KEY_SENDER_ID to preferenceManager.getString
                    (Constants.KEY_USER_ID),
                Constants.KEY_SENDER_NAME to preferenceManager.getString(Constants.KEY_NAME),
                Constants.KEY_RECEIVER_NAME to receiverUser.name!!,
                Constants.KEY_RECEIVER_IMAGE to receiverUser.image!!,
                Constants.KEY_SENDER_IMAGE to preferenceManager.getString(Constants.KEY_IMAGE),
                Constants.KEY_RECEIVER_ID to receiverUser.id!!,
                Constants.KEY_LAST_MESSAGE to binding.etMsg.text.toString(),
                Constants.KEY_TIMESTAMP to Date()
            )
            addConversation(conversation)
        }else{
            updateConversation(binding.etMsg.text.toString())
        }
        if(!isReceiverAvailable){
            try {
                val tokens = JSONArray()
                tokens.put(receiverUser.token!!)
                val data = JSONObject()
                data.put(Constants.KEY_USER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                data.put(Constants.KEY_NAME,preferenceManager.getString(Constants.KEY_NAME))
                data.put(Constants.KEY_FCM_TOKEN,preferenceManager.getString(Constants.KEY_FCM_TOKEN))
                data.put(Constants.KEY_MESSAGE,binding.etMsg.text)

                val body = JSONObject()
                body.put(Constants.KEY_REMOTE_MESSAGE_DATA,data)
                body.put(Constants.KEY_REMOTE_MESSAGE_REGISTRATION_IDS,tokens)

                sendNotification(body.toString())
            }catch (e:Exception){
                showToast(e.message!!)
            }
        }
        binding.etMsg.text = null
    }

    private fun showToast(message:String){
        Toast.makeText(this,message, Toast.LENGTH_LONG).show()
    }

    private fun sendNotification(messageBody :String){
        ApiClient.getClient().create(ApiService::class.java)
            .sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
            ).enqueue(object : Callback<String>{
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if(response.isSuccessful){
                        try {
                            if(response.body()!=null){
                                val responseJson = JSONObject(response.body()!!)
                                val results = responseJson.getJSONArray("results")
                                if (responseJson.getInt("failure")==1){
                                    val error = results.get(0) as JSONObject
                                    showToast(error.getString("error"))
                                    return
                                }
                            }
                        }catch (e:JSONException){
                            e.printStackTrace()
                        }
                        showToast("Notification sent successfully")
                    }else{
                        showToast("Error : "+response.code())
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    showToast(t.message!!)
                }
            })
    }

    private fun listenForReceiverAvailability(){
        database.collection(Constants.KEY_COLLECTION_USER)
            .document(receiverUser.id!!)
            .addSnapshotListener(this@ChatActivity
            ) { value, error ->
                if (error!=null) return@addSnapshotListener
                if (value!=null){
                    if (value.getLong(Constants.KEY_AVAILABILITY)!=null){
                        val availability = value.getLong(Constants.KEY_AVAILABILITY)
                        isReceiverAvailable = availability==1L
                    }
                    receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN)
                    if (receiverUser.image==null){
                        receiverUser.image = value.getString(Constants.KEY_IMAGE)
                        adapter.setReceiverProfileImage(getUserImage(receiverUser.image))
                        adapter.notifyItemRangeChanged(0,chatMessages.size)
                    }
                }
                if (isReceiverAvailable){
                    binding.tvAvailable.visibility = View.VISIBLE
                }else{
                    binding.tvAvailable.visibility = View.GONE
                }
            }

    }

    private fun getUserImage(encodedImage: String?): Bitmap? {
        if(encodedImage!=null){
            val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        return null
    }

    private fun loadReceiverUser() {
        receiverUser = intent.getSerializableExtra(Constants.KEY_USER) as User
        binding.tvName.text = receiverUser.name
    }

    private fun setListeners() {
        binding.imgBack.setOnClickListener { onBackPressed() }
        binding.layoutSend.setOnClickListener { sendMessage() }
    }

    override fun onResume() {
        super.onResume()
        listenForReceiverAvailability()
    }
}