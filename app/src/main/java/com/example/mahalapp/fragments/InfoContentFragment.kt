package com.example.mahalapp.fragments

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mahalapp.models.MyFile
import com.example.mahalapp.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// Fragment that loads and displays detailed information pages from Firestore
class InfoContentFragment : Fragment() {

    private lateinit var infoContainer: LinearLayout   // Container for dynamic content
    private val db = FirebaseFirestore.getInstance()
    private lateinit var category: String

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate layout and setup back button
        val view = inflater.inflate(R.layout.fragment_info_content, container, false)
        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }

        // Initialize UI and get category from arguments
        infoContainer = view.findViewById(R.id.infoContainer)
        category = arguments?.getString("category") ?: "unknown"
        loadInformation(category)

        return view
    }

    // Fetch and display content from Firestore based on category
    private fun loadInformation(category: String) {
        db.collection("info_pages").document(category)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    when (category) {
                        "housing" -> {
                            val content = document.getString("content") ?: "אין מידע להצגה."
                            displayHousingFromText(content)
                        }
                        "health" -> {
                            val content = document.getString("content") ?: "אין מידע להצגה."
                            displayHealthFromText(content)
                        }
                        "aliyah" -> {
                            val content = document.getString("content") ?: "אין מידע להצגה."
                            displayAliyahFromText(content)
                        }
                        "rights" -> {
                            val title = document.getString("title") ?: ""
                            val filesList = document.get("files") as? List<Map<String, Any>> ?: emptyList()

                            Log.d("RightsDebug", "מספר קבצים שהתקבלו: ${filesList.size}")
                            filesList.forEachIndexed { index, map ->
                                Log.d("RightsDebug", "קובץ #$index: map=$map")
                            }

                            // Convert Firestore maps to MyFile objects
                            val fileObjects = filesList.map { f ->
                                val fixedMap = f.mapKeys { it.key.trim() } // Remove extra spaces from keys
                                MyFile(
                                    name = fixedMap["name"]?.toString() ?: "קובץ ללא שם",
                                    storagePath = fixedMap["storagePath"]?.toString() ?: "",
                                    mimeType = fixedMap["mimeType"]?.toString() ?: "application/pdf"
                                )
                            }

                            // Display file cards on screen
                            displayRightsFiles(fileObjects, title)
                        }
                        else -> {
                            val content = document.getString("content") ?: "אין מידע להצגה."
                            displayFormattedContent(content)
                        }
                    }
                } else {
                    displayFormattedContent("אין מידע להצגה.")
                }
            }
            .addOnFailureListener {
                displayFormattedContent("שגיאה בטעינה.")
            }
    }

    // Default text display for simple categories
    private fun displayFormattedContent(content: String) {
        infoContainer.removeAllViews()
        val lines = content.split("\n\n")
        lines.forEach { paragraph ->
            val tv = TextView(requireContext()).apply {
                text = paragraph
                textSize = 16f
                setTextColor(Color.DKGRAY)
            }
            infoContainer.addView(tv)
        }
    }

    // -------- HOUSING CONTENT --------
    private fun displayHousingFromText(rawText: String) {
        val parts = rawText.split("\n\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (parts.isEmpty()) return

        // First part is a header
        val headerTitle = parts[0]
        addHeaderTitle(headerTitle)

        // Remaining parts are housing cards
        val cards = parts.drop(1)
        for (cardText in cards) {
            val lines = cardText.lines().map { it.trim() }.filter { it.isNotBlank() }
            if (lines.isEmpty()) continue

            val title = lines.first()
            val address = lines.find { it.contains("כתובת") }
            val contact = lines.find { it.contains("יצירת קשר") }
            val link = lines.find { it.contains("http") || it.contains("www.") }
            val details = lines.drop(1).filterNot {
                it.contains("כתובת") || it.contains("יצירת קשר") || it.contains("http")
            }

            addHousingCard(
                title = title,
                details = details.joinToString("\n"),
                address = address?.substringAfter(":")?.trim(),
                contact = contact?.substringAfter(":")?.trim(),
                link = link
            )
        }
    }

    // Create a styled card for housing info
    private fun addHousingCard(
        title: String,
        details: String? = null,
        address: String? = null,
        contact: String? = null,
        link: String? = null
    ) {
        val context = requireContext()
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundResource(R.drawable.card_background)
            gravity = Gravity.END
            elevation = 8f
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val titleView = TextView(context).apply {
            text = "\u200F🏠 $title"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#3B3B98"))
            setPadding(0, 0, 0, 16)
            textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            typeface = ResourcesCompat.getFont(context, R.font.rubik_bold)
        }
        card.addView(titleView)

        details?.let { card.addView(makeLine("\u200Fℹ️ $it")) }
        address?.let { card.addView(makeLine("\u200F📍 $address")) }
        contact?.let { card.addView(makeLine("\u200F📞 $contact")) }
        link?.let { card.addView(makeLine("\u200F🔗 $link")) }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, 0, 32) }

        card.layoutParams = params
        infoContainer.addView(card)
    }

    // Create a styled text line
    private fun makeLine(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = "\u200F$text"
            textSize = 16f
            setTextColor(Color.parseColor("#333333"))
            setPadding(0, 8, 0, 0)
            textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            typeface = ResourcesCompat.getFont(context, R.font.assistant_regular)
            autoLinkMask = Linkify.WEB_URLS
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    // Create a styled header title
    private fun addHeaderTitle(title: String) {
        val header = TextView(requireContext()).apply {
            text = "\u200F $title"
            textSize = 24f
            setTextColor(Color.parseColor("#1E3A8A"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 16, 0, 32)
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            gravity = Gravity.CENTER
            typeface = ResourcesCompat.getFont(requireContext(), R.font.rubik_bold)
        }
        infoContainer.addView(header)
    }

    // -------- HEALTH CONTENT --------
    private fun displayHealthFromText(rawText: String) {
        infoContainer.removeAllViews()
        val context = requireContext()

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return

        val title = lines.first()
        val body = lines.drop(1)

        val rubikFont = ResourcesCompat.getFont(context, R.font.rubik_bold)
        val assistantFont = ResourcesCompat.getFont(context, R.font.assistant_regular)

        // Questions
        val questions = body.takeWhile { it.endsWith("?") }
        questions.forEach { question ->
            val qView = TextView(context).apply {
                text = "\u200F$question"
                textSize = 14f
                setTextColor(Color.parseColor("#374151"))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER
                typeface = rubikFont
                setPadding(0, 4, 0, 4)
            }
            infoContainer.addView(qView)
        }

        // Exclamation-mark answers
        val afterQuestions = body.drop(questions.size)
        val exclamations = afterQuestions.takeWhile { it.contains("!") }
        exclamations.forEach { line ->
            val exView = TextView(context).apply {
                text = "\u200F$line"
                textSize = 16f
                setTextColor(Color.parseColor("#D32F2F"))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER
                typeface = rubikFont
                setPadding(0, 12, 0, 12)
            }
            infoContainer.addView(exView)
        }

        // Company info cards
        val companyLines = afterQuestions.drop(exclamations.size)
        val grouped = companyLines.chunked(3)
        grouped.forEach { group ->
            if (group.size == 3) {
                val (companyName, phone, link) = group

                val logoRes = when {
                    companyName.contains("הראל") -> R.drawable.harel_logo
                    companyName.contains("מנורה") -> R.drawable.menora_logo
                    else -> 0
                }

                val card = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(40, 40, 40, 40)
                    setBackgroundResource(R.drawable.card_background)
                    gravity = Gravity.END
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 16, 0, 32) }
                }

                // Title layout with optional logo
                val titleLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.END
                    layoutDirection = View.LAYOUT_DIRECTION_RTL
                }
                if (logoRes != 0) {
                    val logoView = ImageView(context).apply {
                        setImageResource(logoRes)
                        layoutParams = LinearLayout.LayoutParams(150, 150).apply { leftMargin = 16 }
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    }
                    card.addView(titleLayout)
                    titleLayout.addView(logoView)
                }

                val companyView = TextView(context).apply {
                    text = "\u200F$companyName"
                    textSize = 15f
                    setTextColor(Color.parseColor("#1E3A8A"))
                    typeface = rubikFont
                    textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                }
                titleLayout.addView(companyView)

                val phoneView = TextView(context).apply {
                    text = "\u200F📞 ${phone.replace("טלפון", "").replace(":", "").trim()}"
                    textSize = 16f
                    setTextColor(Color.DKGRAY)
                    textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                    typeface = assistantFont
                    setPadding(0, 12, 0, 0)
                }
                card.addView(phoneView)

                val linkView = TextView(context).apply {
                    text = HtmlCompat.fromHtml(
                        "\u200F🌐 לאתר החברה : <a href=\"$link\">לחץ כאן</a>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                    movementMethod = LinkMovementMethod.getInstance()
                    textSize = 16f
                    setTextColor(Color.DKGRAY)
                    textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                    typeface = assistantFont
                    setPadding(0, 8, 0, 0)
                }
                card.addView(linkView)

                infoContainer.addView(card)
            }
        }
    }

    // -------- ALIYAH CONTENT --------
    private fun addAliyahCard(title: String, contact: String) {
        val context = requireContext()
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundResource(R.drawable.card_background)
            gravity = Gravity.END
            elevation = 8f
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val titleView = TextView(context).apply {
            text = "\u200F⭐ $title"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#3B3B98"))
            setPadding(0, 0, 0, 16)
            textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            typeface = ResourcesCompat.getFont(context, R.font.rubik_bold)
        }
        card.addView(titleView)

        card.addView(makeLine("\u200F📞 $contact"))

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, 0, 32) }

        card.layoutParams = params
        infoContainer.addView(card)
    }

    private fun displayAliyahFromText(rawText: String) {
        infoContainer.removeAllViews()
        val parts = rawText.split("\n\n").map { it.trim() }.filter { it.isNotBlank() }
        if (parts.isEmpty()) return

        val headerTitle = parts[0]
        addHeaderTitle(headerTitle)

        val cards = parts.drop(1)
        for (cardText in cards) {
            val lines = cardText.lines().map { it.trim() }.filter { it.isNotBlank() }
            if (lines.size >= 2) {
                val title = lines[0]
                val phone = lines[1].replace("טלפון", "").replace(":", "").trim()
                addAliyahCard(title, phone)
            }
        }
    }

    // -------- RIGHTS CONTENT --------
    private fun displayRightsFiles(files: List<MyFile>, title: String) {
        infoContainer.removeAllViews()

        // Optional top title
        if (title.isNotBlank()) {
            addHeaderTitle(title)
        }

        files.forEach { file ->
            val card = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(40, 40, 40, 40)
                setBackgroundResource(R.drawable.card_background)
                gravity = Gravity.CENTER_VERTICAL
                layoutDirection = View.LAYOUT_DIRECTION_RTL
                elevation = 8f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 32) }
            }

            val nameView = TextView(requireContext()).apply {
                text = file.name.ifBlank { "קובץ ללא שם" }
                textSize = 16f
                setTextColor(Color.parseColor("#3B3B98"))
                typeface = ResourcesCompat.getFont(requireContext(), R.font.rubik_bold)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val icon = ImageView(requireContext()).apply {
                setImageResource(R.drawable.ic_download)
                layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                    marginStart = 24
                }
            }

            card.addView(nameView)
            card.addView(icon)

            // Click on card downloads file
            card.setOnClickListener {
                downloadFile(file)
            }

            infoContainer.addView(card)
        }
    }

    // Handle file downloading from Firebase Storage
    private fun downloadFile(fileItem: MyFile) {
        if (fileItem.storagePath.isBlank()) {
            Toast.makeText(requireContext(), "לא נמצא נתיב לקובץ", Toast.LENGTH_LONG).show()
            return
        }

        val storageRef = if (fileItem.storagePath.startsWith("http")) {
            FirebaseStorage.getInstance().getReferenceFromUrl(fileItem.storagePath)
        } else {
            FirebaseStorage.getInstance().getReference(fileItem.storagePath)
        }

        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                val request = DownloadManager.Request(uri)
                    .setTitle(fileItem.name.ifBlank { "קובץ" })
                    .setDescription("מוריד קובץ...")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        "${fileItem.name.ifBlank { "קובץ" }}.pdf"
                    )
                    .setMimeType(fileItem.mimeType.ifBlank { "application/pdf" })
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

                val dm = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)

                Toast.makeText(requireContext(), "מוריד קובץ...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("RightsDebug", "שגיאה בקבלת קובץ: ${e.message}", e)
                Toast.makeText(requireContext(), "שגיאה בקבלת קובץ", Toast.LENGTH_LONG).show()
            }
    }
}
