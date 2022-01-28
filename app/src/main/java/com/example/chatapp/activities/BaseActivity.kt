package com.example.chatapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

open class BaseActivity : AppCompatActivity() {
    private val database: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val preferenceManager: PreferenceManager by lazy {
        PreferenceManager(applicationContext)
    }
    private val docRef :DocumentReference by lazy {
        database.collection(Constants.KEY_COLLECTION_USER)
            .document(preferenceManager.getString(Constants.KEY_USER_ID))
    }

    override fun onPause() {
        super.onPause()
        docRef.update(mapOf(
            Constants.KEY_AVAILABILITY to 0
        ))
    }

    override fun onResume() {
        super.onResume()
        docRef.update(mapOf(
            Constants.KEY_AVAILABILITY to 1
        ))
    }
}