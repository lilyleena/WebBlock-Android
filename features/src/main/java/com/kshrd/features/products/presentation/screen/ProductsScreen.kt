package com.kshrd.features.products.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.OnEachEvent
import com.kshrd.core.session.PortalChrome
import com.kshrd.core.session.WorkspaceState
import com.kshrd.core.session.WorkspaceWebsite
import com.kshrd.coreui.components.button.SecondaryButton
import com.kshrd.coreui.components.decor.LiquidBackdrop
import com.kshrd.coreui.components.feedback.ConfirmDialog
import com.kshrd.coreui.components.feedback.EmptyStateView
import com.kshrd.coreui.components.feedback.ErrorStateView
import com.kshrd.coreui.components.feedback.LoadingView
import com.kshrd.coreui.components.feedback.ToastData
import com.kshrd.coreui.components.feedback.WbToastHost
import com.kshrd.coreui.components.nav.dockFloatingOffset
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.features.R
import com.kshrd.features.business.presentation.components.PortalTitle
import com.kshrd.features.business.presentation.components.PortalTopBar
import com.kshrd.features.business.presentation.components.SiteHeaderCard
import com.kshrd.features.products.domain.model.Product
import com.kshrd.features.products.domain.model.ProductCatalog
import com.kshrd.features.products.domain.model.ProductImages
import com.kshrd.features.products.presentation.components.AddProductButton
import com.kshrd.features.products.presentation.components.ProductCategoryChips
import com.kshrd.features.products.presentation.components.ProductEditorSheet
import com.kshrd.features.products.presentation.components.ProductRow
import com.kshrd.features.products.presentation.components.ProductSearchField
import com.kshrd.features.products.presentation.components.ProductSummaryBar
import com.kshrd.features.products.presentation.components.ProductTile
import com.kshrd.features.products.presentation.model.ProductLayout
import com.kshrd.features.products.presentation.model.ProductsEffect
import com.kshrd.features.products.presentation.model.ProductsEvent
import com.kshrd.features.products.presentation.model.ProductsUiState
import com.kshrd.features.products.presentation.viewmodel.ProductsViewModel

@Composable
fun ProductsScreen(
    onOpenProduct: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onBackToWebsites: () -> Unit,
    viewModel: ProductsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var toast by remember { mutableStateOf<ToastData?>(null) }
    val context = LocalContext.current
    OnEachEvent(viewModel.effects) { effect ->
        when (effect) {
            is ProductsEffect.ShowToast -> {
                val request = effect.toast
                val message = request.arg?.let { context.getString(request.messageRes, it) } ?: context.getString(request.messageRes)
                toast = ToastData(request.kind, context.getString(request.titleRes), message)
            }
            is ProductsEffect.OpenProduct -> onOpenProduct(effect.productId)
            ProductsEffect.OpenNotifications -> onOpenNotifications()
            ProductsEffect.BackToWebsites -> onBackToWebsites()
        }
    }
    ProductsContent(state = state, toast = toast, onDismissToast = { toast = null }, onEvent = viewModel::onEvent)
}

@Composable
fun ProductsContent(
    state: ProductsUiState,
    toast: ToastData?,
    onDismissToast: () -> Unit,
    onEvent: (ProductsEvent) -> Unit,
) {
    LiquidBackdrop(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            PortalTopBar(
                ownerName = state.chrome.ownerName,
                unreadCount = state.chrome.unreadCount,
                onBack = { onEvent(ProductsEvent.AllWebsitesClicked) },
                onBell = { onEvent(ProductsEvent.BellClicked) },
            )
            ProductsGrid(state, onEvent)
        }
        AddProductButton(
            onClick = { onEvent(ProductsEvent.AddClicked) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = Space.screen, bottom = dockFloatingOffset()),
        )
        WbToastHost(toast = toast, onDismiss = onDismissToast)
    }
    ProductsOverlays(state, onEvent)
}

@Composable
private fun ProductsGrid(
    state: ProductsUiState,
    onEvent: (ProductsEvent) -> Unit,
) {
    val isList = state.layout == ProductLayout.List
    val catalog = (state.catalog as? DataState.Success)?.data
    LazyVerticalGrid(
        columns = if (isList) GridCells.Fixed(1) else GridCells.Adaptive(150.dp),
        // Room for the floating "Add product" button and the dock under the last row.
        contentPadding = PaddingValues(start = Space.screen, end = Space.screen, top = Space.xs, bottom = dockFloatingOffset() + Sizes.control + Space.l),
        horizontalArrangement = Arrangement.spacedBy(Space.s),
        verticalArrangement = Arrangement.spacedBy(Space.s),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            // The large title scrolls away with the content, like the iOS navigation title.
            PortalTitle(stringResource(R.string.products_title), horizontalPadding = 0.dp)
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            ProductsHeader(state, catalog, onEvent)
        }
        when (val data = state.catalog) {
            DataState.Loading -> fullRow { LoadingView() }
            is DataState.Error -> fullRow { ErrorStateView(message = stringResource(R.string.pv_load_error), onRetry = { onEvent(ProductsEvent.RetryClicked) }) }
            is DataState.MessageSuccess -> Unit
            is DataState.Success -> productItems(state, data.data, isList, onEvent)
        }
    }
}

