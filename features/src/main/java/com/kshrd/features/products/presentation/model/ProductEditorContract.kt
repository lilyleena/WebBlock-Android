package com.kshrd.features.products.presentation.model

import androidx.compose.runtime.Immutable
import com.kshrd.features.products.domain.model.ProductFormError

/** State of the add/edit product sheet. `null` in the host screen's state means the sheet is closed. */
@Immutable
data class ProductEditorUiState(
    /** Null when adding a new product. */
    val productId: String? = null,
    val name: String = "",
    val category: String = "",
    val price: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val isUploadingPhoto: Boolean = false,
    val photoError: String? = null,
    val categories: List<String> = emptyList(),
    val errors: Set<ProductFormError> = emptySet(),
    val isSaving: Boolean = false,
    val showCreateCategory: Boolean = false,
    val newCategory: String = "",
    val newCategoryError: String? = null,
) {
    val isNew: Boolean get() = productId == null
}

sealed interface ProductEditorEvent {
    data class NameChanged(val value: String) : ProductEditorEvent

    data class CategoryChanged(val value: String) : ProductEditorEvent

    data class PriceChanged(val value: String) : ProductEditorEvent

    data class DescriptionChanged(val value: String) : ProductEditorEvent

    /** The owner chose an image in the system photo picker. */
    data class PhotoPicked(val uri: String) : ProductEditorEvent

    data object PhotoRemoved : ProductEditorEvent

    data object DeleteClicked : ProductEditorEvent

    data object SaveClicked : ProductEditorEvent

    data object CancelClicked : ProductEditorEvent

    data object CreateCategoryClicked : ProductEditorEvent

    data class NewCategoryChanged(val value: String) : ProductEditorEvent

    data object NewCategoryConfirmed : ProductEditorEvent

    data object NewCategoryDismissed : ProductEditorEvent
}
