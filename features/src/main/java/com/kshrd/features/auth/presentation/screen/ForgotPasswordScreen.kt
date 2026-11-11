package com.kshrd.features.auth.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kshrd.core.presentation.OnEachEvent
import com.kshrd.coreui.components.decor.LiquidBackdrop
import com.kshrd.coreui.components.nav.BackTopBar
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.auth.presentation.components.CodeStep
import com.kshrd.features.auth.presentation.components.EmailStep
import com.kshrd.features.auth.presentation.components.PasswordStep
import com.kshrd.features.auth.presentation.model.ForgotPasswordEffect
import com.kshrd.features.auth.presentation.model.ForgotPasswordEvent
import com.kshrd.features.auth.presentation.model.ForgotPasswordUiState
import com.kshrd.features.auth.presentation.model.ResetStep
import com.kshrd.features.auth.presentation.viewmodel.ForgotPasswordViewModel

private const val TOTAL_STEPS = 3

/** Three-step reset (iOS `ForgotPasswordFlow`). [onFinish] gets the email when the password was updated. */
@Composable
fun ForgotPasswordScreen(
    onFinish: (changedEmail: String?) -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnEachEvent(viewModel.effects) { effect ->
        when (effect) {
            is ForgotPasswordEffect.Finished -> onFinish(effect.changedEmail)
        }
    }
    BackHandler { viewModel.onEvent(ForgotPasswordEvent.BackClicked) }
    ForgotPasswordContent(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun ForgotPasswordContent(
    state: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit,
) {
    LiquidBackdrop {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            // Same back bar as every pushed screen, with the step as its title.
            BackTopBar(
                backLabel = stringResource(R.string.auth_back),
                onBack = { onEvent(ForgotPasswordEvent.BackClicked) },
                title = stringResource(R.string.auth_step_format, state.step.number),
            )
            StepProgress(current = state.step.number, modifier = Modifier.padding(top = Space.xs))
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    targetState = state.step,
                    transitionSpec = {
                        val dir = if (state.goingBack) -1 else 1
                        (slideInHorizontally { it * dir } + fadeIn()) togetherWith (slideOutHorizontally { -it * dir } + fadeOut())
                    },
                    label = "reset-step",
                ) { step ->
                    Column(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = Space.screen)
                                .padding(top = Space.l, bottom = Space.l),
                    ) {
                        when (step) {
                            ResetStep.Email -> EmailStep(state, onEvent)
                            ResetStep.Code -> CodeStep(state, onEvent)
                            ResetStep.NewPassword -> PasswordStep(state, onEvent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepProgress(
    current: Int,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = Space.screen),
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        repeat(TOTAL_STEPS) { index ->
            val fill by animateFloatAsState(if (index + 1 <= current) 1f else 0f, spring(dampingRatio = 0.8f, stiffness = 130f), label = "step-fill")
            // Web step bars: thin, flat brand fill on a soft brand track.
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(PillShape)
                    .background(wb.brand.copy(alpha = 0.16f)),
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fill)
                        .background(wb.brand, PillShape),
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun ForgotPasswordEmailPreview() {
    PreviewSurface(padding = 0) { ForgotPasswordContent(ForgotPasswordUiState(email = "owner@example.com"), onEvent = {}) }
}

@ThemePreviews
@Composable
private fun ForgotPasswordCodePreview() {
    PreviewSurface(padding = 0) {
        ForgotPasswordContent(ForgotPasswordUiState(step = ResetStep.Code, email = "owner@example.com", code = "12"), onEvent = {})
    }
}

@ThemePreviews
@Composable
private fun ForgotPasswordPasswordPreview() {
    PreviewSurface(padding = 0) {
        ForgotPasswordContent(ForgotPasswordUiState(step = ResetStep.NewPassword, email = "owner@example.com", newPassword = "Abcdef12"), onEvent = {})
    }
}
