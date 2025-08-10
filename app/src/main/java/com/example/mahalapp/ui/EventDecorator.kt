package com.example.mahalapp.ui

import android.graphics.Color
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

// Adds a green dot under specific calendar days
class EventDecorator(
    private val dates: Set<CalendarDay> // Dates to decorate
) : DayViewDecorator {

    // Check if this day should be decorated
    override fun shouldDecorate(day: CalendarDay) = day in dates

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, Color.parseColor("#006400")))
    }
}
