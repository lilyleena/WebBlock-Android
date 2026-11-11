package com.kshrd.features.products.presentation.model

import androidx.compose.runtime.Immutable
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.UiState
import com.kshrd.core.session.PortalChrome
import com.kshrd.features.products.domain.model.Product
import com.kshrd.features.products.domain.model.ProductCatalog

enum class ProductLayout { Grid, List }

@Immutable
data class ProductsUiState(
    val chrome: PortalChrome = PortalChrome(),
    val catalog: UiState<ProductCatalog> = DataState.Loading,
    val query: String = "",
    /** Null means "All". */
    val selectedCategory: String? = null,
    val layout: ProductLayout = ProductLayout.Grid,
    /** How many filtered products are on screen; "Show more" grows it by [PAGE_SIZE]. */
    val visibleCount: Int = PAGE_SIZE,
    val deleteTarget: Product? = null,
    val editor: ProductEditorUiState? = null,
) {
    private val all: List<Product> get() = (catalog as? DataState.Success)?.data?.products.orEmpty()

    val totalCount: Int get() = all.size

    /** Search + category filter. Lives on the state so composables stay free of logic. */
    val visibleProducts: List<Product>
        get() =
            all.filter { product ->
                (selectedCategory == null || product.category == selectedCategory) &&
                    (query.isBlank() || product.name.contains(query.trim(), ignoreCase = true))
            }

    val shownProducts: List<Product> get() = visibleProducts.take(visibleCount)

    val canLoadMore: Boolean get() = visibleCount < visibleProducts.size

    companion object {
        const val PAGE_SIZE = 6
    }
}

sealed interface ProductsEvent {
    data class QueryChanged(val value: String) : ProductsEvent

    data class CategorySelected(val category: String?) : ProductsEvent

    data class LayoutChanged(val layout: ProductLayout) : ProductsEvent

    data class ProductClicked(val productId: String) : ProductsEvent

    data class EditClicked(val product: Product) : ProductsEvent

    data object AddClicked : ProductsEvent

    data class DeleteClicked(val product: Product) : ProductsEvent

    data object DeleteConfirmed : ProductsEvent

    data object DeleteDismissed : ProductsEvent

    data object RetryClicked : ProductsEvent

    data object BellClicked : ProductsEvent

    data object ShowMoreClicked : ProductsEvent

    data class WebsiteSwitched(val websiteId: String) : ProductsEvent

    data object AllWebsitesClicked : ProductsEvent

    data class Editor(val event: ProductEditorEvent) : ProductsEvent
}

sealed interface ProductsEffect {
    data class ShowToast(val toast: ToastRequest) : ProductsEffect

    data class OpenProduct(val productId: String) : ProductsEffect

    data object OpenNotifications : ProductsEffect

    data object BackToWebsites : ProductsEffect
}
