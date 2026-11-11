package com.kshrd.features.products.data.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.toDataState
import com.kshrd.features.products.data.source.InMemoryProductDataSource
import com.kshrd.features.products.data.source.LocalPhotoStore
import com.kshrd.features.products.domain.model.Product
import com.kshrd.features.products.domain.model.ProductCatalog
import com.kshrd.features.products.domain.repository.ProductRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl
    @Inject
    constructor(
        private val source: InMemoryProductDataSource,
        private val photoStore: LocalPhotoStore,
    ) : ProductRepository {
        override fun observeCatalog(websiteId: String): Flow<DataState<ProductCatalog>> =
            flow {
                delay(LOAD_LATENCY_MS)
                emitAll(source.catalog(websiteId).map { DataState.Success(it) })
            }

        override fun saveProduct(
            websiteId: String,
            product: Product,
        ): Flow<DataState<Product>> = flow { emit(DataState.Success(source.save(websiteId, product))) }

        override fun deleteProduct(
            websiteId: String,
            productId: String,
        ): Flow<DataState<Unit>> =
            flow {
                source.delete(websiteId, productId)
                emit(DataState.Success(Unit))
            }

        override fun addCategory(
            websiteId: String,
            name: String,
        ): Flow<DataState<String>> = flow { emit(source.addCategory(websiteId, name).toDataState()) }

        override fun uploadPhoto(sourceUri: String): Flow<DataState<String>> = flow { emit(photoStore.store(sourceUri).toDataState()) }

        private companion object {
            const val LOAD_LATENCY_MS = 600L
        }
    }
