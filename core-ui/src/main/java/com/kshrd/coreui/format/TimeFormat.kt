package com.kshrd.coreui.format

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** "2 days ago", "Yesterday"-style relative time for a past instant. */
fun formatRelativeTime(
    epochMillis: Long,
    nowMillis: Long = System.currentTimeMillis(),
): String = DateUtils.getRelativeTimeSpanString(epochMillis, nowMillis, DateUtils.MINUTE_IN_MILLIS).toString()

/** "18 Oct 2026, 2:15 PM" */
fun formatDateTime(epochMillis: Long): String = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()).format(Date(epochMillis))

/** "$38.00" */
fun formatPrice(amount: Double): String = String.format(Locale.US, "$%.2f", amount)
