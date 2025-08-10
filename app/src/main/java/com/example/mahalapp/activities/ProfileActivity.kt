package com.example.mahalapp.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.*
import com.example.mahalapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : BaseActivity() {

    private lateinit var db: FirebaseFirestore // Firestore database instance
    private lateinit var auth: FirebaseAuth // Firebase authentication instance
    private var isEditing = false // Tracks whether fields are in edit mode

    private lateinit var editButton: Button
    private lateinit var saveButton: Button
    private lateinit var allFields: List<EditText>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBaseContent(R.layout.activity_profile)

        db = FirebaseFirestore.getInstance() // Initialize Firestore
        auth = FirebaseAuth.getInstance() // Initialize Firebase Auth

        // Buttons
        editButton = findViewById(R.id.editButton)
        saveButton = findViewById(R.id.saveButton)

        // List of all profile fields (for enabling/disabling together)
        allFields = listOf(
            findViewById(R.id.fullNameField),
            findViewById(R.id.phoneCodeDropdown),
            findViewById(R.id.phoneNumberField),
            findViewById(R.id.israeliPhoneField),
            findViewById(R.id.emailField),
            findViewById(R.id.birthDateField),
            findViewById(R.id.countryField),
            findViewById(R.id.personalNumberField),
            findViewById(R.id.idNumberField),
            findViewById(R.id.addressField),
            findViewById(R.id.livingWithField),
            findViewById(R.id.enlistmentDateField),
            findViewById(R.id.releaseDateField),
            findViewById(R.id.roleField),
            findViewById(R.id.baseField),
            findViewById(R.id.statusField),
            findViewById(R.id.armyContactField),
            findViewById(R.id.emergencyAbroadField),
            findViewById(R.id.emergencyIsraelField)
        )

        // Disable editing by default
        setEditing(false)

        // Attach DatePicker only to date fields
        setupDatePicker(findViewById(R.id.birthDateField))
        setupDatePicker(findViewById(R.id.enlistmentDateField))
        setupDatePicker(findViewById(R.id.releaseDateField))

        // Dropdown for phone code selection
        val phoneCodeDropdown = findViewById<MaterialAutoCompleteTextView>(R.id.phoneCodeDropdown)
        val phoneCodes = listOf("+972", "+1", "+33", "+32", "+49", "+7","+61")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, phoneCodes)
        phoneCodeDropdown.setAdapter(adapter)
        phoneCodeDropdown.setText("+972", false) // Default selection

        // Load current user data from Firestore
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    findViewById<TextInputEditText>(R.id.fullNameField).setText(document.getString("fullName"))
                    findViewById<TextInputEditText>(R.id.phoneNumberField).setText(document.getString("phoneNumber"))
                    findViewById<TextInputEditText>(R.id.birthDateField).setText(document.getString("birthDate"))
                    findViewById<TextInputEditText>(R.id.countryField).setText(document.getString("country"))
                    findViewById<TextInputEditText>(R.id.personalNumberField).setText(document.getString("personalNumber"))
                    findViewById<TextInputEditText>(R.id.idNumberField).setText(document.getString("idNumber"))
                    findViewById<TextInputEditText>(R.id.emailField).setText(document.getString("email"))
                    findViewById<TextInputEditText>(R.id.addressField).setText(document.getString("address"))
                    findViewById<TextInputEditText>(R.id.livingWithField).setText(document.getString("livingWith"))
                    findViewById<TextInputEditText>(R.id.enlistmentDateField).setText(document.getString("enlistmentDate"))
                    findViewById<TextInputEditText>(R.id.releaseDateField).setText(document.getString("releaseDate"))
                    findViewById<TextInputEditText>(R.id.roleField).setText(document.getString("role"))
                    findViewById<TextInputEditText>(R.id.baseField).setText(document.getString("base"))
                    findViewById<TextInputEditText>(R.id.statusField).setText(document.getString("armyStage"))
                    findViewById<TextInputEditText>(R.id.armyContactField).setText(document.getString("armyContact"))
                    findViewById<TextInputEditText>(R.id.emergencyAbroadField).setText(document.getString("emergencyAbroad"))
                    findViewById<TextInputEditText>(R.id.emergencyIsraelField).setText(document.getString("emergencyIsrael"))
                    findViewById<TextInputEditText>(R.id.israeliPhoneField).setText(document.getString("israeliPhone"))
                }
            }
        }

        // Edit button - enable all fields
        editButton.setOnClickListener {
            isEditing = true
            setEditing(true)
        }

        // Save button - validate and update Firestore
        saveButton.setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.emailField).text.toString()
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = hashMapOf(
                "fullName" to findViewById<TextInputEditText>(R.id.fullNameField).text.toString(),
                "phoneNumber" to findViewById<TextInputEditText>(R.id.phoneNumberField).text.toString(),
                "birthDate" to findViewById<TextInputEditText>(R.id.birthDateField).text.toString(),
                "country" to findViewById<TextInputEditText>(R.id.countryField).text.toString(),
                "personalNumber" to findViewById<TextInputEditText>(R.id.personalNumberField).text.toString(),
                "idNumber" to findViewById<TextInputEditText>(R.id.idNumberField).text.toString(),
                "email" to email,
                "address" to findViewById<TextInputEditText>(R.id.addressField).text.toString(),
                "livingWith" to findViewById<TextInputEditText>(R.id.livingWithField).text.toString(),
                "enlistmentDate" to findViewById<TextInputEditText>(R.id.enlistmentDateField).text.toString(),
                "releaseDate" to findViewById<TextInputEditText>(R.id.releaseDateField).text.toString(),
                "role" to findViewById<TextInputEditText>(R.id.roleField).text.toString(),
                "base" to findViewById<TextInputEditText>(R.id.baseField).text.toString(),
                "armyStage" to findViewById<TextInputEditText>(R.id.statusField).text.toString(),
                "armyContact" to findViewById<TextInputEditText>(R.id.armyContactField).text.toString(),
                "emergencyAbroad" to findViewById<TextInputEditText>(R.id.emergencyAbroadField).text.toString(),
                "emergencyIsrael" to findViewById<TextInputEditText>(R.id.emergencyIsraelField).text.toString(),
                "israeliPhone" to findViewById<TextInputEditText>(R.id.israeliPhoneField).text.toString()
            )

            if (userId != null) {
                db.collection("users").document(userId).set(data)
                    .addOnSuccessListener {
                        // Successfully updated profile
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        isEditing = false
                        setEditing(false)
                    }
                    .addOnFailureListener { e ->
                        // Log error and show toast
                        Log.e("Update_Profile", "Firebase error: ${e.message}")
                        Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Enables/disables all fields and toggles button visibility
    private fun setEditing(enabled: Boolean) {
        allFields.forEach { it.isEnabled = enabled }
        editButton.visibility = if (enabled) Button.GONE else Button.VISIBLE
        saveButton.visibility = if (enabled) Button.VISIBLE else Button.GONE
    }

    // Sets up date picker for a specific EditText
    private fun setupDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            editText.setText(dateFormat.format(calendar.time))
        }

        // Open date picker only if field is editable
        editText.setOnClickListener {
            if (editText.isEnabled) {
                DatePickerDialog(
                    this,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }
}
