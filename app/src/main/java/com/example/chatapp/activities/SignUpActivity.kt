package com.example.chatapp.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.databinding.ActivitySignUpBinding
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class SignUpActivity : AppCompatActivity() {
    private val binding:ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }
    private val preferenceManager:PreferenceManager by lazy {
        PreferenceManager(applicationContext)
    }
    private lateinit var encodedImage:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners(){
        binding.tvSignIn.setOnClickListener {
            onBackPressed()
        }
        binding.btnSignUp.setOnClickListener {
            if(validateSignUp()){
                signUp()
            }
        }
        binding.frameLayoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun showToast(message:String){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }

    private fun encodeImage(bitmap: Bitmap):String{
        val previewWidth = 150
        val previewHeight = bitmap.height*previewWidth/bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes,Base64.DEFAULT)
    }

    private val pickImage :ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode== RESULT_OK){
            if(it.data!=null){
                val imageUri = it.data!!.data
                try {
                    val inputStream = contentResolver.openInputStream(imageUri!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imgProfile.setImageBitmap(bitmap)
                    binding.tvAddImage.visibility = View.GONE
                    encodedImage = encodeImage(bitmap)
                }catch (e:FileNotFoundException){
                    e.printStackTrace()
                }
            }
        }
    }

    private fun signUp(){
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val user: Map<String, Any> = mapOf(
            Constants.KEY_NAME to binding.etName.text.toString(),
            Constants.KEY_EMAIL to binding.etEmail.text.toString(),
            Constants.KEY_PASSWORD to binding.etPassword.text.toString(),
            Constants.KEY_IMAGE to encodedImage)
        database.collection(Constants.KEY_COLLECTION_USER)
            .add(user)
            .addOnSuccessListener {
                loading(false)
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true)
                preferenceManager.putString(Constants.KEY_USER_ID,it.id)
                preferenceManager.putString(Constants.KEY_NAME,binding.etName.text.toString())
                preferenceManager.putString(Constants.KEY_IMAGE,encodedImage)
                val intent = Intent(this,MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener{
                loading(false)
                showToast(it.message.toString())
            }
    }

    private fun validateSignUp():Boolean{
        if(encodedImage==null){
            showToast("Select Profile Image")
            return false
        }else if(binding.etName.text.toString().trim().isEmpty()) {
            showToast("Please Enter your name")
            return false
        }else if(binding.etEmail.text.toString().trim().isEmpty()){
            showToast("Please Enter your email")
            return false
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString().trim()).matches()){
            showToast("Please Enter valid email")
            return false
        }else if(binding.etPassword.text.toString().trim().isEmpty()){
            showToast("Please Enter your password")
            return false
        }else if(binding.etConfirmPassword.text.toString().trim().isEmpty()){
            showToast("Please confirm your password")
            return false
        }else if(binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString()){
            showToast("Passwords don\'t match")
            return false
        }
        return true
    }

    private fun loading(isLoading:Boolean){
        if(isLoading){
            binding.btnSignUp.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.btnSignUp.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

}