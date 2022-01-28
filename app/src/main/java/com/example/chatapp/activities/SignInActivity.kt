package com.example.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivitySignInBinding
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {
    private val binding:ActivitySignInBinding by lazy {
        ActivitySignInBinding.inflate(layoutInflater)
    }
    private val preferenceManager: PreferenceManager by lazy {
        PreferenceManager(applicationContext)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners(){
        binding.tvCreateNewAccount.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
        }
        binding.btnSignIn.setOnClickListener {
            if(validateSignIn()){
                signIn()
            }
        }
    }
    private fun signIn(){
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USER)
            .whereEqualTo(Constants.KEY_EMAIL,binding.etEmail.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD,binding.etPassword.text.toString())
            .get()
            .addOnCompleteListener{
                if(it.isSuccessful && it.result!=null && it.result!!.documents.size>0){
                    val snapshot = it.result!!.documents[0]
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true)
                    preferenceManager.putString(Constants.KEY_USER_ID,snapshot.id)
                    preferenceManager.putString(Constants.KEY_NAME, snapshot.getString(Constants
                        .KEY_NAME).toString()
                    )
                    preferenceManager.putString(Constants.KEY_IMAGE,snapshot.get(Constants
                        .KEY_IMAGE).toString())
                    val intent = Intent(this,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                }else{
                    loading(false)
                    showToast("Unable to sign in")
                }
            }
            .addOnFailureListener{

            }
    }
    private fun showToast(message:String){
        Toast.makeText(this,message, Toast.LENGTH_LONG).show()
    }
    private fun validateSignIn():Boolean{
        if(binding.etEmail.text.toString().trim().isEmpty()){
            showToast("Please Enter your email")
            return false
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString().trim()).matches()){
            showToast("Please Enter valid email")
            return false
        }else if(binding.etPassword.text.toString().trim().isEmpty()){
            showToast("Please Enter your password")
            return false
        }
        return true
    }
    private fun loading(isLoading:Boolean){
        if(isLoading){
            binding.btnSignIn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.btnSignIn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}