package com.kshrd.features.products.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.kshrd.coreui.components.button.AppTextAction
import com.kshrd.coreui.components.button.PrimaryButton
import com.kshrd.coreui.components.button.SecondaryButton
import com.kshrd.coreui.components.decor.LiquidBackdrop
import com.kshrd.coreui.components.feedback.WbAlertDialog
import com.kshrd.coreui.components.form.AppDropdownField
import com.kshrd.coreui.components.form.LabeledField
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.SheetShape
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.products.domain.model.ProductFormError
import com.kshrd.features.products.presentation.model.ProductEditorEvent
import com.kshrd.features.products.presentation.model.ProductEditorUiState

/**
 * Add / edit product, as a full-height sheet on the portal backdrop: a back arrow, the title and a small
 * Save on top; the photo row; name, price, description and category; Cancel and Save at the bottom.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditorSheet(
    state: ProductEditorUiState,
    onEvent: (ProductEditorEvent) -> Unit,
) {
    // The system photo picker needs no storage permission and works with the gallery or Google Photos.
    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) onEvent(ProductEditorEvent.PhotoPicked(uri.toString()))
        }
    ModalBottomSheet(
        onDismissRequest = { onEvent(ProductEditorEvent.CancelClicked) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.wb.background,
        dragHandle = null,
        shape = SheetShape,
    ) {
        LiquidBackdrop {
            ProductEditorContent(
                state = state,
                onEvent = onEvent,
                onPickPhoto = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            )
        }
    }
    if (state.showCreateCategory) CreateCategoryDialog(state, onEvent)
}

@Composable
fun ProductEditorContent(
    state: ProductEditorUiState,
    onEvent: (ProductEditorEvent) -> Unit,
    onPickPhoto: () -> Unit,
) {
    val save = { onEvent(ProductEditorEvent.SaveClicked) }
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        EditorTopBar(
            title = stringResource(if (state.isNew) R.string.pv_editor_add_title else R.string.pv_editor_edit_title),
            saving = state.isSaving,
            canSave = !state.isUploadingPhoto,
            onBack = { onEvent(ProductEditorEvent.CancelClicked) },
            onSave = save,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(Space.m),
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = Space.screen, end = Space.screen, top = Space.xs, bottom = Space.l),
        ) {
            ProductPhotoField(
                imageUrl = state.imageUrl,
                isUploading = state.isUploadingPhoto,
                error = state.photoError,
                onPick = onPickPhoto,
                onRemove = { onEvent(ProductEditorEvent.PhotoRemoved) },
            )
            EditorFields(state, onEvent)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(Space.s),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.wb.background.copy(alpha = 0.92f))
                    .navigationBarsPadding()
                    .padding(horizontal = Space.screen, vertical = Space.s),
        ) {
            SecondaryButton(text = stringResource(R.string.pv_cancel), onClick = { onEvent(ProductEditorEvent.CancelClicked) }, modifier = Modifier.weight(1f))
            PrimaryButton(
                text = stringResource(R.string.pv_save),
                onClick = save,
                enabled = !state.isUploadingPhoto,
                loading = state.isSaving,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Name, price ("$" prefix), description and the category dropdown (with "New category" at its end). */
