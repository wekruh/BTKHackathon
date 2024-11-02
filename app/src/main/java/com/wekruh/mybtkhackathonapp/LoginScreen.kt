package com.wekruh.mybtkhackathonapp

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.wekruh.mybtkhackathonapp.databinding.ActivityMainBinding

class LoginScreen : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.textPassword.typeface = ResourcesCompat.getFont(this, R.font.nexa) // Assuming the font is in res/font

        binding.textPassword.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.textPassword.compoundDrawablesRelative[2]
                if (drawableEnd != null && event.rawX >= (binding.textPassword.right - drawableEnd.bounds.width())) {
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun togglePasswordVisibility() {
        val currentTypeface = binding.textPassword.typeface

        if (binding.textPassword.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            binding.textPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.textPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.show_password, 0) // Set show password icon
        } else {
            binding.textPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.textPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.hide_password, 0) // Set hide password icon
        }

        binding.textPassword.typeface = currentTypeface

        binding.textPassword.setSelection(binding.textPassword.text.length)
    }

    fun signIn(view: View) {
        println("Sign in button clicked")
        val intent = Intent(this@LoginScreen, SignUpScreen::class.java)
        startActivity(intent)
        finish()
    }

    fun login(view: View) {
        val email = binding.textEmail.text.toString()
        val password = binding.textPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            println("All fields are required")
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
        } else {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("Navigating to NavigateScreen")
                        val intent = Intent(this@LoginScreen, NavigateScreen::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Email or password is incorrect.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}