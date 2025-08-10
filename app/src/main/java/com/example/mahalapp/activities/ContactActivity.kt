package com.example.mahalapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.mahalapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ContactActivity : BaseActivity() {

    // UI elements
    private lateinit var editTextSubject: EditText
    private lateinit var editTextMessage: EditText
    private lateinit var buttonChooseFile: Button
    private lateinit var buttonSend: Button
    private lateinit var textViewFileName: TextView
    private lateinit var textViewStatus: TextView

    // Upload progress views
    private lateinit var progressLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textProgressPercent: TextView

    // File selection
    private var fileUri: Uri? = null
    private val PICK_FILE_REQUEST = 100

    // Firebase references
    private val firestore = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBaseContent(R.layout.activity_contact)

        // Initialize UI components
        editTextSubject = findViewById(R.id.editTextSubject)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonChooseFile = findViewById(R.id.buttonChooseFile)
        buttonSend = findViewById(R.id.buttonSend)
        textViewFileName = findViewById(R.id.textViewFileName)
        textViewStatus = findViewById(R.id.textViewStatus)

        progressLayout = findViewById(R.id.uploadProgressLayout)
        progressBar = findViewById(R.id.progressBarUpload)
        textProgressPercent = findViewById(R.id.textViewProgressPercent)

        // Set button listeners
        buttonChooseFile.setOnClickListener { openFilePicker() }
        buttonSend.setOnClickListener { sendRequest() }
    }

    private fun openFilePicker() {
        // Launch a file picker for PDF or image files
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/*"))
        }
        startActivityForResult(Intent.createChooser(intent, "בחר קובץ"), PICK_FILE_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Handle file selection result
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK) {
            fileUri = data?.data
            textViewFileName.text = fileUri?.lastPathSegment ?: "קובץ נבחר"
        }
    }

    private fun sendRequest() {
        // Get subject and message text
        val subject = editTextSubject.text.toString().trim()
        val message = editTextMessage.text.toString().trim()

        // Validate fields
        if (subject.isEmpty() || message.isEmpty()) {
            textViewStatus.text = "אנא מלא נושא ותוכן פנייה"
            return
        }

        textViewStatus.text = "שולח פנייה..."
        val requestId = UUID.randomUUID().toString()

        // Prepare request data
        val requestData = hashMapOf(
            "subject" to subject,
            "message" to message,
            "userId" to auth.currentUser?.uid,
            "timestamp" to FieldValue.serverTimestamp(),
            "fileUrl" to ""
        )

        // If file is selected, upload it first
        if (fileUri != null) {
            val fileRef = storageRef.child("requests_files/$requestId")
            progressLayout.visibility = View.VISIBLE
            progressBar.progress = 0
            textProgressPercent.text = "0%"

            fileRef.putFile(fileUri!!)
                .addOnProgressListener { taskSnapshot ->
                    // Update progress bar and percentage
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    progressBar.progress = progress
                    textProgressPercent.text = "$progress%"
                }
                .addOnSuccessListener {
                    // Get file download URL and save request to Firestore
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        requestData["fileUrl"] = uri.toString()
                        saveRequestToFirestore(requestId, requestData)
                    }
                }
                .addOnFailureListener {
                    // Upload failed
                    progressLayout.visibility = View.GONE
                    textViewStatus.text = "שגיאה בהעלאת הקובץ"
                }
        } else {
            // No file attached, save request directly
            saveRequestToFirestore(requestId, requestData)
        }
    }

    private fun saveRequestToFirestore(requestId: String, requestData: HashMap<String, Any?>) {
        // Save request document to Firestore
        firestore.collection("requests").document(requestId)
            .set(requestData)
            .addOnSuccessListener {
                // Reset UI after success
                progressLayout.visibility = View.GONE
                textViewStatus.text = "הפנייה נשלחה בהצלחה!"
                editTextSubject.text.clear()
                editTextMessage.text.clear()
                textViewFileName.text = "No file selected"
                fileUri = null
            }
            .addOnFailureListener {
                // Failed to save
                progressLayout.visibility = View.GONE
                textViewStatus.text = "שגיאה בשליחת הפנייה"
            }
    }
}
