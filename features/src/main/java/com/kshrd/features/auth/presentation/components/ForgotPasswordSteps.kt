package com.kshrd.features.auth.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.button.PrimaryButton
import com.kshrd.coreui.components.card.glassFill
import com.kshrd.coreui.components.form.LabeledField
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.auth.domain.validation.AuthValidator
import com.kshrd.features.auth.presentation.model.ForgotPasswordEvent
import com.kshrd.features.auth.presentation.model.ForgotPasswordUiState
import com.kshrd.features.auth.presentation.model.ResetError

private const val STRENGTH_SEGMENTS = 4

@Composable
private fun ResetError?.message(): String? =
    when (this) {
        null -> null
        ResetError.EmailEmpty -> stringResource(R.string.auth_error_email_empty)
        ResetError.EmailInvalid -> stringResource(R.string.auth_error_email_invalid)
        ResetError.CodeExpired -> stringResource(R.string.auth_error_code_expired)
        ResetError.CodeIncomplete -> stringResource(R.string.auth_error_code_incomplete)
        ResetError.PasswordRules -> stringResource(R.string.auth_error_password_rules)
        ResetError.ConfirmEmpty -> stringResource(R.string.auth_error_confirm_empty)
        ResetError.Mismatch -> stringResource(R.string.auth_error_mismatch)
    }

private val ForgotPasswordUiState.errorText: String?
    @Composable get() = formError.message() ?: serviceError

/** Title + subtitle shared by the three steps (iOS `StepHeading`). */
@Composable
private fun StepHeading(
    title: String,
    subtitle: androidx.compose.ui.text.AnnotatedString,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = Space.s),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.wb.text, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.wb.muted, textAlign = TextAlign.Center)
    }
}

/** "prefix **email** suffix" with the email set in semibold text colour. */
@Composable
private fun emailSubtitle(
    before: String,
    email: String,
    after: String,
) = buildAnnotatedString {
    append(before)
    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = MaterialTheme.wb.text)) { append(email) }
    append(after)
}

@Composable
fun EmailStep(
    state: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
        StepHeading(
            stringResource(R.string.auth_forgot_title),
            buildAnnotatedString { append(stringResource(R.string.auth_forgot_subtitle)) },
        )
        LabeledField(
            title = stringResource(R.string.auth_forgot_email),
            value = state.email,
            onValueChange = { onEvent(ForgotPasswordEvent.EmailChanged(it)) },
            placeholder = stringResource(R.string.auth_login_email_placeholder),
            error = if (state.email.isEmpty() || state.isEmailValid) null else stringResource(R.string.auth_error_email_invalid),
            hint = stringResource(R.string.auth_forgot_hint),
            keyboardType = KeyboardType.Email,
            capitalization = KeyboardCapitalization.None,
            imeAction = androidx.compose.ui.text.input.ImeAction.Done,
            onImeAction = { onEvent(ForgotPasswordEvent.PrimaryClicked) },
            enabled = !state.isLoading,
        )
        ErrorLine(state.errorText)
        PrimaryButton(
            text = stringResource(R.string.auth_forgot_send_code),
            onClick = { onEvent(ForgotPasswordEvent.PrimaryClicked) },
            loading = state.isLoading,
        )
    }
}

@Composable
fun CodeStep(
    state: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit,
) {
    val wb = MaterialTheme.wb
    Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
        StepHeading(
            stringResource(R.string.auth_code_title),
            emailSubtitle(stringResource(R.string.auth_code_subtitle_before), state.email, stringResource(R.string.auth_code_subtitle_after)),
        )
        CodeBoxes(code = state.code, onCodeChange = { onEvent(ForgotPasswordEvent.CodeChanged(it)) })
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = wb.muted, modifier = Modifier.size(Sizes.iconSmall))
            Text(
                text =
                    buildAnnotatedString {
                        append(stringResource(R.string.auth_code_expires_in))
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = wb.text)) { append(state.timerText) }
                    },
                style = MaterialTheme.typography.bodySmall,
                color = wb.muted,
                modifier = Modifier.padding(start = Space.xxs).weight(1f),
            )
            Text(
                text = stringResource(R.string.auth_code_resend),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = wb.brand,
                modifier = Modifier.clickable(enabled = !state.isLoading) { onEvent(ForgotPasswordEvent.ResendClicked) }.padding(vertical = Space.xs),
            )
        }
        ErrorLine(state.errorText)
        PrimaryButton(
            text = stringResource(R.string.auth_code_verify),
            onClick = { onEvent(ForgotPasswordEvent.PrimaryClicked) },
            loading = state.isLoading,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space.xxs, Alignment.CenterHorizontally),
        ) {
            Text(stringResource(R.string.auth_code_spam), style = MaterialTheme.typography.bodySmall, color = wb.muted)
            Text(
                stringResource(R.string.auth_code_change_email),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = wb.brand,
                modifier = Modifier.clickable { onEvent(ForgotPasswordEvent.ChangeEmailClicked) },
            )
        }
    }
}

