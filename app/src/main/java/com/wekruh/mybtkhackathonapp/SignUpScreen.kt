package com.wekruh.mybtkhackathonapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.wekruh.mybtkhackathonapp.databinding.ActivitySignUpScreenBinding

class SignUpScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpScreenBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
    }

    fun signUp(view: View) {
        val email = binding.textEmail2.text.toString()
        val password = binding.textPassword2.text.toString()
        val passwordConfirm = binding.textPasswordConfirm.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty() && passwordConfirm.isNotEmpty()) {
            if (password == passwordConfirm) {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this@SignUpScreen, LoginScreen::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_LONG).show()
        }
    }

    fun goBack(view: View) {
        val intent = Intent(this@SignUpScreen, LoginScreen::class.java)
        startActivity(intent)
        finish()
    }
}