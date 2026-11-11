package com.kshrd.features.note.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.kshrd.coreui.preview.FontScalePreviews
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space
import com.kshrd.features.note.domain.model.Note

/** Stateless row. It knows nothing about ViewModels: data in, callbacks out. */
@Composable
fun NoteRow(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = Space.m, vertical = Space.s),
    ) {
        Text(text = note.title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = note.body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = Space.xxs),
        )
        Text(
            text = note.createdAt,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Space.xxs),
        )
    }
}

internal val previewNote = Note(1, "Read the architecture doc", "docs/ARCHITECTURE.md explains the module graph.", "2026-10-01T09:00:00")

@ThemePreviews
@FontScalePreviews
@Composable
private fun NoteRowPreview() {
    PreviewSurface(padding = 0) {
        NoteRow(note = previewNote, onClick = {})
    }
}