/** Six boxes backed by one invisible numeric field, so the system keyboard and paste-from-SMS work. */
@Composable
private fun CodeBoxes(
    code: String,
    onCodeChange: (String) -> Unit,
) {
    val wb = MaterialTheme.wb
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    BasicTextField(
        value = code,
        onValueChange = onCodeChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        cursorBrush = SolidColor(Color.Transparent),
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onFocusChanged { focused = it.isFocused },
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                repeat(AuthValidator.CODE_LENGTH) { index ->
                    val current = focused && index == minOf(code.length, AuthValidator.CODE_LENGTH - 1)
                    val shape = RoundedCornerShape(Radius.panel)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(62.dp)
                                .scale(if (index < code.length) 1f else 0.97f)
                                .background(glassFill(), shape)
                                .border(if (current) 1.5.dp else 1.dp, if (current) wb.brand else wb.border, shape),
                    ) {
                        if (index < code.length) {
                            Text(code[index].toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = wb.text)
                        } else if (current) {
                            BlinkingCaret()
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun BlinkingCaret() {
    val alpha by rememberInfiniteTransition(label = "caret").animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 550), RepeatMode.Reverse),
        label = "caret-alpha",
    )
    Box(Modifier.width(2.dp).height(26.dp).background(MaterialTheme.wb.brand.copy(alpha = alpha)))
}

@Composable
fun PasswordStep(
    state: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit,
) {
    val wb = MaterialTheme.wb
    val checks = state.checks
    val strengthTint =
        when (checks.strength) {
            1 -> wb.red
            2 -> wb.amber
            else -> wb.green
        }
    val strengthLabel =
        when (checks.strength) {
            1 -> stringResource(R.string.auth_strength_weak)
            2 -> stringResource(R.string.auth_strength_fair)
            3 -> stringResource(R.string.auth_strength_good)
            4 -> stringResource(R.string.auth_strength_strong)
            else -> ""
        }
    Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
        StepHeading(
            stringResource(R.string.auth_new_title),
            emailSubtitle(stringResource(R.string.auth_new_subtitle_before), state.email, stringResource(R.string.auth_new_subtitle_after)),
        )
        LabeledField(
            title = stringResource(R.string.auth_new_password),
            value = state.newPassword,
            onValueChange = { onEvent(ForgotPasswordEvent.NewPasswordChanged(it)) },
            placeholder = stringResource(R.string.auth_new_password_placeholder),
            isSecure = true,
            revealable = true,
            capitalization = KeyboardCapitalization.None,
            enabled = !state.isLoading,
        )
        Column(verticalArrangement = Arrangement.spacedBy(Space.xxs)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.xxs)) {
                repeat(STRENGTH_SEGMENTS) { index ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(if (index < checks.strength) strengthTint else wb.border, CircleShape),
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.auth_new_strength), style = MaterialTheme.typography.bodySmall, color = wb.muted)
                Text(strengthLabel, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = strengthTint)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
            Rule(stringResource(R.string.auth_rule_length), checks.hasLength)
            Rule(stringResource(R.string.auth_rule_upper_number), checks.hasUpperAndNumber)
            Rule(stringResource(R.string.auth_rule_symbol), checks.hasSymbol)
        }
        LabeledField(
            title = stringResource(R.string.auth_new_confirm),
            value = state.confirm,
            onValueChange = { onEvent(ForgotPasswordEvent.ConfirmChanged(it)) },
            placeholder = stringResource(R.string.auth_new_confirm_placeholder),
            error = if (state.confirm.isNotEmpty() && !state.matches) stringResource(R.string.auth_error_mismatch) else null,
            isSecure = true,
            revealable = true,
            capitalization = KeyboardCapitalization.None,
            imeAction = androidx.compose.ui.text.input.ImeAction.Done,
            onImeAction = { onEvent(ForgotPasswordEvent.PrimaryClicked) },
            enabled = !state.isLoading,
        )
        ErrorLine(state.errorText)
        PrimaryButton(
            text = stringResource(R.string.auth_new_update),
            onClick = { onEvent(ForgotPasswordEvent.PrimaryClicked) },
            loading = state.isLoading,
        )
        Text(
            stringResource(R.string.auth_new_footer),
            style = MaterialTheme.typography.bodySmall,
            color = wb.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun Rule(
    text: String,
    ok: Boolean,
) {
    val wb = MaterialTheme.wb
    val tint = if (ok) wb.green else wb.muted
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
        Icon(if (ok) Icons.Filled.CheckCircle else Icons.Outlined.Circle, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = tint)
    }
}

@ThemePreviews
@Composable
private fun CodeStepPreview() {
    PreviewSurface {
        CodeStep(ForgotPasswordUiState(email = "owner@example.com", code = "123", secondsLeft = 272), onEvent = {})
    }
}

@ThemePreviews
@Composable
private fun PasswordStepPreview() {
    PreviewSurface {
        PasswordStep(ForgotPasswordUiState(email = "owner@example.com", newPassword = "Abcdef12", confirm = "Abc"), onEvent = {})
    }
}
