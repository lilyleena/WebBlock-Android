package com.kshrd.features.business.presentation.components

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val timeFormat = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

private const val MINUTES_PER_HOUR = 60

/** 540 -> "9:00 AM" */
fun formatMinutes(minutes: Int): String = LocalTime.of(minutes / MINUTES_PER_HOUR, minutes % MINUTES_PER_HOUR).format(timeFormat)

fun dayName(day: DayOfWeek): String = day.getDisplayName(TextStyle.FULL, Locale.getDefault())
