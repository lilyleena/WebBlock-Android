package com.kshrd.features.products.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.kshrd.coreui.components.feedback.ToastKind

/** A toast the screen should show. [arg] fills the `%1$s` in the message (for example a product name). */
@Immutable
data class ToastRequest(
    val kind: ToastKind,
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int,
    val arg: String? = null,
)
