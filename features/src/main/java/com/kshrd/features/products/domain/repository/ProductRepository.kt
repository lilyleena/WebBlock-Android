package com.kshrd.features.products.domain.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.features.products.domain.model.Product
import com.kshrd.features.products.domain.model.ProductCatalog
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    /** Live catalog of one website; re-emits after every save, delete or new category. */
    fun observeCatalog(websiteId: String): Flow<DataState<ProductCatalog>>

    fun saveProduct(
        websiteId: String,
        product: Product,
    ): Flow<DataState<Product>>

    fun deleteProduct(
        websiteId: String,
        productId: String,
    ): Flow<DataState<Unit>>

    /** Stores a photo the owner picked on their device and returns the URL to show. An upload with a real backend. */
    fun uploadPhoto(sourceUri: String): Flow<DataState<String>>

    fun addCategory(
        websiteId: String,
        name: String,
    ): Flow<DataState<String>>
}
