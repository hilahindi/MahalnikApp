package com.example.mahalapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mahalapp.R
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun setBaseContent(layoutResId: Int) {
        // Set the base layout (includes shared UI like navigation bar)
        setContentView(R.layout.activity_base)

        // Inflate the specific content layout into the container
        val contentFrame = findViewById<FrameLayout>(R.id.contentContainer)
        layoutInflater.inflate(layoutResId, contentFrame)

        // Logout button logic with confirmation dialog
        findViewById<ImageButton>(R.id.logoutButton)?.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    // Sign out from Firebase Auth
                    FirebaseAuth.getInstance().signOut()
                    // Go back to LoginActivity and clear activity stack
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null) // Do nothing on cancel
                .show()
        }

        // Setup bottom navigation click listeners
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        // Home button
        findViewById<LinearLayout>(R.id.navHomeLayout).setOnClickListener {
            if (this !is MainActivity) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        // Profile button
        findViewById<LinearLayout>(R.id.navProfileLayout).setOnClickListener {
            if (this !is ProfileActivity) {
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
            }
        }

        // Events button
        findViewById<LinearLayout>(R.id.navEventsLayout).setOnClickListener {
            if (this !is EventsActivity) {
                startActivity(Intent(this, EventsActivity::class.java))
                finish()
            }
        }

        // Contact button
        findViewById<LinearLayout>(R.id.navContactLayout).setOnClickListener {
            if (this !is ContactActivity) {
                startActivity(Intent(this, ContactActivity::class.java))
                finish()
            }
        }

        // Files button
        findViewById<LinearLayout>(R.id.navFilesLayout).setOnClickListener {
            if (this !is FilesActivity) {
                startActivity(Intent(this, FilesActivity::class.java))
                finish()
            }
        }
    }
}
