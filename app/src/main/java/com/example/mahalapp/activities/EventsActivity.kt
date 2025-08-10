package com.example.mahalapp.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mahalapp.ui.AddEventBottomSheet
import com.example.mahalapp.models.Event
import com.example.mahalapp.ui.EventDecorator
import com.example.mahalapp.adapters.EventsAdapter
import com.example.mahalapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.util.Calendar
import java.util.Date

class EventsActivity : BaseActivity() {

    private lateinit var emptyView: TextView              // Message when no events for selected day
    private lateinit var emptyMonthView: TextView         // Message when no events in month
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var recycler: RecyclerView

    private lateinit var adapter: EventsAdapter
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var monthHasEvents = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBaseContent(R.layout.activity_events)

        emptyView      = findViewById(R.id.eventsEmpty)
        emptyMonthView = findViewById(R.id.eventsMonthEmpty)
        calendarView   = findViewById(R.id.calendarView)
        recycler       = findViewById(R.id.eventsRecycler)

        adapter = EventsAdapter(mutableListOf()) { e ->
            Toast.makeText(this, e.name, Toast.LENGTH_SHORT).show()
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        // Calendar settings: start week on Sunday
        calendarView.state().edit()
            .setFirstDayOfWeek(Calendar.SUNDAY)
            .commit()

        // weekdays
        calendarView.setWeekDayFormatter { dayOfWeek ->
            when (dayOfWeek) {
                Calendar.SUNDAY    -> "א'"
                Calendar.MONDAY    -> "ב'"
                Calendar.TUESDAY   -> "ג'"
                Calendar.WEDNESDAY -> "ד'"
                Calendar.THURSDAY  -> "ה'"
                Calendar.FRIDAY    -> "ו'"
                Calendar.SATURDAY  -> "ש'"
                else -> ""
            }
        }

        // On day selected → load that day's events
        calendarView.setOnDateChangedListener { _, date, _ ->
            adapter.submit(emptyList())
            recycler.visibility = View.GONE
            emptyView.visibility = View.GONE
            loadDayEvents(date.year, date.month, date.day)
        }

        // On month changed - load dots & empty-month message
        calendarView.setOnMonthChangedListener { _, date ->
            adapter.submit(emptyList())
            recycler.visibility = View.GONE
            emptyView.visibility = View.GONE
            loadEventDaysForMonth(date.year, date.month)
        }

        // Floating Action Button - add new event
        findViewById<FloatingActionButton>(R.id.fabAddEvent)
            ?.setOnClickListener {
                AddEventBottomSheet().show(supportFragmentManager, "AddEventBottomSheet")
            }
    }

    override fun onStart() {
        super.onStart()

        // Select today's date
        val today = CalendarDay.today()
        calendarView.currentDate = today
        calendarView.setCurrentDate(today, false)
        calendarView.clearSelection()
        calendarView.setDateSelected(today, true)

        // Load events for today and dots for current month
        loadDayEvents(today.year, today.month, today.day)
        loadEventDaysForMonth(today.year, today.month )
    }

    private fun loadDayEvents(y: Int, m0: Int, d: Int) {
        // Hide views and clear list
        emptyView.visibility = View.GONE
        recycler.visibility = View.GONE
        adapter.submit(emptyList())

        val start = dayStart(y, m0, d)
        val end   = dayEnd(y, m0, d)

        db.collection("events")
            .get() // Load all events to also handle multi-day spans
            .addOnSuccessListener { snap ->
                val events = snap.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val startDate = doc.getTimestamp("date")?.toDate()
                    val endDate = doc.getTimestamp("endDate")?.toDate() ?: startDate

                    // Include if selected day is between startDate and endDate
                    if (startDate != null && endDate != null &&
                        !(endDate.before(start) || startDate.after(end))) {
                        Event.Companion.fromMap(doc.id, data)
                    } else null
                }.sortedBy { it.date }

                adapter.submit(events)

                if (events.isEmpty()) {
                    recycler.visibility = View.GONE
                    emptyView.visibility = if (monthHasEvents) View.VISIBLE else View.GONE
                } else {
                    recycler.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                recycler.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
                Toast.makeText(this, "שגיאה בטעינת אירועים: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadEventDaysForMonth(year: Int, monthZeroBased: Int) {
        val start = monthStart(year, monthZeroBased)
        val end = monthEnd(year, monthZeroBased)

        db.collection("events")
            .get() // Load all events, filter locally
            .addOnSuccessListener { snap ->
                val days = mutableSetOf<CalendarDay>()

                for (doc in snap.documents) {
                    val startDate = doc.getTimestamp("date")?.toDate()
                    val endDate = doc.getTimestamp("endDate")?.toDate() ?: startDate

                    if (startDate != null && endDate != null) {
                        // Check if event overlaps with current month
                        if (!(endDate.before(start) || startDate.after(end))) {
                            val cal = Calendar.getInstance().apply { time = startDate }
                            val calEnd = Calendar.getInstance().apply { time = endDate }

                            // Add all days in range to the set
                            while (!cal.after(calEnd)) {
                                days.add(CalendarDay.from(
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ))
                                cal.add(Calendar.DAY_OF_MONTH, 1)
                            }
                        }
                    }
                }

                monthHasEvents = days.isNotEmpty()
                emptyMonthView.visibility = if (!monthHasEvents) View.VISIBLE else View.GONE

                // Refresh decorators (dots) on calendar
                calendarView.removeDecorators()
                calendarView.addDecorator(EventDecorator(days))
            }
    }

    // Helper: start of day
    private fun dayStart(y: Int, m0: Int, d: Int): Date =
        Calendar.getInstance().apply {
            set(y, m0, d, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

    // Helper: end of day
    private fun dayEnd(y: Int, m0: Int, d: Int): Date =
        Calendar.getInstance().apply {
            set(y, m0, d, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

    // Helper: start of month
    private fun monthStart(y: Int, m0: Int): Date =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m0)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

    // Helper: end of month
    private fun monthEnd(y: Int, m0: Int): Date =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m0)
            val last = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, last)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
}
