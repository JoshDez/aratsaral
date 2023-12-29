package com.labactivity.aratsaral

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.labactivity.aratsaral.databinding.ActivitySignUpBinding

class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.signUpButton.setOnClickListener {
            val username = binding.usernameET.text.toString()
            val email = binding.emailET.text.toString()
            val pass = binding.passwordET.text.toString()
            val confirmPass = binding.confirmPassET.text.toString()

            if (username.isEmpty()) {
                Toast.makeText(applicationContext, "Enter your Username", Toast.LENGTH_SHORT).show()
            } else if (email.isEmpty()){
                Toast.makeText(applicationContext, "Enter your Email", Toast.LENGTH_SHORT).show()
            } else if (pass.isEmpty()){
                Toast.makeText(applicationContext, "Enter your Password", Toast.LENGTH_SHORT).show()
            } else if (confirmPass.isEmpty()){
                Toast.makeText(applicationContext, "Enter your Confirm Password", Toast.LENGTH_SHORT).show()
            }else if (pass.length < 6){
                Toast.makeText(applicationContext, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            }else {
                if (confirmPass == pass){
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener{
                        if (it.isSuccessful){
                            setUsername(username)
                        } else {
                            Toast.makeText(applicationContext, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, "Your password don't match", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun setUsername(username:String){
        val user = firebaseAuth.currentUser

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(applicationContext, "${firebaseAuth.currentUser?.displayName}, your account has been created", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    this.finish()
                }else{
                    Toast.makeText(applicationContext, task.exception.toString(), Toast.LENGTH_SHORT).show()
                }

            }
    }
}