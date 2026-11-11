package com.kshrd.features.products.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.OneShotEvents
import com.kshrd.core.session.ObservePortalChromeUseCase
import com.kshrd.core.session.SwitchWebsiteUseCase
import com.kshrd.features.R
import com.kshrd.features.products.domain.usecase.AddCategoryUseCase
import com.kshrd.features.products.domain.usecase.DeleteProductUseCase
import com.kshrd.features.products.domain.usecase.ObserveCatalogUseCase
import com.kshrd.features.products.domain.usecase.SaveProductUseCase
import com.kshrd.features.products.domain.usecase.UploadProductPhotoUseCase
import com.kshrd.coreui.components.feedback.ToastKind
import com.kshrd.features.products.presentation.model.ProductsEffect
import com.kshrd.features.products.presentation.model.ProductsEvent
import com.kshrd.features.products.presentation.model.ProductsUiState
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel
    @Inject
    constructor(
        private val observeChrome: ObservePortalChromeUseCase,
        private val observeCatalog: ObserveCatalogUseCase,
        private val deleteProduct: DeleteProductUseCase,
        private val switchWebsite: SwitchWebsiteUseCase,
        saveProduct: SaveProductUseCase,
        addCategory: AddCategoryUseCase,
        uploadPhoto: UploadProductPhotoUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(ProductsUiState())
        val state: StateFlow<ProductsUiState> = _state.asStateFlow()

        private val _effects = OneShotEvents<ProductsEffect>()
        val effects = _effects.events

        private val editor =
            ProductEditorController(
                scope = viewModelScope,
                websiteId = { _state.value.chrome.workspace.current?.id },
                saveProduct = saveProduct,
                addCategory = addCategory,
                uploadPhoto = uploadPhoto::invoke,
                onSaved = { _, wasNew ->
                    val toast =
                        if (wasNew) {
                            ToastRequest(ToastKind.Success, R.string.pv_toast_added_title, R.string.pv_toast_added_message)
                        } else {
                            ToastRequest(ToastKind.Success, R.string.pv_toast_saved_title, R.string.pv_toast_saved_message)
                        }
                    _effects.tryEmit(ProductsEffect.ShowToast(toast))
                },
                onDeleteRequested = { id ->
                    val product = (_state.value.catalog as? DataState.Success)?.data?.products?.firstOrNull { it.id == id }
                    _state.update { it.copy(deleteTarget = product) }
                },
            )

        init {
            observeWorkspace()
            observeProducts()
            viewModelScope.launch { editor.state.collect { e -> _state.update { it.copy(editor = e) } } }
        }

        fun onEvent(event: ProductsEvent) {
            when (event) {
                is ProductsEvent.QueryChanged -> _state.update { it.copy(query = event.value) }
                is ProductsEvent.CategorySelected -> _state.update { it.copy(selectedCategory = event.category) }
                is ProductsEvent.LayoutChanged -> _state.update { it.copy(layout = event.layout) }
                is ProductsEvent.ProductClicked -> _effects.tryEmit(ProductsEffect.OpenProduct(event.productId))
                is ProductsEvent.EditClicked -> editor.openEdit(event.product, categories())
                ProductsEvent.AddClicked -> editor.openNew(categories())
                is ProductsEvent.DeleteClicked -> _state.update { it.copy(deleteTarget = event.product) }
                ProductsEvent.DeleteDismissed -> _state.update { it.copy(deleteTarget = null) }
                ProductsEvent.DeleteConfirmed -> confirmDelete()
                ProductsEvent.RetryClicked -> Unit
                ProductsEvent.BellClicked -> _effects.tryEmit(ProductsEffect.OpenNotifications)
                ProductsEvent.ShowMoreClicked -> _state.update { it.copy(visibleCount = it.visibleCount + ProductsUiState.PAGE_SIZE) }
                is ProductsEvent.WebsiteSwitched -> switchWebsite(event.websiteId)
                ProductsEvent.AllWebsitesClicked -> _effects.tryEmit(ProductsEffect.BackToWebsites)
                is ProductsEvent.Editor -> editor.onEvent(event.event)
            }
        }

        private fun categories(): List<String> = (_state.value.catalog as? DataState.Success)?.data?.categories.orEmpty()

        private fun observeWorkspace() {
            viewModelScope.launch {
                observeChrome().collect { chrome -> _state.update { it.copy(chrome = chrome) } }
            }
        }

        /** Re-loads the catalog every time the selected website changes, and resets the filters. */
        @OptIn(ExperimentalCoroutinesApi::class)
        private fun observeProducts() {
            viewModelScope.launch {
                observeChrome()
                    .map { it.workspace.current?.id }
                    .filterNotNull()
                    .distinctUntilChanged()
                    .onEach { _state.update { s -> s.copy(query = "", selectedCategory = null, visibleCount = ProductsUiState.PAGE_SIZE, catalog = DataState.Loading) } }
                    .flatMapLatest { websiteId -> observeCatalog(websiteId) }
                    .collect { catalog -> _state.update { it.copy(catalog = catalog) } }
            }
        }

        private fun confirmDelete() {
            val target = _state.value.deleteTarget ?: return
            val websiteId = _state.value.chrome.workspace.current?.id ?: return
            _state.update { it.copy(deleteTarget = null) }
            viewModelScope.launch {
                deleteProduct(websiteId, target.id).collect { result ->
                    if (result is DataState.Success) {
                        val toast = ToastRequest(ToastKind.Error, R.string.pv_toast_deleted_title, R.string.pv_toast_deleted_message, target.name)
                        _effects.emit(ProductsEffect.ShowToast(toast))
                    }
                }
            }
        }
    }
