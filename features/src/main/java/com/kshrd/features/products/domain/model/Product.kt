package com.kshrd.features.products.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val description: String,
    val imageUrl: String,
    /** The server's version for optimistic locking; `null` for a product never loaded from the API (saved with POST). */
    val version: Long? = null,
    /** A hidden price (G7): [price] is 0 and the app shows "Price hidden". */
    val isPriceHidden: Boolean = false,
)

/** Everything the products tab needs for one website. */
@Immutable
data class ProductCatalog(
    val products: List<Product>,
    val categories: List<String>,
)

/** What the add/edit form produces. [id] is null for a new product. */
@Immutable
data class ProductDraft(
    val id: String?,
    val name: String,
    val category: String,
    val priceText: String,
    val description: String,
    val imageUrl: String,
)

enum class ProductFormError { NameEmpty, CategoryEmpty, PriceInvalid }

/**
 * `Product.imageUrl` is either empty (leaf placeholder), a picked photo (`file://`, `content://`, `https://`),
 * or a bundled sample photo written `drawable:<resource name>` (the iOS asset catalog names).
 */
object ProductImages {
    const val DRAWABLE_PREFIX = "drawable:"

    fun drawable(name: String): String = DRAWABLE_PREFIX + name
}
