package com.sunilnaithani.firebaseemailauth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sunilnaithani.firebaseemailauth.databinding.ActivityUserBinding

class User : AppCompatActivity() {
    private lateinit var binding:ActivityUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var email : String
    private lateinit var password :String
    private lateinit var currentUser: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
    }

    fun regUser(view: View) {
        email = binding.edtEmail.text.toString().trim()
        password = binding.edtPassword.text.toString().trim()
        val rePassword = binding.edtRePassword.text.toString().trim()
        if(email.isEmpty()){
            binding.edtEmail.error = "Insert Email Id"
            binding.edtEmail.requestFocus()
        } else if(password.isEmpty()){
            binding.edtPassword.error = "Create Password"
            binding.edtPassword.requestFocus()
        }else if (password.length < 6) {
            binding.edtPassword.error = "Password Length Should be >= 6 Char."
            binding.edtPassword.requestFocus()
        }else if(password !=rePassword){
            binding.edtRePassword.error = "Password Not Matched"
            binding.edtRePassword.requestFocus()
        }else {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        currentUser = Firebase.auth.currentUser!!
                        currentUser.sendEmailVerification()
                            .addOnCompleteListener{
                                if(it.isSuccessful){
                                    Snackbar.make(findViewById(binding.cardView.id),
                                        "A verification email has been sent. Please Verify", Snackbar.LENGTH_INDEFINITE
                                    ).apply {
                                        setAction("Done"){
                                            verifyUser()
                                        }
                                        show()
                                    }

                                }

                            }

                    }else {
                        val ex = task.exception.toString()
                        if(ex.contains("The email address is already in use by another account.",false)) {
                            Toast.makeText(baseContext, "The email address is already Register", Toast.LENGTH_LONG,).show()
                            finish()
                        } else if(ex.contains("The email address is badly formatted.",false)) {
                            Toast.makeText(baseContext, "Bad Email Id", Toast.LENGTH_LONG,).show()
                        }else{
                            Toast.makeText(baseContext, task.exception.toString(), Toast.LENGTH_LONG,).show()
                        }
                    }

                }

        }

    }
    private  fun verifyUser(){
        Firebase.auth.signOut()
        Thread.sleep(2000)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    currentUser = auth.currentUser!!
                    finish()
                }
            }


    }
}