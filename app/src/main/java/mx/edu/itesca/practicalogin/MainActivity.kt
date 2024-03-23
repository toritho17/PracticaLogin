package com.sunilnaithani.firebaseemailauth

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sunilnaithani.firebaseemailauth.databinding.ActivityMainBinding
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

    }

    private fun logIn() {
        val currentUser = auth.currentUser
        if (currentUser == null) run {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

    }

    fun signOutUser(view: View) {
        Firebase.auth.signOut()
        Toast.makeText(this, "Sign Out", Toast.LENGTH_LONG).show()
        binding.txtName.text = ""
        binding.txtEmail.text = ""
        binding.txtStatus.text = "Sign Out"
        binding.imageView.setImageBitmap(null)
        val intent = Intent(this, Login::class.java)
        startActivity(intent)

    }
    fun chUser(view: View) {
        verifyUser()

    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser !=null){
            showUser()
            binding.btnSignOut.visibility = View.VISIBLE
        }else{
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
    private fun verifyUser(){
        val currentUser = Firebase.auth.currentUser!!
        currentUser.sendEmailVerification()
            .addOnCompleteListener{
                if(it.isSuccessful){
                    Snackbar.make(findViewById(binding.btnVerify.id),
                        "A verification email has been sent. Please Verify", Snackbar.LENGTH_INDEFINITE
                    ).apply {
                        setAction("Done"){
                            Firebase.auth.signOut()
                            logIn()
                        }
                        show()
                    }

                }

            }
    }
    private fun showUser() {
        val user = Firebase.auth.currentUser
        user?.let {
            val name = it.displayName
            val email = it.email
            val photoUrl = it.photoUrl
            val emailVerified = it.isEmailVerified
            binding.txtName.text = name
            binding.txtEmail.text = email
            if (emailVerified) {
                binding.txtStatus.text = "Verified Email"
                binding.btnVerify.visibility = View.INVISIBLE
            }else{
                binding.btnVerify.visibility = View.VISIBLE
            }
            var image: Bitmap? = null
            val imageURL = photoUrl.toString()
            val executorService = Executors.newSingleThreadExecutor()
            executorService.execute {
                try {
                    val `in` = java.net.URL(imageURL).openStream()
                    image = BitmapFactory.decodeStream(`in`)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            runOnUiThread {
                try {
                    Thread.sleep(1000)
                    binding.imageView.setImageBitmap(image)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }

        }
    }

}