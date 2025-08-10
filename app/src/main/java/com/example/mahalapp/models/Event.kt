package com.example.mahalapp.models

import com.google.firebase.Timestamp
import java.util.Date

// Data model for an event
data class Event(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val date: Date = Date(),
    val endDate: Date? = null           // Optional end date (for multi-day events)
) {
    companion object {
        // Convert a Firestore document map into an Event object
        fun fromMap(id: String, map: Map<String, Any?>): Event {
            val start = (map["date"] as? Timestamp)?.toDate() ?: Date()
            val end = (map["endDate"] as? Timestamp)?.toDate()
            return Event(
                id = id,
                name = map["name"] as? String ?: "",
                description = map["description"] as? String ?: "",
                date = start,
                endDate = end
            )
        }
    }
}
