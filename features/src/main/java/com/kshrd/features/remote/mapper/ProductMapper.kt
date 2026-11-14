package com.kshrd.features.remote.mapper

import com.kshrd.features.products.domain.model.Product
import com.kshrd.features.remote.dto.ProductDto
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.math.BigDecimal
import java.math.RoundingMode

private const val PRICE_DECIMALS = 2

/** What a save does to the product's photo (`imageUrl` in the body). */
sealed interface PhotoChange {
    /** `imageUrl` left out: the photo stays. */
    data object Keep : PhotoChange

    /** `imageUrl: null`: the photo is removed. */
    data object Remove : PhotoChange

    /** A URL just returned by `POST /media` (the server accepts only this website's media URLs). */
    data class Set(
        val url: String,
    ) : PhotoChange
}

/** docs/API-MAPPING.md 2.4. A `null` (or unreadable) price is a hidden price (G7). */
fun ProductDto.toProduct(): Product {
    val amount = price?.toBigDecimalOrNull()
    return Product(
        id = id,
        name = name,
        category = category,
        price = amount?.toDouble() ?: 0.0,
        description = description,
        imageUrl = imageUrl.orEmpty(),
        version = version,
        isPriceHidden = amount == null,
    )
}

/** "6.00": a dot and two decimals, never grouped, whatever the device's region. */
fun priceText(amount: Double): String = BigDecimal.valueOf(amount).setScale(PRICE_DECIMALS, RoundingMode.HALF_UP).toPlainString()

/**
 * The body of `POST /products` and `PUT /products/{id}`. [price] `null` keeps (or makes) the price hidden.
 * [version] is sent on an update so the server can answer 409 when the product changed elsewhere.
 * A blank description is left out: Project refuses `""` (400 FIELD_LENGTH_INVALID, AWS run 14 Nov 2026), and the
 * contract doesn't require the field.
 */
fun productBody(
    name: String,
    description: String,
    category: String,
    price: Double?,
    photo: PhotoChange,
    version: Long?,
): JsonObject =
    buildJsonObject {
        put("name", name)
        if (description.isNotBlank()) put("description", description)
        put("category", category)
        put("price", price?.let(::priceText))
        when (photo) {
            PhotoChange.Keep -> Unit
            PhotoChange.Remove -> put("imageUrl", JsonNull)
            is PhotoChange.Set -> put("imageUrl", photo.url)
        }
        version?.let { put("version", it) }
    }
