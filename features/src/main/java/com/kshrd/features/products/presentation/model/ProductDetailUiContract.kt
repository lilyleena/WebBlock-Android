package com.kshrd.features.products.presentation.model

import androidx.compose.runtime.Immutable
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.UiState
import com.kshrd.core.session.WorkspaceWebsite
import com.kshrd.features.products.domain.model.ProductCatalog

@Immutable
data class ProductDetailUiState(
    val website: WorkspaceWebsite? = null,
    val catalog: UiState<ProductCatalog> = DataState.Loading,
    val productId: String = "",
    val editor: ProductEditorUiState? = null,
    val confirmDelete: Boolean = false,
) {
    val product get() = (catalog as? DataState.Success)?.data?.products?.firstOrNull { it.id == productId }
}

sealed interface ProductDetailEvent {
    data object BackClicked : ProductDetailEvent

    data object EditClicked : ProductDetailEvent

    data object DeleteClicked : ProductDetailEvent

    /** The "Shown on" card: open the live website. */
    data object WebsiteClicked : ProductDetailEvent

    data object DeleteConfirmed : ProductDetailEvent

    data object DeleteDismissed : ProductDetailEvent

    data class Editor(val event: ProductEditorEvent) : ProductDetailEvent
}

sealed interface ProductDetailEffect {
    data object Back : ProductDetailEffect

    data class ShowToast(val toast: ToastRequest) : ProductDetailEffect

    data class OpenUrl(val url: String) : ProductDetailEffect
}
