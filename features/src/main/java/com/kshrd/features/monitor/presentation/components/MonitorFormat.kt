package com.kshrd.features.monitor.presentation.components

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val SECONDS_IN_MINUTE = 60

/** "1m 41s" (seconds padded like the iOS sample data, so 69 becomes "1m 09s"). */
fun formatDeployDuration(seconds: Int): String = String.format(Locale.US, "%dm %02ds", seconds / SECONDS_IN_MINUTE, seconds % SECONDS_IN_MINUTE)

/** "1m 28s" for the average tile (not padded, as on iOS). */
fun formatAverageDuration(seconds: Int): String = "${seconds / SECONDS_IN_MINUTE}m ${seconds % SECONDS_IN_MINUTE}s"

private fun formatter(pattern: String): DateTimeFormatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())

private fun Long.format(pattern: String): String = formatter(pattern).format(Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()))

/** "Oct 18, 2026 at 2:15 PM" */
fun formatDeployDate(millis: Long): String = millis.format("MMM d, yyyy 'at' h:mm a")

/** "Oct 18, 2:15 PM" */
fun formatActivityDate(millis: Long): String = millis.format("MMM d, h:mm a")

/** "October 18, 2026 at 2:15 PM" */
fun formatActivityDateLong(millis: Long): String = millis.format("MMMM d, yyyy 'at' h:mm a")
