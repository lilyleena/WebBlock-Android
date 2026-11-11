package com.kshrd.features.products.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.navigation.ProductDetail
import com.kshrd.core.presentation.OneShotEvents
import com.kshrd.core.session.ObserveWorkspaceUseCase
import com.kshrd.coreui.components.feedback.ToastKind
import com.kshrd.features.R
import com.kshrd.features.products.domain.usecase.AddCategoryUseCase
import com.kshrd.features.products.domain.usecase.DeleteProductUseCase
import com.kshrd.features.products.domain.usecase.ObserveCatalogUseCase
import com.kshrd.features.products.domain.usecase.SaveProductUseCase
import com.kshrd.features.products.domain.usecase.UploadProductPhotoUseCase
import com.kshrd.features.products.presentation.model.ProductDetailEffect
import com.kshrd.features.products.presentation.model.ProductDetailEvent
import com.kshrd.features.products.presentation.model.ProductDetailUiState
import com.kshrd.features.products.presentation.model.ToastRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val observeWorkspace: ObserveWorkspaceUseCase,
        private val observeCatalog: ObserveCatalogUseCase,
        private val deleteProduct: DeleteProductUseCase,
        saveProduct: SaveProductUseCase,
        addCategory: AddCategoryUseCase,
        uploadPhoto: UploadProductPhotoUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(ProductDetailUiState(productId = savedStateHandle.toRoute<ProductDetail>().productId))
        val state: StateFlow<ProductDetailUiState> = _state.asStateFlow()

        private val _effects = OneShotEvents<ProductDetailEffect>()
        val effects = _effects.events

        private val editor =
            ProductEditorController(
                scope = viewModelScope,
                websiteId = { _state.value.website?.id },
                saveProduct = saveProduct,
                addCategory = addCategory,
                uploadPhoto = uploadPhoto::invoke,
                onSaved = { _, _ ->
                    val toast = ToastRequest(ToastKind.Success, R.string.pv_toast_saved_title, R.string.pv_toast_saved_message)
                    _effects.tryEmit(ProductDetailEffect.ShowToast(toast))
                },
                onDeleteRequested = { _state.update { it.copy(confirmDelete = true) } },
            )

        init {
            viewModelScope.launch { observeWorkspace().collect { ws -> _state.update { it.copy(website = ws.current) } } }
            viewModelScope.launch { editor.state.collect { e -> _state.update { it.copy(editor = e) } } }
            observeProduct()
        }

        fun onEvent(event: ProductDetailEvent) {
            when (event) {
                ProductDetailEvent.BackClicked -> _effects.tryEmit(ProductDetailEffect.Back)
                ProductDetailEvent.EditClicked -> {
                    val current = _state.value
                    val product = current.product ?: return
                    editor.openEdit(product, (current.catalog as? DataState.Success)?.data?.categories.orEmpty())
                }
                ProductDetailEvent.DeleteClicked -> _state.update { it.copy(confirmDelete = true) }
                ProductDetailEvent.WebsiteClicked -> _state.value.website?.let { _effects.tryEmit(ProductDetailEffect.OpenUrl("https://" + it.domain)) }
                ProductDetailEvent.DeleteDismissed -> _state.update { it.copy(confirmDelete = false) }
                ProductDetailEvent.DeleteConfirmed -> confirmDelete()
                is ProductDetailEvent.Editor -> editor.onEvent(event.event)
            }
        }

        private fun confirmDelete() {
            val current = _state.value
            val websiteId = current.website?.id ?: return
            _state.update { it.copy(confirmDelete = false) }
            viewModelScope.launch {
                deleteProduct(websiteId, current.productId).collect { result ->
                    if (result is DataState.Success) _effects.emit(ProductDetailEffect.Back)
                }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun observeProduct() {
            viewModelScope.launch {
                observeWorkspace()
                    .map { it.current?.id }
                    .filterNotNull()
                    .distinctUntilChanged()
                    .flatMapLatest { observeCatalog(it) }
                    .collect { catalog -> _state.update { it.copy(catalog = catalog) } }
            }
        }
    }
