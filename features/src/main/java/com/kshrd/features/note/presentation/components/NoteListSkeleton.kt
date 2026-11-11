package com.kshrd.features.note.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kshrd.coreui.components.skeleton.SkeletonBox
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space

private const val SKELETON_ROWS = 4
private const val TITLE_HEIGHT = 20
private const val BODY_HEIGHT = 14
private const val DATE_HEIGHT = 12
private const val TITLE_WIDTH_FRACTION = 0.6f
private const val DATE_WIDTH_FRACTION = 0.3f

/** Feature-specific skeleton built from the shared `SkeletonBox` primitive. */
@Composable
fun NoteListSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(SKELETON_ROWS) {
            Column(modifier = Modifier.padding(horizontal = Space.m, vertical = Space.s)) {
                SkeletonBox(modifier = Modifier.fillMaxWidth(TITLE_WIDTH_FRACTION), height = TITLE_HEIGHT)
                SkeletonBox(modifier = Modifier.fillMaxWidth().padding(top = Space.xs), height = BODY_HEIGHT)
                SkeletonBox(modifier = Modifier.fillMaxWidth(DATE_WIDTH_FRACTION).padding(top = Space.xs), height = DATE_HEIGHT)
            }
        }
    }
}

@ThemePreviews
@Composable
private fun NoteListSkeletonPreview() {
    PreviewSurface(padding = 0) { NoteListSkeleton() }
}
