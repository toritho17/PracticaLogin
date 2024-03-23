package com.sunilnaithani.firebaseemailauth

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sunilnaithani.firebaseemailauth.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var email : String
    private lateinit var password :String
    private lateinit var currentUser: FirebaseUser
    private var oneTapClient: SignInClient? = null
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(this)
        requestSignIn()
    }

    fun loginUser(view: View) {
        email = binding.edtEmail.text.toString().trim()
        password = binding.edtPassword.text.toString().trim()
        if (email.isEmpty()) {
            binding.edtEmail.error = "Insert Email ID"
            binding.edtEmail.requestFocus()
        } else if (password.isEmpty()) {
            binding.edtPassword.error = "Create Password"
            binding.edtPassword.requestFocus()
        } else if (password.length < 6) {
            binding.edtPassword.error = "Password Length Should be > 6 Char."
            binding.edtPassword.requestFocus()
        }else{
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        currentUser = auth.currentUser!!
                        finish()
                    }else{
                        val ex = it.exception.toString()
                        Log.d(TAG, it.exception.toString())
                        if(ex.contains("The supplied auth credential is incorrect, malformed or has expired.",false)) {
                            Toast.makeText(baseContext, "User Not Registered SignUp First", Toast.LENGTH_LONG,).show()
                        } else if(ex.contains("The email address is badly formatted.",false)) {
                            Toast.makeText(baseContext, "Bad Email Id", Toast.LENGTH_LONG,).show()
                        }else{
                            Toast.makeText(baseContext, it.exception.toString(), Toast.LENGTH_LONG,).show()
                        }
                    }
                }
        }


    }

    fun resetPassword(view: View) {
        email = binding.edtEmail.text.toString().trim()
        if (email.isEmpty()) {
            binding.edtEmail.error = "Insert Email ID"
            binding.edtEmail.requestFocus()
        }else{
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener {task ->
                    if(task.isSuccessful){
                        Toast.makeText(baseContext, "Password Reset Link sent please check email",
                            Toast.LENGTH_SHORT,).show()
                    }else{
                        Toast.makeText(baseContext, task.exception.toString(),
                            Toast.LENGTH_SHORT,).show()
                    }

                }
        }

    }

    fun registerUser(view: View) {
        val intent = Intent(this, User::class.java)
        startActivity(intent)
        finish()
    }
    private fun requestSignIn(){
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .build()
    }

    fun signingGoogle(view: View) {
        CoroutineScope(Dispatchers.Main).launch {
            signingGoogle()
        }
    }


    private suspend fun signingGoogle() {
        val result = oneTapClient?.beginSignIn(signInRequest)?.await()
        val intentSenderRequest = IntentSenderRequest.Builder(result!!.pendingIntent).build()
        activityResultLauncher.launch(intentSenderRequest)
        binding.progressBar.visibility = View.VISIBLE
    }


    private val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential = oneTapClient!!.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential).addOnCompleteListener {
                            if (it.isSuccessful) {
                                currentUser = auth.currentUser!!
                                binding.progressBar.visibility = View.INVISIBLE
                                Toast.makeText(this, "Sign In Complete", Toast.LENGTH_LONG).show()
                                finish()

                            }
                        }
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
        }


}