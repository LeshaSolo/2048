package com.example.noblenumbers.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noblenumbers.R
import com.example.noblenumbers.game.logic.GameEngine
import com.example.noblenumbers.game.model.GameState
import com.example.noblenumbers.ui.localizedString
import com.example.noblenumbers.ui.theme.NoblePalette

@Composable
fun NobleWoodBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        NoblePalette.DeepWood,
                        NoblePalette.DarkWalnut,
                        NoblePalette.MediumWalnut,
                        NoblePalette.DeepWood,
                    ),
                ),
            )
            .drawBehind {
                val grain = listOf(
                    NoblePalette.WarmEdge.copy(alpha = 0.08f),
                    Color.Black.copy(alpha = 0.12f),
                    NoblePalette.Gold.copy(alpha = 0.04f),
                )
                repeat(32) { index ->
                    val y = size.height * (index + 1) / 33f
                    val wave = if (index % 2 == 0) 18.dp.toPx() else -14.dp.toPx()
                    val xOffset = if (index % 4 == 0) -16.dp.toPx() else if (index % 4 == 2) 8.dp.toPx() else 0f
                    drawLine(
                        color = grain[index % grain.size],
                        start = Offset(-20.dp.toPx() + xOffset, y),
                        end = Offset(size.width + 20.dp.toPx() + xOffset, y + wave),
                        strokeWidth = if (index % 3 == 0) 2.4f else if (index % 2 == 0) 1.6f else 1.0f,
                    )
                }
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.34f)),
                        center = Offset(size.width * 0.5f, size.height * 0.42f),
                        radius = size.maxDimension * 0.78f,
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                        center = Offset.Zero,
                        radius = size.maxDimension * 0.5f,
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.40f)),
                        center = Offset(size.width, size.height),
                        radius = size.maxDimension * 0.45f,
                    ),
                )
            },
    ) {
        Image(
            painter = painterResource(R.drawable.noble_table_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.98f,
        )
        content()
    }
}

@Composable
fun NobleScreenFrame(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable () -> Unit,
) {
    NobleWoodBackground {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(0.dp)
                .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.safeDrawing)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.Black.copy(alpha = 0.03f))
                .padding(contentPadding),
        ) {
            content()
        }
    }
}

@Composable
fun NobleTitlePlaque(
    title: String,
    modifier: Modifier = Modifier,
) {
    NobleWoodPanel(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(modifier = Modifier.size(width = 132.dp, height = 15.dp)) {
                val centerY = size.height / 2f
                drawLine(
                    color = NoblePalette.Brass.copy(alpha = 0.85f),
                    start = Offset(size.width * 0.08f, centerY),
                    end = Offset(size.width * 0.92f, centerY),
                    strokeWidth = 2f,
                )
                drawCircle(NoblePalette.Gold, radius = 4.dp.toPx(), center = Offset(size.width / 2f, centerY))
                drawCircle(NoblePalette.Brass, radius = 2.5.dp.toPx(), center = Offset(size.width * 0.22f, centerY))
                drawCircle(NoblePalette.Brass, radius = 2.5.dp.toPx(), center = Offset(size.width * 0.78f, centerY))
            }
            Text(
                text = title,
                color = NoblePalette.GoldLight,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.75f),
                        offset = Offset(0f, 3f),
                        blurRadius = 3f,
                    ),
                ),
            )
        }
    }
}

@Composable
fun NobleParchmentPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(cornerRadius), clip = false)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    listOf(
                        NoblePalette.Ivory,
                        NoblePalette.Parchment,
                        NoblePalette.DarkParchment,
                    ),
                ),
            )
            .drawBehind {
                repeat(18) { index ->
                    val y = size.height * (index + 1) / 20f
                    drawLine(
                        color = NoblePalette.Ink.copy(alpha = 0.035f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y + (index % 2) * 2.dp.toPx()),
                        strokeWidth = 1f,
                    )
                }
            }
            .border(2.dp, NoblePalette.Brass.copy(alpha = 0.88f), RoundedCornerShape(cornerRadius))
            .padding(contentPadding),
    ) {
        Image(
            painter = painterResource(R.drawable.noble_parchment_panel),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.22f,
        )
        content()
    }
}

@Composable
fun NobleWoodPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(cornerRadius), clip = false)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    listOf(
                        NoblePalette.WarmEdge,
                        NoblePalette.MediumWalnut,
                        NoblePalette.WoodPanel,
                        NoblePalette.DarkWalnut,
                    ),
                ),
            )
            .drawBehind {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.08f),
                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                    size = Size(size.width - 4.dp.toPx(), size.height * 0.34f),
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                )
                repeat(9) { index ->
                    val y = size.height * (index + 1) / 10f
                    drawLine(
                        color = Color.Black.copy(alpha = 0.09f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y - 6.dp.toPx()),
                        strokeWidth = 1.4f,
                    )
                }
            }
            .border(2.dp, NoblePalette.Brass.copy(alpha = 0.68f), RoundedCornerShape(cornerRadius))
            .padding(contentPadding),
    ) {
        Image(
            painter = painterResource(R.drawable.noble_wood_panel),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.28f,
        )
        content()
    }
}

enum class NobleButtonStyle {
    Primary,
    Secondary,
    Danger,
    Gold,
}