@Composable
private fun ProductsHeader(
    state: ProductsUiState,
    catalog: ProductCatalog?,
    onEvent: (ProductsEvent) -> Unit,
) {
    val workspace = state.chrome.workspace
    // Purely visual: whether the category chips and layout switch are showing.
    var filtersShown by rememberSaveable { mutableStateOf(true) }
    Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
        ProductSearchField(
            query = state.query,
            onQueryChange = { onEvent(ProductsEvent.QueryChanged(it)) },
            filtersShown = filtersShown,
            onToggleFilters = { filtersShown = !filtersShown },
        )
        SiteHeaderCard(
            website = workspace.current,
            websites = workspace.websites,
            onSwitch = { onEvent(ProductsEvent.WebsiteSwitched(it)) },
        )
        if (catalog != null && filtersShown) {
            ProductCategoryChips(
                categories = catalog.categories,
                totalCount = state.totalCount,
                selected = state.selectedCategory,
                onSelect = { onEvent(ProductsEvent.CategorySelected(it)) },
            )
            ProductSummaryBar(
                summary = summaryText(state),
                layout = state.layout,
                onLayoutChange = { onEvent(ProductsEvent.LayoutChanged(it)) },
            )
        }
    }
}

@Composable
private fun summaryText(state: ProductsUiState): String {
    val total = state.visibleProducts.size
    return when {
        state.selectedCategory == null && state.query.isEmpty() ->
            stringResource(R.string.pv_summary_all, state.shownProducts.size, total)
        total == 1 -> stringResource(R.string.pv_summary_one)
        else -> stringResource(R.string.pv_summary_many, total)
    }
}

private fun LazyGridScope.fullRow(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

private fun LazyGridScope.productItems(
    state: ProductsUiState,
    catalog: ProductCatalog,
    isList: Boolean,
    onEvent: (ProductsEvent) -> Unit,
) {
    if (state.visibleProducts.isEmpty()) {
        fullRow {
            val noQuery = state.query.isEmpty()
            EmptyStateView(
                title = stringResource(if (noQuery) R.string.pv_empty_title else R.string.pv_no_results_title),
                message = stringResource(if (noQuery) R.string.pv_empty_message else R.string.pv_no_results_message),
                icon = if (noQuery) WebIcons.Package else Icons.Outlined.SearchOff,
                modifier = Modifier.padding(vertical = Space.l),
            )
        }
        return
    }
    items(state.shownProducts, key = { it.id }) { product ->
        val open = { onEvent(ProductsEvent.ProductClicked(product.id)) }
        val edit = { onEvent(ProductsEvent.EditClicked(product)) }
        val delete = { onEvent(ProductsEvent.DeleteClicked(product)) }
        if (isList) {
            ProductRow(product, open, edit, delete, Modifier.animateItem())
        } else {
            ProductTile(product, open, edit, delete, Modifier.animateItem())
        }
    }
    if (state.canLoadMore) {
        fullRow {
            SecondaryButton(text = stringResource(R.string.pv_show_more), onClick = { onEvent(ProductsEvent.ShowMoreClicked) })
        }
    }
}

@Composable
private fun ProductsOverlays(
    state: ProductsUiState,
    onEvent: (ProductsEvent) -> Unit,
) {
    state.editor?.let { ProductEditorSheet(state = it, onEvent = { e -> onEvent(ProductsEvent.Editor(e)) }) }
    state.deleteTarget?.let { target ->
        ConfirmDialog(
            title = stringResource(R.string.pv_delete_title, target.name),
            message = stringResource(R.string.pv_delete_message),
            confirmLabel = stringResource(R.string.pv_delete_confirm),
            cancelLabel = stringResource(R.string.pv_cancel),
            onConfirm = { onEvent(ProductsEvent.DeleteConfirmed) },
            onDismiss = { onEvent(ProductsEvent.DeleteDismissed) },
            destructive = true,
        )
    }
}

private val previewSite = WorkspaceWebsite("1", "Petal & Stem", "petalandstem.webblock.com")

private val previewChrome =
    PortalChrome(
        workspace = WorkspaceState(current = previewSite, websites = listOf(previewSite, WorkspaceWebsite("2", "Golden Crumb Bakery", "goldencrumb.webblock.com"))),
        unreadCount = 2,
    )

private fun sample(
    id: String,
    name: String,
    category: String,
    price: Double,
    image: String,
) = Product(id, name, category, price, "A hand-tied bouquet of seasonal flowers.", ProductImages.drawable(image))

@ThemePreviews
@Composable
private fun ProductsContentPreview() {
    PreviewSurface(padding = 0) {
        ProductsContent(
            state =
                ProductsUiState(
                    chrome = previewChrome,
                    catalog =
                        DataState.Success(
                            ProductCatalog(
                                listOf(
                                    sample("1", "Blue lily stem", "Flowers", 6.0, "blue_lily_stem"),
                                    sample("2", "Gift bouquet", "Bouquets", 38.0, "gift_bouquet"),
                                    sample("3", "Stone vase", "Gifts", 24.0, "stone_vase"),
                                ),
                                listOf("Flowers", "Bouquets", "Gifts"),
                            ),
                        ),
                ),
            toast = null,
            onDismissToast = {},
            onEvent = {},
        )
    }
}

@ThemePreviews
@Composable
private fun ProductsEmptyPreview() {
    PreviewSurface(padding = 0) {
        ProductsContent(
            state = ProductsUiState(chrome = previewChrome, catalog = DataState.Success(ProductCatalog(emptyList(), listOf("Flowers", "Bouquets")))),
            toast = null,
            onDismissToast = {},
            onEvent = {},
        )
    }
}
