package com.kshrd.features.remote.repository

import com.kshrd.core.api.ApiException
import com.kshrd.core.api.ApiMessages
import com.kshrd.core.api.apiCall
import com.kshrd.core.api.apiFlow
import com.kshrd.core.api.toErrorState
import com.kshrd.core.data.entity.DataState
import com.kshrd.features.products.domain.model.Product
import com.kshrd.features.products.domain.model.ProductCatalog
import com.kshrd.features.products.domain.repository.ProductRepository
import com.kshrd.features.remote.api.ProductApi
import com.kshrd.features.remote.dto.CategoryRequest
import com.kshrd.features.remote.dto.allPages
import com.kshrd.features.remote.mapper.PhotoChange
import com.kshrd.features.remote.mapper.productBody
import com.kshrd.features.remote.mapper.toProduct
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private const val HTTP_CONFLICT = 409

/**
 * Project v2 products, categories and photos (docs/API-MAPPING.md 2.4).
 *
 * The catalog reloads after every change. A replaced or removed photo is deleted from the website's library
 * afterwards (owner, 14 Nov 2026), so it stops counting against the photo limit; that delete is best effort, and
 * Project refuses it (409 `MEDIA_IN_USE`) while anything still uses the image.
 */
@Singleton
class ApiProductRepository
    @Inject
    constructor(
        private val productApi: ProductApi,
        private val photoEncoder: PhotoEncoder,
    ) : ProductRepository {
        /** The last catalog read per website: the photo a save replaces. */
        private val latest = ConcurrentHashMap<String, List<Product>>()
        private val changed = MutableSharedFlow<String>(extraBufferCapacity = 1)

        override fun observeCatalog(websiteId: String): Flow<DataState<ProductCatalog>> =
            flow {
                emit(loadState { fetch(websiteId) })
                changed.filter { it == websiteId }.collect { emit(loadState { fetch(websiteId) }) }
            }

        override fun saveProduct(
            websiteId: String,
            product: Product,
        ): Flow<DataState<Product>> =
            flow {
                val previous = latest[websiteId]?.firstOrNull { it.id == product.id }
                val result =
                    attempt { apiCall { write(websiteId, product, previous) } }
                        .fold(onSuccess = { DataState.Success(it) }, onFailure = { it.toSaveError() })
                emit(result)
                if (result is DataState.Success) {
                    changed.tryEmit(websiteId)
                    val oldPhoto = previous?.imageUrl.orEmpty()
                    if (oldPhoto.isNotEmpty() && oldPhoto != result.data.imageUrl) deleteUnusedPhoto(websiteId, oldPhoto)
                }
            }

        override fun deleteProduct(
            websiteId: String,
            productId: String,
        ): Flow<DataState<Unit>> =
            apiFlow {
                val deleted = productApi.deleteProduct(websiteId, productId)
                changed.tryEmit(websiteId)
                deleted.imageUrl?.takeIf { it.isNotEmpty() }?.let { deleteUnusedPhoto(websiteId, it) }
            }

        override fun addCategory(
            websiteId: String,
            name: String,
        ): Flow<DataState<String>> =
            apiFlow {
                try {
                    apiCall { productApi.addCategory(websiteId, CategoryRequest(name)) }
                } catch (e: ApiException) {
                    // CATEGORY_NAME_TAKEN: it exists already, which is what the owner wanted.
                    if (e.status != HTTP_CONFLICT) throw e
                }
                changed.tryEmit(websiteId)
                name
            }

        override fun uploadPhoto(
            websiteId: String,
            sourceUri: String,
        ): Flow<DataState<String>> =
            apiFlow {
                val photo = photoEncoder.encode(sourceUri)
                val part =
                    MultipartBody.Part.createFormData("file", photo.fileName, photo.bytes.toRequestBody(photo.mimeType.toMediaType()))
                productApi.uploadMedia(websiteId, part).url
            }

        private suspend fun fetch(websiteId: String): ProductCatalog =
            coroutineScope {
                val products = async { allPages { page -> productApi.products(websiteId, page) }.map { it.toProduct() } }
                val categories = async { productApi.categories(websiteId) }
                val list = products.await()
                latest[websiteId] = list
                ProductCatalog(list, categories.await())
            }

        /** POST for a product never loaded from the API (no version), else PUT with its version. */
        private suspend fun write(
            websiteId: String,
            product: Product,
            previous: Product?,
        ): Product {
            val photo =
                when {
                    previous != null && product.imageUrl == previous.imageUrl -> PhotoChange.Keep
                    product.imageUrl.isEmpty() -> if (previous == null) PhotoChange.Keep else PhotoChange.Remove
                    else -> PhotoChange.Set(product.imageUrl)
                }
            val price = if (product.isPriceHidden) null else product.price
            val body = productBody(product.name, product.description, product.category, price, photo, product.version)
            val saved =
                if (product.version == null) {
                    productApi.createProduct(websiteId, body)
                } else {
                    productApi.updateProduct(websiteId, product.id, body)
                }
            return saved.toProduct()
        }

        /** Finds the image by its URL in the library and deletes it; any refusal or failure is fine. */
        private suspend fun deleteUnusedPhoto(
            websiteId: String,
            url: String,
        ) {
            optional {
                val asset = allPages { page -> productApi.media(websiteId, page) }.firstOrNull { it.url == url }
                if (asset != null) productApi.deleteMedia(websiteId, asset.id)
            }
        }
    }

/** A save's error: 409 is "changed somewhere else" (the version is stale), anything else as usual. */
private fun Throwable.toSaveError(): DataState.Error =
    if (this is ApiException && status == HTTP_CONFLICT) {
        DataState.Error(status, ApiMessages.CHANGED_ELSEWHERE, this)
    } else {
        toErrorState()
    }