@Composable
private fun EditorFields(
    state: ProductEditorUiState,
    onEvent: (ProductEditorEvent) -> Unit,
) {
    val wb = MaterialTheme.wb
    LabeledField(
        title = stringResource(R.string.pv_name_label),
        value = state.name,
        onValueChange = { onEvent(ProductEditorEvent.NameChanged(it)) },
        placeholder = stringResource(R.string.pv_name_placeholder),
        error = if (ProductFormError.NameEmpty in state.errors) stringResource(R.string.pv_error_name) else null,
        capitalization = KeyboardCapitalization.Words,
    )
    LabeledField(
        title = stringResource(R.string.pv_price_label),
        value = state.price,
        onValueChange = { onEvent(ProductEditorEvent.PriceChanged(it)) },
        placeholder = stringResource(R.string.pv_price_placeholder),
        error = if (ProductFormError.PriceInvalid in state.errors) stringResource(R.string.pv_error_price) else null,
        keyboardType = KeyboardType.Decimal,
        prefix = stringResource(R.string.pv_price_prefix),
    )
    LabeledField(
        title = stringResource(R.string.pv_description_label),
        value = state.description,
        onValueChange = { onEvent(ProductEditorEvent.DescriptionChanged(it)) },
        placeholder = stringResource(R.string.pv_description_placeholder),
        isOptional = true,
        minLines = 3,
        imeAction = ImeAction.Default,
    )
    AppDropdownField(
        label = stringResource(R.string.pv_category_label),
        value = state.category,
        options = state.categories,
        onSelect = { onEvent(ProductEditorEvent.CategoryChanged(it)) },
        placeholder = stringResource(R.string.pv_category_placeholder),
        error = if (ProductFormError.CategoryEmpty in state.errors) stringResource(R.string.pv_error_category) else null,
        leadingIcon = Icons.Outlined.Sell,
        iconTint = wb.purple,
        iconContainer = wb.purpleSoft,
        extraActionLabel = stringResource(R.string.pv_category_create),
        onExtraAction = { onEvent(ProductEditorEvent.CreateCategoryClicked) },
    )
}

/** "‹ Edit product" on the left, a small Save pill on the right. */
@Composable
private fun EditorTopBar(
    title: String,
    saving: Boolean,
    canSave: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    val wb = MaterialTheme.wb
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(start = Space.xxs, end = Space.screen, top = Space.s, bottom = Space.xs),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(Sizes.compact).clip(CircleShape).clickable(role = Role.Button, onClick = onBack),
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBackIos,
                contentDescription = stringResource(R.string.pv_editor_close),
                tint = wb.text,
                modifier = Modifier.padding(start = Space.xxs).size(Sizes.icon),
            )
        }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = wb.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        PrimaryButton(text = stringResource(R.string.pv_save), onClick = onSave, enabled = canSave, loading = saving, fullWidth = false, compact = true)
    }
}

@Composable
private fun CreateCategoryDialog(
    state: ProductEditorUiState,
    onEvent: (ProductEditorEvent) -> Unit,
) {
    WbAlertDialog(
        title = stringResource(R.string.pv_new_category_title),
        onDismissRequest = { onEvent(ProductEditorEvent.NewCategoryDismissed) },
        text = {
            LabeledField(
                title = stringResource(R.string.pv_new_category_hint),
                value = state.newCategory,
                onValueChange = { onEvent(ProductEditorEvent.NewCategoryChanged(it)) },
                error = state.newCategoryError,
                imeAction = ImeAction.Done,
                onImeAction = { onEvent(ProductEditorEvent.NewCategoryConfirmed) },
            )
        },
        confirmButton = {
            AppTextAction(text = stringResource(R.string.pv_new_category_add), onClick = { onEvent(ProductEditorEvent.NewCategoryConfirmed) })
        },
        dismissButton = {
            AppTextAction(text = stringResource(R.string.pv_cancel), onClick = { onEvent(ProductEditorEvent.NewCategoryDismissed) })
        },
    )
}

@ThemePreviews
@Composable
private fun ProductEditorContentPreview() {
    PreviewSurface(padding = 0) {
        LiquidBackdrop {
            ProductEditorContent(
                state =
                    ProductEditorUiState(
                        productId = "1",
                        name = "Gift bouquet",
                        category = "Bouquets",
                        price = "38.00",
                        description = "A hand-tied bouquet of seasonal flowers.",
                        imageUrl = "drawable:gift_bouquet",
                        categories = listOf("Flowers", "Bouquets", "Gifts", "Events"),
                        errors = setOf(ProductFormError.PriceInvalid),
                    ),
                onEvent = {},
                onPickPhoto = {},
            )
        }
    }
}
