package com.kshrd.features.products.presentation.viewmodel

import com.kshrd.core.data.entity.DataState
import com.kshrd.features.products.domain.model.Product
import com.kshrd.features.products.domain.model.ProductDraft
import com.kshrd.features.products.domain.usecase.AddCategoryUseCase
import com.kshrd.features.products.domain.usecase.ProductValidator
import com.kshrd.features.products.domain.usecase.SaveProductUseCase
import com.kshrd.features.products.presentation.model.ProductEditorEvent
import com.kshrd.features.products.presentation.model.ProductEditorUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the add/edit product sheet. Both the products list and the product detail screens host
 * one, so the form logic exists once. Owned by a ViewModel, which supplies its scope and callbacks.
 */
class ProductEditorController(
    private val scope: CoroutineScope,
    private val websiteId: () -> String?,
    private val saveProduct: SaveProductUseCase,
    private val addCategory: AddCategoryUseCase,
    private val uploadPhoto: (sourceUri: String) -> Flow<DataState<String>>,
    private val onSaved: (product: Product, wasNew: Boolean) -> Unit,
    private val onDeleteRequested: (productId: String) -> Unit,
) {
    private val _state = MutableStateFlow<ProductEditorUiState?>(null)
    val state: StateFlow<ProductEditorUiState?> = _state.asStateFlow()

    fun openNew(categories: List<String>) {
        // A new product starts empty: no photo and no category until the owner chooses them.
        _state.value = ProductEditorUiState(categories = categories)
    }

    fun openEdit(
        product: Product,
        categories: List<String>,
    ) {
        _state.value =
            ProductEditorUiState(
                productId = product.id,
                name = product.name,
                category = product.category,
                price = "%.2f".format(java.util.Locale.US, product.price),
                description = product.description,
                imageUrl = product.imageUrl,
                categories = categories,
            )
    }

    fun close() {
        _state.value = null
    }

    fun onEvent(event: ProductEditorEvent) {
        when (event) {
            is ProductEditorEvent.NameChanged -> edit { it.copy(name = event.value, errors = it.errors - NAME) }
            is ProductEditorEvent.CategoryChanged -> edit { it.copy(category = event.value, errors = it.errors - CATEGORY) }
            is ProductEditorEvent.PriceChanged -> edit { it.copy(price = event.value, errors = it.errors - PRICE) }
            is ProductEditorEvent.DescriptionChanged -> edit { it.copy(description = event.value) }
            is ProductEditorEvent.PhotoPicked -> upload(event.uri)
            ProductEditorEvent.PhotoRemoved -> edit { it.copy(imageUrl = "", photoError = null) }
            ProductEditorEvent.SaveClicked -> save()
            ProductEditorEvent.CancelClicked -> close()
            ProductEditorEvent.DeleteClicked -> {
                val id = _state.value?.productId
                close()
                if (id != null) onDeleteRequested(id)
            }
            ProductEditorEvent.CreateCategoryClicked -> edit { it.copy(showCreateCategory = true, newCategory = "", newCategoryError = null) }
            is ProductEditorEvent.NewCategoryChanged -> edit { it.copy(newCategory = event.value, newCategoryError = null) }
            ProductEditorEvent.NewCategoryDismissed -> edit { it.copy(showCreateCategory = false) }
            ProductEditorEvent.NewCategoryConfirmed -> createCategory()
        }
    }

    private fun edit(transform: (ProductEditorUiState) -> ProductEditorUiState) {
        _state.update { current -> current?.let(transform) }
    }

    private fun draftOf(s: ProductEditorUiState) =
        ProductDraft(s.productId, s.name, s.category, s.price, s.description, s.imageUrl)

    private fun upload(uri: String) {
        scope.launch {
            uploadPhoto(uri).collect { result ->
                when (result) {
                    is DataState.Loading -> edit { it.copy(isUploadingPhoto = true, photoError = null) }
                    is DataState.Success -> edit { it.copy(imageUrl = result.data, isUploadingPhoto = false, photoError = null) }
                    is DataState.Error -> edit { it.copy(isUploadingPhoto = false, photoError = result.message) }
                    else -> edit { it.copy(isUploadingPhoto = false) }
                }
            }
        }
    }

    private fun save() {
        val current = _state.value ?: return
        if (current.isSaving || current.isUploadingPhoto) return
        val draft = draftOf(current)
        val errors = ProductValidator.validate(draft)
        if (errors.isNotEmpty()) {
            edit { it.copy(errors = errors) }
            return
        }
        val site = websiteId() ?: return
        scope.launch {
            saveProduct(site, draft).collect { result ->
                when (result) {
                    is DataState.Loading -> edit { it.copy(isSaving = true) }
                    is DataState.Success -> {
                        close()
                        onSaved(result.data, current.isNew)
                    }
                    else -> edit { it.copy(isSaving = false) }
                }
            }
        }
    }

    private fun createCategory() {
        val current = _state.value ?: return
        val site = websiteId() ?: return
        scope.launch {
            addCategory(site, current.newCategory).collect { result ->
                when (result) {
                    is DataState.Success ->
                        edit {
                            it.copy(
                                categories = it.categories + result.data,
                                category = result.data,
                                errors = it.errors - CATEGORY,
                                showCreateCategory = false,
                            )
                        }
                    is DataState.Error -> edit { it.copy(newCategoryError = result.message) }
                    else -> Unit
                }
            }
        }
    }

    private companion object {
        val NAME = com.kshrd.features.products.domain.model.ProductFormError.NameEmpty
        val CATEGORY = com.kshrd.features.products.domain.model.ProductFormError.CategoryEmpty
        val PRICE = com.kshrd.features.products.domain.model.ProductFormError.PriceInvalid
    }
}
