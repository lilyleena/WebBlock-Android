@file:Suppress("MagicNumber") // Fixed-size illustration: every number is a pixel measurement from the iOS drawing.

package com.kshrd.features.auth.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R

// The mobile site is drawn in its own fixed palette (it is a picture of a website), so it does not follow the app theme.
private val SiteInk = Color(0xFF1F2D5A)
private val SiteMuted = Color(0xFF6B7796)
private val SiteLine = Color(0xFFDDE3EE)
private val Bezel = Color(0xFF0E1421)
private val SideButton = Color(0xFF6A7388)

/**
 * A phone (steel rim, black bezel, dynamic island, status bar) showing the shop's mobile page,
 * styled after the petalandstem.webblock.com mobile site (iOS `PhoneMockup`).
 */
@Composable
fun PhoneMockup(modifier: Modifier = Modifier) {
    val rim = Brush.linearGradient(listOf(Color(0xFFA3ABBA), Color(0xFF4A5368), Color(0xFF9AA2B2)))
    Box(modifier = modifier.size(width = 118.dp, height = 240.dp).shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Bezel, spotColor = Bezel)) {
        Box(Modifier.fillMaxSize().background(rim, RoundedCornerShape(24.dp)))
        Box(Modifier.fillMaxSize().padding(1.5.dp).background(Bezel, RoundedCornerShape(22.5.dp)))
        PhoneScreen(Modifier.fillMaxSize().padding(4.5.dp).clip(RoundedCornerShape(19.dp)))
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 9.dp)
                .size(width = 34.dp, height = 10.dp)
                .background(Color.Black, CircleShape),
        )
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 2.dp, y = (-28).dp)
                .size(width = 2.5.dp, height = 30.dp)
                .background(SideButton, CircleShape),
        )
        Column(
            modifier = Modifier.align(Alignment.CenterStart).offset(x = (-2).dp, y = (-36).dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            listOf(9.dp, 15.dp, 15.dp).forEach { Box(Modifier.size(width = 2.5.dp, height = it).background(SideButton, CircleShape)) }
        }
    }
}

@Composable
private fun PhoneScreen(modifier: Modifier) {
    Box(modifier = modifier.background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            StatusBar()
            SiteHeader()
            HeroCard()
            Image(
                painter = painterResource(R.drawable.blue_lily_stem),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .padding(horizontal = 6.dp)
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(bottomStart = 9.dp, bottomEnd = 9.dp)),
            )
            Text(
                text = stringResource(R.string.auth_ob_phone_popular),
                fontSize = fixedSp(3.5f),
                fontWeight = FontWeight.Medium,
                color = SiteMuted,
                modifier =
                    Modifier
                        .padding(top = 7.dp)
                        .border(0.6.dp, SiteLine, CircleShape)
                        .padding(horizontal = 7.dp, vertical = 3.dp),
            )
            Text(
                text = stringResource(R.string.auth_ob_phone_picks),
                fontSize = fixedSp(7f),
                fontWeight = FontWeight.ExtraBold,
                color = SiteInk,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier.padding(horizontal = 6.dp).padding(top = 5.dp),
            )
        }
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
                .size(width = 36.dp, height = 2.5.dp)
                .background(Bezel, CircleShape),
        )
    }
}

@Composable
private fun StatusBar() {
    Row(
        modifier = Modifier.fillMaxWidth().height(24.dp).padding(horizontal = 11.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(stringResource(R.string.auth_ob_phone_time), fontSize = fixedSp(6.5f), fontWeight = FontWeight.SemiBold, color = Bezel)
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.SignalCellularAlt, null, tint = Bezel, modifier = Modifier.size(5.5.dp))
            Icon(Icons.Filled.Wifi, null, tint = Bezel, modifier = Modifier.size(5.5.dp))
            Icon(Icons.Filled.BatteryFull, null, tint = Bezel, modifier = Modifier.size(7.dp))
        }
    }
}

@Composable
private fun SiteHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(stringResource(R.string.auth_ob_phone_logo), fontSize = fixedSp(5f), fontWeight = FontWeight.Bold, color = SiteMuted.copy(alpha = 0.7f))
        Spacer(Modifier.weight(1f))
        Box(
            Modifier.size(width = 24.dp, height = 9.dp).background(MaterialTheme.wb.brand, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.auth_ob_phone_contact), fontSize = fixedSp(4.5f), fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.5.dp)) {
            repeat(3) { Box(Modifier.size(width = 7.dp, height = 0.8.dp).background(SiteMuted.copy(alpha = 0.6f), CircleShape)) }
        }
    }
}

@Composable
private fun HeroCard() {
    Column(
        modifier =
            Modifier
                .padding(horizontal = 6.dp)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFF4F6FB), Color(0xFFE3E7F0))),
                    RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp),
                ).padding(7.dp),
        verticalArrangement = Arrangement.spacedBy(3.5.dp),
    ) {
        Text(
            stringResource(R.string.auth_ob_phone_headline),
            fontSize = fixedSp(8.6f),
            lineHeight = fixedSp(10f),
            fontWeight = FontWeight.ExtraBold,
            color = SiteInk,
        )
        Text(
            stringResource(R.string.auth_ob_phone_body),
            fontSize = fixedSp(4.2f),
            lineHeight = fixedSp(5.7f),
            color = SiteMuted,
        )
        PhoneButton(stringResource(R.string.auth_ob_shop_flowers), fill = Color(0xFF3A5FBE), text = Color.White, outlined = false, modifier = Modifier.padding(top = 2.dp))
        PhoneButton(stringResource(R.string.auth_ob_phone_visit), fill = Color.White, text = SiteInk, outlined = true)
    }
}

@Composable
private fun PhoneButton(
    label: String,
    fill: Color,
    text: Color,
    outlined: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(13.dp)
                .background(fill, CircleShape)
                .then(if (outlined) Modifier.border(0.8.dp, SiteLine, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = fixedSp(5.5f), fontWeight = FontWeight.SemiBold, color = text, textAlign = TextAlign.Center)
    }
}

@ThemePreviews
@Composable
private fun PhoneMockupPreview() {
    PreviewSurface { Box(Modifier.padding(16.dp)) { PhoneMockup() } }
}
