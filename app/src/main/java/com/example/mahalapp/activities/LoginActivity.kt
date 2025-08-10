package com.example.mahalapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import com.example.mahalapp.R
import com.example.mahalapp.activities.SignupActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Firebase authentication instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance() // Init FirebaseAuth

        // Grab views from layout
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val goToSignup = findViewById<TextView>(R.id.goToSignup)

        // When login button is clicked
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Basic email format check
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Check password is not empty
            if (password.isEmpty()) {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sign in to Firebase with email + password
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // If success → go to main activity
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish() // close login screen so user can’t go back here
                    } else {
                        // If fail → log the reason + show error toast
                        Log.e("Login", "Firebase error: ${task.exception?.message}")
                        Toast.makeText(this, "Login failed. Try again", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Go to signup screen if user clicks the text
        goToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // Uncomment below line if you want to force logout every time (for testing)
        // auth.signOut()

        // Auto-login: if user already signed in → skip login and go to main screen
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
