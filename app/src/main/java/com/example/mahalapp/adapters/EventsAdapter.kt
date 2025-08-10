package com.example.mahalapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mahalapp.models.Event
import com.example.mahalapp.R

// Adapter for displaying a list of events in a RecyclerView
class EventsAdapter(
    private var events: MutableList<Event>,          // List of events to display
    private val onClick: (Event) -> Unit              // Callback when an event is clicked
) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    // ViewHolder: Holds references to the views for each event item
    inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvEventName)
        val description: TextView = view.findViewById(R.id.tvEventDesc)

        // Bind event data to UI
        fun bind(event: Event) {
            name.text = event.name
            description.text = event.description ?: "" // If no description, show empty
            itemView.setOnClickListener { onClick(event) }
        }
    }

    // Inflate the item layout and create the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    // Bind data to a specific position in the list
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    // Return number of items
    override fun getItemCount(): Int = events.size

    // Replace the current list of events and refresh UI
    fun submit(newEvents: List<Event>) {
        events.clear()
        events.addAll(newEvents)
        notifyDataSetChanged()
    }
}
