package com.kshrd.features.remote.mapper

import java.time.OffsetDateTime

/**
 * An ISO 8601 time from the API ("2026-11-10T09:34:12Z", with or without fractions or an offset) as epoch millis;
 * `null` when absent or unreadable.
 */
fun parseApiTime(text: String?): Long? = text?.let { runCatching { OffsetDateTime.parse(it).toInstant().toEpochMilli() }.getOrNull() }
