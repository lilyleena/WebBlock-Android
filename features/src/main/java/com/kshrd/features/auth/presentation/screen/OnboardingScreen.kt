package com.kshrd.features.auth.presentation.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kshrd.core.presentation.OnEachEvent
import com.kshrd.coreui.components.brand.WebBlocksLogo
import com.kshrd.coreui.components.button.PrimaryButton
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.overline
import com.kshrd.coreui.theme.paragraph
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.auth.presentation.components.MonitorIllustration
import com.kshrd.features.auth.presentation.components.OnboardingBackdrop
import com.kshrd.features.auth.presentation.components.ProductsIllustration
import com.kshrd.features.auth.presentation.components.StepsIllustration
import com.kshrd.features.auth.presentation.components.WebsiteIllustration
import com.kshrd.features.auth.presentation.model.OnboardingEffect
import com.kshrd.features.auth.presentation.model.OnboardingEvent
import com.kshrd.features.auth.presentation.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

private class OnboardingPage(
    val eyebrow: Int,
    val title: Int,
    val accent: Int,
    val message: Int,
)

private val Pages =
    listOf(
        OnboardingPage(R.string.auth_ob_eyebrow_1, R.string.auth_ob_title_1, R.string.auth_ob_accent_1, R.string.auth_ob_message_1),
        OnboardingPage(R.string.auth_ob_eyebrow_2, R.string.auth_ob_title_2, R.string.auth_ob_accent_2, R.string.auth_ob_message_2),
        OnboardingPage(R.string.auth_ob_eyebrow_3, R.string.auth_ob_title_3, R.string.auth_ob_accent_3, R.string.auth_ob_message_3),
        OnboardingPage(R.string.auth_ob_eyebrow_4, R.string.auth_ob_title_4, R.string.auth_ob_accent_4, R.string.auth_ob_message_4),
    )

/** First-launch intro shown before sign-in (iOS `OnboardingView`): four pages, then "Get started". */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    OnEachEvent(viewModel.effects) { effect ->
        when (effect) {
            OnboardingEffect.Finished -> onFinished()
        }
    }
    OnboardingContent(onFinish = { viewModel.onEvent(OnboardingEvent.Finish) })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingContent(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { Pages.size })
    val scope = rememberCoroutineScope()
    val page = pagerState.currentPage
    val isLast = page == Pages.lastIndex

    OnboardingBackdrop {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = Space.screen),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WebBlocksLogo(height = 48.dp)
                Box(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.auth_ob_skip),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.wb.muted,
                    modifier =
                        Modifier
                            .alpha(if (isLast) 0f else 1f)
                            .clickable(
                                enabled = !isLast,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onFinish,
                            ).padding(vertical = Space.s, horizontal = Space.xs),
                )
            }
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
                PageContent(index = index, active = page == index)
            }
            PagerDots(count = Pages.size, selected = page)
            PrimaryButton(
                text = stringResource(if (isLast) R.string.auth_ob_get_started else R.string.auth_ob_next),
                onClick = { if (isLast) onFinish() else scope.launch { pagerState.animateScrollToPage(page + 1, animationSpec = tween(400)) } },
                modifier = Modifier.padding(horizontal = Space.screen).padding(bottom = Space.m),
            )
        }
    }
}

@Composable
private fun PageContent(
    index: Int,
    active: Boolean,
) {
    val wb = MaterialTheme.wb
    val item = Pages[index]
    val reveal by animateFloatAsState(if (active) 1f else 0f, tween(450), label = "copy-reveal")
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Space.l, Alignment.CenterVertically),
    ) {
        Box(modifier = Modifier.clearAndSetSemantics { }) {
            when (index) {
                0 -> WebsiteIllustration(active)
                1 -> StepsIllustration(active)
                2 -> ProductsIllustration(active)
                else -> MonitorIllustration(active)
            }
        }
        Column(
            modifier =
                Modifier
                    .widthIn(max = 360.dp)
                    .padding(horizontal = Space.xl)
                    .graphicsLayer {
                        alpha = reveal
                        translationY = (1f - reveal) * 14.dp.toPx()
                    },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space.s),
        ) {
            Text(
                text = stringResource(item.eyebrow),
                style = MaterialTheme.typography.overline,
                color = wb.brand,
            )
            Text(
                text =
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = wb.text)) { append(stringResource(item.title)) }
                        withStyle(SpanStyle(color = wb.brand)) { append(stringResource(item.accent)) }
                    },
                style = MaterialTheme.typography.headlineMedium.copy(lineBreak = LineBreak.Heading),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(item.message),
                style = MaterialTheme.typography.paragraph,
                color = wb.muted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PagerDots(
    count: Int,
    selected: Int,
) {
    val brand = MaterialTheme.wb.brand
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = Space.l).clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(Space.xs, Alignment.CenterHorizontally),
    ) {
        repeat(count) { index ->
            val width by animateDpAsState(if (index == selected) 26.dp else 8.dp, spring(dampingRatio = 0.75f, stiffness = 300f), label = "dot-width")
            Box(Modifier.width(width).height(8.dp).background(if (index == selected) brand else brand.copy(alpha = 0.22f), PillShape))
        }
    }
}

@ThemePreviews
@Composable
private fun OnboardingContentPreview() {
    PreviewSurface(padding = 0) { OnboardingContent(onFinish = {}) }
}
