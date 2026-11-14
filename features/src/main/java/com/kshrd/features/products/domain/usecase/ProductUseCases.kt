package com.kshrd.features.products.domain.usecase

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.asUseCaseFlow
import com.kshrd.features.products.domain.model.Product
import com.kshrd.features.products.domain.model.ProductCatalog
import com.kshrd.features.products.domain.model.ProductDraft
import com.kshrd.features.products.domain.model.ProductFormError
import com.kshrd.features.products.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import javax.inject.Inject

/** Form rules for a product. Pure, so the editor can validate before calling the use case. */
object ProductValidator {
    fun validate(draft: ProductDraft): Set<ProductFormError> =
        buildSet {
            if (draft.name.isBlank()) add(ProductFormError.NameEmpty)
            if (draft.category.isBlank()) add(ProductFormError.CategoryEmpty)
            if (!keepsPriceHidden(draft) && parsePrice(draft.priceText) == null) add(ProductFormError.PriceInvalid)
        }

    /** A hidden price (G7) stays hidden while the price field is left empty; typing a price shows it. */
    fun keepsPriceHidden(draft: ProductDraft): Boolean = draft.wasPriceHidden && draft.priceText.isBlank()

    /** Project's rule: up to 6 digits, optionally 1 or 2 decimals ("3.50"); anything else is refused, not rounded. */
    private val PRICE = Regex("""^\d{1,6}(\.\d{1,2})?$""")

    fun parsePrice(text: String): Double? =
        text
            .trim()
            .removePrefix("$")
            .replace(",", "")
            .takeIf { PRICE.matches(it) }
            ?.toDoubleOrNull()
}

class ObserveCatalogUseCase
    @Inject
    constructor(
        private val repository: ProductRepository,
    ) {
        operator fun invoke(websiteId: String): Flow<DataState<ProductCatalog>> = repository.observeCatalog(websiteId).asUseCaseFlow()
    }

class SaveProductUseCase
    @Inject
    constructor(
        private val repository: ProductRepository,
    ) {
        operator fun invoke(
            websiteId: String,
            draft: ProductDraft,
        ): Flow<DataState<Product>> {
            val hidden = ProductValidator.keepsPriceHidden(draft)
            val price = if (hidden) 0.0 else ProductValidator.parsePrice(draft.priceText)
            if (ProductValidator.validate(draft).isNotEmpty() || price == null) {
                return flowOf(DataState.Error(message = "Please complete the form."))
            }
            val product =
                Product(
                    id = draft.id ?: UUID.randomUUID().toString(),
                    name = draft.name.trim(),
                    category = draft.category.trim(),
                    price = price,
                    description = draft.description.trim(),
                    imageUrl = draft.imageUrl,
                    version = draft.version,
                    isPriceHidden = hidden,
                )
            return repository.saveProduct(websiteId, product).asUseCaseFlow()
        }
    }

class DeleteProductUseCase
    @Inject
    constructor(
        private val repository: ProductRepository,
    ) {
        operator fun invoke(
            websiteId: String,
            productId: String,
        ): Flow<DataState<Unit>> = repository.deleteProduct(websiteId, productId).asUseCaseFlow()
    }

class UploadProductPhotoUseCase
    @Inject
    constructor(
        private val repository: ProductRepository,
    ) {
        operator fun invoke(
            websiteId: String,
            sourceUri: String,
        ): Flow<DataState<String>> = repository.uploadPhoto(websiteId, sourceUri).asUseCaseFlow()
    }

class AddCategoryUseCase
    @Inject
    constructor(
        private val repository: ProductRepository,
    ) {
        operator fun invoke(
            websiteId: String,
            name: String,
        ): Flow<DataState<String>> {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return flowOf(DataState.Error(message = "Category name is required."))
            return repository.addCategory(websiteId, trimmed).asUseCaseFlow()
        }
    }
