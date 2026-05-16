package com.example.noblenumbers.ui.screens

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import com.example.noblenumbers.game.model.GameState
import com.example.noblenumbers.ui.components.NobleParchmentPanel
import com.example.noblenumbers.ui.components.NobleTitlePlaque
import com.example.noblenumbers.ui.localizedString
import com.example.noblenumbers.ui.theme.NoblePalette

@Composable
fun GameOverScreen(
    game: GameState,
    rewardedAdAvailable: Boolean,
    activity: Activity?,
    onNewGame: () -> Unit,
    onContinue: (Activity) -> Unit,
) {
    WoodScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            NobleTitlePlaque(
                title = localizedString(R.string.game_over),
                modifier = Modifier.widthIn(max = 420.dp),
            )
            Spacer(Modifier.height(20.dp))
            NobleParchmentPanel(
                modifier = Modifier
                    .widthIn(max = 390.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 17.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Text(
                        text = "${localizedString(R.string.final_score)} ${game.score}",
                        color = NoblePalette.Ink,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            shadow = Shadow(Color.White.copy(alpha = 0.45f), Offset(0f, 1f), 0.8f),
                        ),
                    )
                    Text(
                        text = "${localizedString(R.string.best)} ${game.bestScore}",
                        color = NoblePalette.Brass,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
            GameOverMenuButton(
                text = localizedString(R.string.new_game),
                onClick = onNewGame,
                modifier = Modifier
                    .widthIn(max = 390.dp)
                    .fillMaxWidth(),
                style = GameOverButtonStyle.Green,
            )
            if (game.continueAvailable && rewardedAdAvailable && activity != null) {
                Spacer(Modifier.height(14.dp))
                GameOverMenuButton(
                    text = localizedString(R.string.continue_watch_ad),
                    onClick = { onContinue(activity) },
                    modifier = Modifier
                        .widthIn(max = 390.dp)
                        .fillMaxWidth(),
                    style = GameOverButtonStyle.Gold,
                )
            }
        }
    }
}

private enum class GameOverButtonStyle {
    Green,
    Gold,
}

@Composable
private fun GameOverMenuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GameOverButtonStyle,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        label = "game-over-button-scale",
    )
    val shape = RoundedCornerShape(13.dp)
    val colors = when (style) {
        GameOverButtonStyle.Green -> listOf(
            Color(0xFF6E7F2C),
            Color(0xFF53681F),
            Color(0xFF364616),
        )
        GameOverButtonStyle.Gold -> listOf(
            Color(0xFFD2A446),
            Color(0xFFB78225),
            Color(0xFF8F5C17),
        )
    }
    val textColor = NoblePalette.Ivory

    Box(
        modifier = modifier
            .height(64.dp)
            .scale(scale)
            .shadow(10.dp, shape, clip = false)
            .clip(shape)
            .background(Brush.verticalGradient(colors))
            .drawBehind {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.18f),
                    topLeft = Offset(4.dp.toPx(), 3.dp.toPx()),
                    size = Size(size.width - 8.dp.toPx(), size.height * 0.35f),
                    cornerRadius = CornerRadius(11.dp.toPx()),
                )
                repeat(6) { index ->
                    val y = size.height * (index + 1) / 7f
                    drawLine(
                        color = Color.Black.copy(alpha = 0.08f),
                        start = Offset(10.dp.toPx(), y),
                        end = Offset(size.width - 10.dp.toPx(), y - 3.dp.toPx()),
                        strokeWidth = 1.1f,
                    )
                }
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.18f),
                    topLeft = Offset(3.dp.toPx(), size.height * 0.68f),
                    size = Size(size.width - 6.dp.toPx(), size.height * 0.28f),
                    cornerRadius = CornerRadius(10.dp.toPx()),
                )
            }
            .border(3.dp, NoblePalette.Brass.copy(alpha = 0.96f), shape)
            .border(1.dp, NoblePalette.GoldLight.copy(alpha = 0.72f), shape)
            .clickable(
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(
                when (style) {
                    GameOverButtonStyle.Green -> R.drawable.noble_wood_panel
                    GameOverButtonStyle.Gold -> R.drawable.noble_parchment_panel
                },
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = if (style == GameOverButtonStyle.Green) 0.18f else 0.16f,
        )
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 25.sp,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.72f),
                    offset = Offset(0f, 2.2f),
                    blurRadius = 2.2f,
                ),
            ),
        )
    }
}
