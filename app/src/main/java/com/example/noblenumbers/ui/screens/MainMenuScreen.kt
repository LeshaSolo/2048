package com.example.noblenumbers.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noblenumbers.R
import com.example.noblenumbers.ui.localizedString
import com.example.noblenumbers.ui.theme.NoblePalette

@Composable
fun MainMenuScreen(
    hasSavedGame: Boolean,
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
    onRecords: () -> Unit,
    onSettings: () -> Unit,
    onExit: () -> Unit = {},
) {
    WoodScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 430.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = localizedString(R.string.app_name).uppercase(),
                    modifier = Modifier.fillMaxWidth(),
                    color = NoblePalette.GoldLight,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontSize = 45.sp,
                        lineHeight = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.85f),
                            blurRadius = 4f,
                        ),
                    ),
                )
                Spacer(Modifier.height(36.dp))
                MenuImageButton(
                    drawableRes = R.drawable.menu_button_new_game,
                    contentDescription = localizedString(R.string.new_game),
                    aspectRatio = 3.27f,
                    onClick = onNewGame,
                )
                Spacer(Modifier.height(14.dp))
                MenuImageButton(
                    drawableRes = R.drawable.menu_button_continue,
                    contentDescription = localizedString(R.string.continue_text),
                    aspectRatio = 3.10f,
                    enabled = hasSavedGame,
                    onClick = onContinue,
                )
                Spacer(Modifier.height(12.dp))
                MenuImageButton(
                    drawableRes = R.drawable.menu_button_records,
                    contentDescription = localizedString(R.string.records),
                    aspectRatio = 3.14f,
                    onClick = onRecords,
                )
                Spacer(Modifier.height(12.dp))
                MenuImageButton(
                    drawableRes = R.drawable.menu_button_settings,
                    contentDescription = localizedString(R.string.settings),
                    aspectRatio = 3.61f,
                    onClick = onSettings,
                )
                Spacer(Modifier.height(12.dp))
                MenuImageButton(
                    drawableRes = R.drawable.menu_button_exit,
                    contentDescription = localizedString(R.string.exit),
                    aspectRatio = 3.41f,
                    onClick = onExit,
                )
            }
        }
    }
}

@Composable
private fun MenuImageButton(
    @DrawableRes drawableRes: Int,
    contentDescription: String,
    aspectRatio: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.975f else 1f,
        label = "menu-image-button-scale",
    )
    Image(
        painter = painterResource(drawableRes),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .scale(scale)
            .alpha(if (enabled) 1f else 0.45f)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    )
}
