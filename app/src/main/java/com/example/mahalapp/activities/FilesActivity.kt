package com.example.mahalapp.activities

import android.app.DownloadManager
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mahalapp.adapters.FilesAdapter
import com.example.mahalapp.models.MyFile
import com.example.mahalapp.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// Activity for displaying and downloading files from Firestore & Firebase Storage
class FilesActivity : BaseActivity() {

    private lateinit var filesRecycler: RecyclerView
    private lateinit var adapter: FilesAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBaseContent(R.layout.activity_files)

        // Setup RecyclerView
        filesRecycler = findViewById(R.id.filesRecycler)
        filesRecycler.layoutManager = LinearLayoutManager(this)

        // Load files from Firestore
        loadFilesFromFirestore()
    }

    private fun loadFilesFromFirestore() {
        // Get the "files" collection from Firestore
        firestore.collection("files")
            .get()
            .addOnSuccessListener { snapshot ->
                // Map Firestore documents to MyFile objects
                val filesList = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name")
                    val path = doc.getString("storagePath")
                    val mime = doc.getString("mimeType") ?: "application/pdf"
                    if (!name.isNullOrEmpty() && !path.isNullOrEmpty()) {
                        MyFile(name, path, mime)
                    } else null
                }
                // Set adapter with click listener for downloading
                adapter = FilesAdapter(filesList) { fileItem ->
                    downloadFile(fileItem)
                }
                filesRecycler.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "שגיאה בטעינת מסמכים: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun downloadFile(fileItem: MyFile) {
        // Get reference to file in Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference.child(fileItem.storagePath)

        // Get a download URL for the file
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            // Configure DownloadManager request
            val request = DownloadManager.Request(uri)
                .setTitle(fileItem.name)
                .setDescription("מוריד קובץ...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileItem.name + ".pdf")
                .setMimeType(fileItem.mimeType)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            // Start download
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            Toast.makeText(this, "מוריד קובץ...", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "שגיאה בקבלת קובץ", Toast.LENGTH_LONG).show()
        }
    }
}