@Composable
fun NobleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: NobleButtonStyle = NobleButtonStyle.Primary,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        label = "noble-button-scale",
    )
    val colors = buttonColors(style)
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = 50.dp)
            .shadow(6.dp, RoundedCornerShape(10.dp), clip = false),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(2.dp, NoblePalette.Brass.copy(alpha = if (enabled) 0.9f else 0.3f)),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.container,
            contentColor = colors.content,
            disabledContainerColor = NoblePalette.DisabledBrownGray,
            disabledContentColor = NoblePalette.DarkParchment,
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 11.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 19.sp,
                shadow = Shadow(Color.Black.copy(alpha = 0.36f), Offset(0f, 1.5f), 1f),
            ),
        )
    }
}

@Composable
fun NobleIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) 0.94f else 1f, label = "noble-icon-button-scale")
    Box(
        modifier = modifier
            .size(50.dp)
            .scale(scale)
            .shadow(6.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(NoblePalette.WarmEdge, NoblePalette.MediumWalnut, NoblePalette.WoodPanel),
                ),
            )
            .border(2.dp, NoblePalette.Brass.copy(alpha = 0.82f), RoundedCornerShape(12.dp))
            .semantics { this.contentDescription = contentDescription }
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun NobleHudPanel(
    game: GameState,
    modifier: Modifier = Modifier,
) {
    NobleWoodPanel(
        modifier = modifier,
        cornerRadius = 14.dp,
        contentPadding = PaddingValues(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NobleScorePlaque(
                    label = localizedString(R.string.score),
                    value = game.score,
                    modifier = Modifier.weight(1f),
                )
                NobleScorePlaque(
                    label = localizedString(R.string.best),
                    value = game.bestScore,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (game.comboCount > 1) {
                    NobleParchmentPanel(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 10.dp,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "x${game.comboCount}",
                            color = NoblePalette.Gold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }
                if (game.nextTilePreview != null) {
                    NobleParchmentPanel(
                        modifier = if (game.comboCount > 1) Modifier.weight(1f) else Modifier.weight(1f),
                        cornerRadius = 10.dp,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "${game.nextTilePreview}",
                            color = NoblePalette.Ink,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }
            }
            if (game.frozenModeEnabled) {
                NobleParchmentPanel(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 10.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = localizedString(R.string.frozen_mode),
                        color = NoblePalette.DeepIceShadow,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            } else {
                val progress = (game.maxTile.toFloat() / GameEngine.TARGET_TILE).coerceIn(0f, 1f)
                NobleProgressBar(
                    label = localizedString(R.string.target_progress),
                    progressText = "${game.maxTile.coerceAtLeast(0)} / ${GameEngine.TARGET_TILE}",
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (game.extraMovesRemaining > 0) {
                Text(
                    text = localizedString(R.string.moves_remaining, game.extraMovesRemaining),
                    color = NoblePalette.GoldLight,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
fun NobleProgressBar(
    label: String,
    progressText: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, color = NoblePalette.Parchment, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = progressText,
                color = NoblePalette.GoldLight,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(15.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NoblePalette.DeepWood)
                .border(1.dp, NoblePalette.Brass.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                .padding(3.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(9.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(NoblePalette.PrimaryGreenDark, NoblePalette.PrimaryGreen, NoblePalette.Gold),
                        ),
                    ),
            )
        }
    }
}

@Composable
fun NobleSettingsRow(
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    val rowModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(NoblePalette.Parchment.copy(alpha = 0.9f))
        .border(1.dp, NoblePalette.Brass.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
        .then(
            if (onClick != null) {
                Modifier.clickable(role = Role.Button, onClick = onClick)
            } else {
                Modifier
            },
        )
        .padding(horizontal = 14.dp, vertical = 10.dp)

    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = NoblePalette.Ink,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        )
        trailing()
    }
}

@Composable
fun NobleConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NoblePalette.Parchment,
        titleContentColor = NoblePalette.Ink,
        textContentColor = NoblePalette.Ink,
        title = {
            Text(
                text = localizedString(R.string.new_game),
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif),
            )
        },
        text = { Text(localizedString(R.string.new_game_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(localizedString(R.string.start), color = NoblePalette.PrimaryGreenDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizedString(R.string.cancel), color = NoblePalette.Ink)
            }
        },
    )
}

@Composable
private fun NobleScorePlaque(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    NobleParchmentPanel(
        modifier = modifier,
        cornerRadius = 10.dp,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = NoblePalette.Ink,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = value.toString(),
                color = NoblePalette.Brass,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

private data class NobleButtonColors(
    val container: Color,
    val content: Color,
)

private fun buttonColors(style: NobleButtonStyle): NobleButtonColors = when (style) {
    NobleButtonStyle.Primary -> NobleButtonColors(NoblePalette.PrimaryGreen, NoblePalette.Ivory)
    NobleButtonStyle.Secondary -> NobleButtonColors(NoblePalette.DarkParchment, NoblePalette.Ink)
    NobleButtonStyle.Danger -> NobleButtonColors(NoblePalette.DangerRedBrown, NoblePalette.Ivory)
    NobleButtonStyle.Gold -> NobleButtonColors(NoblePalette.Brass, NoblePalette.Ivory)
}
