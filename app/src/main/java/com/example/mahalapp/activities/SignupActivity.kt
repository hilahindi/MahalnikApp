package com.example.mahalapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mahalapp.R
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance() // Initialize Firebase auth

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val signupButton = findViewById<Button>(R.id.signupButton)
        val goToLogin = findViewById<TextView>(R.id.goToLogin)

        signupButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Check if email format is valid
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if password is at least 6 characters
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if passwords match
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create user in Firebase
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    // Registration successful - go to profile page
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    // Registration failed
                    Log.e("Register", "Firebase error: ${it.message}")
                    Toast.makeText(this, "Registration failed: Try again", Toast.LENGTH_LONG).show()
                }
        }
        // Go to login screen when text is clicked
        goToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}