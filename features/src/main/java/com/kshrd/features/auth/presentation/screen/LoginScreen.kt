package com.kshrd.features.auth.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kshrd.core.presentation.OnEachEvent
import com.kshrd.coreui.components.brand.WebBlocksLogo
import com.kshrd.coreui.components.button.GoogleButton
import com.kshrd.coreui.components.button.PrimaryButton
import com.kshrd.coreui.components.card.glassFill
import com.kshrd.coreui.components.decor.AuthBackground
import com.kshrd.coreui.components.form.LabeledField
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.auth.domain.validation.FieldError
import com.kshrd.features.auth.presentation.components.ErrorBanner
import com.kshrd.features.auth.presentation.components.GoogleChooserSheet
import com.kshrd.features.auth.presentation.components.OrDivider
import com.kshrd.features.auth.presentation.components.emailMessage
import com.kshrd.features.auth.presentation.components.passwordMessage
import com.kshrd.features.auth.presentation.components.pullUp
import com.kshrd.features.auth.presentation.model.LoginEffect
import com.kshrd.features.auth.presentation.model.LoginEvent
import com.kshrd.features.auth.presentation.model.LoginUiState
import com.kshrd.features.auth.presentation.viewmodel.LoginViewModel

private val SignInCardShape = RoundedCornerShape(Radius.hero)

/**
 * Owner sign-in (iOS `SignInView`). Accounts are created on the WebBlock website.
 * [prefillEmail] arrives from the forgot-password flow after a successful reset.
 */
@Composable
fun LoginScreen(
    onSignedIn: (showIntro: Boolean) -> Unit,
    onForgotPassword: (email: String) -> Unit,
    prefillEmail: String? = null,
    onPrefillConsumed: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnEachEvent(viewModel.effects) { effect ->
        when (effect) {
            is LoginEffect.SignedIn -> onSignedIn(effect.showIntro)
            is LoginEffect.OpenForgotPassword -> onForgotPassword(effect.email)
        }
    }
    LaunchedEffect(prefillEmail) {
        if (prefillEmail != null) {
            viewModel.onEvent(LoginEvent.EmailPrefilled(prefillEmail))
            onPrefillConsumed()
        }
    }
    LoginContent(state = state, onEvent = viewModel::onEvent)
    if (state.showGoogleChooser) {
        GoogleChooserSheet(
            isLoading = state.isLoading,
            errorMessage = state.submitError,
            onAccountClick = { viewModel.onEvent(LoginEvent.GoogleAccountClicked) },
            onDismiss = { viewModel.onEvent(LoginEvent.GoogleDismissed) },
        )
    }
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
) {
    AuthBackground {
        Box(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier =
                    Modifier
                        .widthIn(max = 520.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Space.screen, vertical = Space.l),
                verticalArrangement = Arrangement.spacedBy(Space.l),
            ) {
                WebBlocksLogo(height = 56.dp)
                Text(
                    text =
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = MaterialTheme.wb.text)) { append(stringResource(R.string.auth_login_headline_plain)) }
                            withStyle(SpanStyle(color = MaterialTheme.wb.brand)) { append(stringResource(R.string.auth_login_headline_accent)) }
                        },
                    style = MaterialTheme.typography.headlineLarge,
                )
                SignInCard(state, onEvent)
            }
        }
    }
}

@Composable
private fun SignInCard(
    state: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
) {
    val wb = MaterialTheme.wb
    val focus = LocalFocusManager.current
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(14.dp, SignInCardShape, ambientColor = wb.brand.copy(alpha = 0.12f), spotColor = wb.brand.copy(alpha = 0.12f))
                .background(glassFill(), SignInCardShape)
                .border(BorderStroke(1.dp, wb.border), SignInCardShape)
                .padding(Space.l),
        verticalArrangement = Arrangement.spacedBy(Space.m),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Space.xxxs)) {
            Text(stringResource(R.string.auth_login_welcome), style = MaterialTheme.typography.titleLarge, color = wb.text)
            Text(stringResource(R.string.auth_login_subtitle), style = MaterialTheme.typography.bodyMedium, color = wb.muted)
        }
        GoogleButton(text = stringResource(R.string.auth_login_google), onClick = { onEvent(LoginEvent.GoogleClicked) })
        OrDivider(stringResource(R.string.auth_login_or))
        LabeledField(
            title = stringResource(R.string.auth_login_email),
            value = state.email,
            onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
            placeholder = stringResource(R.string.auth_login_email_placeholder),
            error = state.emailError.emailMessage(),
            keyboardType = KeyboardType.Email,
            capitalization = KeyboardCapitalization.None,
            imeAction = ImeAction.Next,
            onImeAction = { focus.moveFocus(FocusDirection.Down) },
            enabled = !state.isLoading,
        )
        LabeledField(
            title = stringResource(R.string.auth_login_password),
            value = state.password,
            onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
            placeholder = stringResource(R.string.auth_login_password_placeholder),
            error = state.passwordError.passwordMessage(),
            isSecure = true,
            revealable = true,
            capitalization = KeyboardCapitalization.None,
            imeAction = ImeAction.Done,
            onImeAction = {
                focus.clearFocus()
                onEvent(LoginEvent.SignInClicked)
            },
            enabled = !state.isLoading,
        )
        Row(modifier = Modifier.fillMaxWidth().pullUp(Space.xs), horizontalArrangement = Arrangement.End) {
            Text(
                text = stringResource(R.string.auth_login_forgot),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = wb.brand,
                modifier = Modifier.clip(PillShape).clickable(role = Role.Button) { onEvent(LoginEvent.ForgotPasswordClicked) }.padding(vertical = Space.xs),
            )
        }
        if (state.submitError != null && !state.showGoogleChooser) ErrorBanner(state.submitError)
        PrimaryButton(
            text = stringResource(R.string.auth_login_sign_in),
            onClick = { onEvent(LoginEvent.SignInClicked) },
            loading = state.isLoading && !state.showGoogleChooser,
        )
        Text(
            text = stringResource(R.string.auth_login_footer),
            style = MaterialTheme.typography.bodySmall,
            color = wb.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@ThemePreviews
@Composable
private fun LoginContentPreview() {
    PreviewSurface(padding = 0) { LoginContent(state = LoginUiState(email = "owner@example.com"), onEvent = {}) }
}

@ThemePreviews
@Composable
private fun LoginContentErrorPreview() {
    PreviewSurface(padding = 0) {
        LoginContent(
            state =
                LoginUiState(
                    email = "bad",
                    emailError = FieldError.InvalidEmail,
                    passwordError = FieldError.Empty,
                    submitError = "Email or password is incorrect. Check them and try again.",
                ),
            onEvent = {},
        )
    }
}
