package com.example.mahalapp.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.mahalapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AddEventBottomSheet : BottomSheetDialogFragment() {

    // Firestore database instance
    private val db by lazy { FirebaseFirestore.getInstance() }
    // To store the selected date from the date picker
    private var selectedDate: Calendar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottomsheet_add_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val etName: TextInputEditText = view.findViewById(R.id.etEventName)
        val etDesc: TextInputEditText = view.findViewById(R.id.etEventDesc)
        val etDate: TextInputEditText = view.findViewById(R.id.date)
        val btnSave: View = view.findViewById(R.id.btnSaveEvent)

        // Show DatePicker when clicking the date field
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dpd = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    // Save the selected date at 09:00 with milliseconds reset
                    selectedDate = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth, 9, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    // Display the selected date in dd/MM/yyyy format
                    etDate.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show()
        }

        // save button
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val desc = etDesc.text.toString().trim()

            // Validate required fields
            if (name.isEmpty()) {
                etName.error = "שדה חובה" // Required field
                return@setOnClickListener
            }
            if (selectedDate == null) {
                etDate.error = "בחר תאריך" // Select date
                return@setOnClickListener
            }

            // Prepare event data for Firestore
            val data = hashMapOf(
                "name" to name,
                "description" to desc,
                "date" to Timestamp(selectedDate!!.time)
            )

            // Save event to Firestore
            db.collection("events")
                .add(data)
                .addOnSuccessListener {
                    // Show success message and close bottom sheet
                    Toast.makeText(requireContext(), "האירוע נוסף", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                .addOnFailureListener { e ->
                    // Show error message
                    Toast.makeText(requireContext(), "שגיאה: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
