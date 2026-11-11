package com.kshrd.features.products.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.OnEachEvent
import com.kshrd.core.session.WorkspaceWebsite
import com.kshrd.coreui.components.decor.LiquidBackdrop
import com.kshrd.coreui.components.feedback.ConfirmDialog
import com.kshrd.coreui.components.feedback.EmptyStateView
import com.kshrd.coreui.components.feedback.LoadingView
import com.kshrd.coreui.components.feedback.ToastData
import com.kshrd.coreui.components.feedback.WbToastHost
import com.kshrd.coreui.components.nav.dockContentPadding
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space
import com.kshrd.features.R
import com.kshrd.features.products.domain.model.ProductCatalog
import com.kshrd.features.products.presentation.components.ProductDetailBody
import com.kshrd.features.products.presentation.components.ProductDetailTopBar
import com.kshrd.features.products.presentation.components.ProductEditorSheet
import com.kshrd.features.products.presentation.components.previewProduct
import com.kshrd.features.products.presentation.model.ProductDetailEffect
import com.kshrd.features.products.presentation.model.ProductDetailEvent
import com.kshrd.features.products.presentation.model.ProductDetailUiState
import com.kshrd.features.products.presentation.viewmodel.ProductDetailViewModel

@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var toast by remember { mutableStateOf<ToastData?>(null) }
    val context = LocalContext.current
    OnEachEvent(viewModel.effects) { effect ->
        when (effect) {
            ProductDetailEffect.Back -> onBack()
            is ProductDetailEffect.ShowToast -> {
                val request = effect.toast
                val message = request.arg?.let { context.getString(request.messageRes, it) } ?: context.getString(request.messageRes)
                toast = ToastData(request.kind, context.getString(request.titleRes), message)
            }
        }
    }
    ProductDetailContent(state = state, toast = toast, onDismissToast = { toast = null }, onEvent = viewModel::onEvent)
}

@Composable
fun ProductDetailContent(
    state: ProductDetailUiState,
    toast: ToastData?,
    onDismissToast: () -> Unit,
    onEvent: (ProductDetailEvent) -> Unit,
) {
    LiquidBackdrop(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            ProductDetailTopBar(
                productName = state.product?.name,
                onBack = { onEvent(ProductDetailEvent.BackClicked) },
                onEdit = { onEvent(ProductDetailEvent.EditClicked) },
                onDelete = { onEvent(ProductDetailEvent.DeleteClicked) },
            )
            when (val catalog = state.catalog) {
                DataState.Loading -> LoadingView()
                is DataState.Error -> EmptyStateView(title = stringResource(R.string.pv_load_error), icon = Icons.Outlined.WifiOff)
                is DataState.MessageSuccess -> Unit
                is DataState.Success -> {
                    val product = state.product
                    if (product == null) {
                        EmptyStateView(title = stringResource(R.string.pv_detail_removed), icon = Icons.Outlined.Inventory2)
                    } else {
                        ProductDetailBody(
                            product = product,
                            websiteName = state.website?.name,
                            onEdit = { onEvent(ProductDetailEvent.EditClicked) },
                            onDelete = { onEvent(ProductDetailEvent.DeleteClicked) },
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = Space.screen)
                                    .padding(top = Space.xs, bottom = dockContentPadding()),
                        )
                    }
                }
            }
        }
        WbToastHost(toast = toast, onDismiss = onDismissToast)
    }
    state.editor?.let { ProductEditorSheet(state = it, onEvent = { e -> onEvent(ProductDetailEvent.Editor(e)) }) }
    val product = state.product
    if (state.confirmDelete && product != null) {
        ConfirmDialog(
            title = stringResource(R.string.pv_delete_title, product.name),
            message = stringResource(R.string.pv_delete_message),
            confirmLabel = stringResource(R.string.pv_delete_confirm),
            cancelLabel = stringResource(R.string.pv_cancel),
            onConfirm = { onEvent(ProductDetailEvent.DeleteConfirmed) },
            onDismiss = { onEvent(ProductDetailEvent.DeleteDismissed) },
            destructive = true,
        )
    }
}

@ThemePreviews
@Composable
private fun ProductDetailContentPreview() {
    PreviewSurface(padding = 0) {
        ProductDetailContent(
            state =
                ProductDetailUiState(
                    website = WorkspaceWebsite("1", "Petal & Stem", "petalandstem.webblock.com"),
                    productId = previewProduct.id,
                    catalog = DataState.Success(ProductCatalog(listOf(previewProduct), listOf("Bouquets"))),
                ),
            toast = null,
            onDismissToast = {},
            onEvent = {},
        )
    }
}
