package com.labactivity.aratsaral

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.labactivity.aratsaral.databinding.ActivityLogInBinding

class LogIn : AppCompatActivity() {
    private lateinit var binding:ActivityLogInBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.signUpTV.setOnClickListener{
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            this.finish()
        }

        binding.skipTV.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        binding.resetPassTV.setOnClickListener{
            val email = binding.emailET.text.toString()
            if (email.isEmpty()){
                Toast.makeText(applicationContext, "Enter your email", Toast.LENGTH_SHORT).show()
            } else {
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(applicationContext, "Password reset is sent to your email", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(applicationContext, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailET.text.toString()
            val pass = binding.passwordET.text.toString()

            if (email.isEmpty()){
                Toast.makeText(applicationContext, "Enter your email", Toast.LENGTH_SHORT).show()
            } else if (pass.isEmpty()){
                Toast.makeText(applicationContext, "Enter your password", Toast.LENGTH_SHORT).show()
            } else {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener{
                    if (it.isSuccessful){
                        FirebaseDBHandler(this).importTasks(displayMessage = false)
                        Handler().postDelayed({
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            this.finish()
                        }, 2000)
                    } else {
                        Toast.makeText(applicationContext, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }


    }
}