package com.labactivity.aratsaral

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import com.google.firebase.auth.FirebaseAuth
import com.labactivity.aratsaral.databinding.ActivitySplashBinding

class Splash : AppCompatActivity() {
    private lateinit var binding:ActivitySplashBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DBListHandler(this)
        firebaseAuth = FirebaseAuth.getInstance()

        //Loading Animation
        val rotate = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 1000
        rotate.interpolator = LinearInterpolator()
        rotate.repeatMode = Animation.RESTART
        rotate.repeatCount = Animation.INFINITE
        binding.loadingIV.startAnimation(rotate)


        Handler().postDelayed({
            val mainActivity = Intent(this, MainActivity::class.java)
            val loginActivity = Intent(this, LogIn::class.java)
            if (firebaseAuth.currentUser != null){
                startActivity(mainActivity)
            } else if (firebaseAuth.currentUser == null && db.getAllTasks().isNotEmpty()) {
                startActivity(mainActivity)
            } else {
                startActivity(loginActivity)
            }
            this.finish()
        }, 2000)

    }
}