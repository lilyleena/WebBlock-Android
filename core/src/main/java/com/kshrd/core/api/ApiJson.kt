package com.kshrd.core.api

import kotlinx.serialization.json.Json

/**
 * The JSON settings for the WebBlock v2 API (docs/API-MAPPING.md section 1).
 *
 * - Unknown fields are skipped, so an additive contract change never breaks the app.
 * - Defaults are written (`stayLoggedIn: true` must reach the server) and `null` is written as `null`
 *   (for example `mapsUrl: null`). Bodies that must leave a field out are built as a `JsonObject`.
 */
val ApiJson: Json =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = true
    }
