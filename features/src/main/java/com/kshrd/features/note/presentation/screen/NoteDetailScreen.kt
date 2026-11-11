package com.kshrd.features.note.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.UiState
import com.kshrd.coreui.components.feedback.EmptyStateView
import com.kshrd.coreui.components.screen.DataStateScreenShell
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space
import com.kshrd.features.R
import com.kshrd.features.note.domain.model.Note
import com.kshrd.features.note.presentation.components.NoteListSkeleton
import com.kshrd.features.note.presentation.components.previewNote
import com.kshrd.features.note.presentation.viewmodel.NoteDetailViewModel

@Composable
fun NoteDetailScreen(
    onBack: () -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NoteDetailContent(state = state, onBack = onBack, onRetry = viewModel::load)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailContent(
    state: UiState<Note>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.note_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        DataStateScreenShell(
            state = state,
            modifier = Modifier.fillMaxSize().padding(padding),
            loading = { NoteListSkeleton() },
            error = {
                EmptyStateView(
                    title = stringResource(R.string.note_error_title),
                    message = (state as? DataState.Error)?.message,
                    actionLabel = stringResource(R.string.action_retry),
                    onAction = onRetry,
                )
            },
            success = { note ->
                Column(modifier = Modifier.padding(Space.m)) {
                    Text(note.title, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        note.createdAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Space.xxs),
                    )
                    Text(note.body, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = Space.m))
                }
            },
        )
    }
}

@ThemePreviews
@Composable
private fun NoteDetailContentPreview() {
    PreviewSurface(padding = 0) {
        NoteDetailContent(state = DataState.Success(previewNote), onBack = {}, onRetry = {})
    }
}
