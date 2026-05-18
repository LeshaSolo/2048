package com.example.noblenumbers.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.noblenumbers.R
import androidx.compose.animation.core.tween
import com.example.noblenumbers.game.model.Board
import com.example.noblenumbers.game.model.MoveDirection
import com.example.noblenumbers.game.model.ScorePopup
import com.example.noblenumbers.ui.localizedString
import com.example.noblenumbers.ui.model.BoardTileUi
import com.example.noblenumbers.ui.model.toBoardTileUi
import com.example.noblenumbers.ui.theme.NoblePalette

@Composable
fun GameBoard(
    board: Board,
    onSwipe: (MoveDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    NobleGameBoard(
        board = board,
        displayTiles = null,
        onSwipe = onSwipe,
        modifier = modifier,
    )
}

@Composable
fun NobleGameBoard(
    board: Board,
    displayTiles: List<BoardTileUi>?,
    onSwipe: (MoveDirection) -> Unit,
    modifier: Modifier = Modifier,
    scorePopups: List<ScorePopup> = emptyList(),
) {
    val boardDescription = localizedString(R.string.board_content_description)
    val tiles = displayTiles ?: board.tiles.map { it.toBoardTileUi() }

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = boardDescription }
            .drawBehind {
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.42f),
                    topLeft = Offset(0f, 8.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(18.dp.toPx()),
                )
            }
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        NoblePalette.WarmEdge,
                        NoblePalette.MediumWalnut,
                        NoblePalette.WoodPanel,
                        NoblePalette.DeepWood,
                    ),
                ),
            )
            .border(3.dp, NoblePalette.Brass.copy(alpha = 0.78f), RoundedCornerShape(18.dp))
            .padding(13.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.noble_board_texture),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.42f,
        )
        val gap = 7.dp
        val cellSize = (maxWidth - gap * (board.size - 1)) / board.size

        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(12) { index ->
                val y = size.height * (index + 1) / 13f
                drawLine(
                    color = Color.Black.copy(alpha = 0.11f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y - 10.dp.toPx()),
                    strokeWidth = 1.4f,
                )
            }
            drawRoundRect(
                color = Color.White.copy(alpha = 0.06f),
                topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                size = Size(size.width - 6.dp.toPx(), size.height * 0.22f),
                cornerRadius = CornerRadius(14.dp.toPx()),
            )
        }

        for (row in 0 until board.size) {
            for (column in 0 until board.size) {
                NobleBoardCell(
                    modifier = Modifier
                        .offset(x = (cellSize + gap) * column, y = (cellSize + gap) * row)
                        .size(cellSize),
                )
            }
        }

        tiles.forEach { tile ->
            key(tile.id) {
                val x by animateDpAsState(
                    targetValue = (cellSize + gap) * tile.column,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "noble-tile-x-${tile.id}",
                )
                val y by animateDpAsState(
                    targetValue = (cellSize + gap) * tile.row,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "noble-tile-y-${tile.id}",
                )
                NobleTile(
                    tile = tile,
                    tileSize = cellSize,
                    modifier = Modifier
                        .offset(x = x, y = y)
                        .size(cellSize)
                        .zIndex(tileRenderLayer(tile)),
                )
            }
        }

        scorePopups.forEach { popup ->
            key("popup-${popup.row}-${popup.column}-${popup.value}") {
                ScorePopupTile(
                    value = popup.value,
                    cellSize = cellSize,
                    gap = gap,
                    row = popup.row,
                    column = popup.column,
                )
            }
        }
    }
}

@Composable
private fun ScorePopupTile(
    value: Int,
    cellSize: Dp,
    gap: Dp,
    row: Int,
    column: Int,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    val offsetY by animateFloatAsState(
        targetValue = if (visible) -20f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "score-popup-y",
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 0f else 1f,
        animationSpec = tween(durationMillis = 400),
        label = "score-popup-alpha",
    )
    val fontSize = when {
        value < 100 -> 16
        value < 1000 -> 14
        else -> 12
    }
    Text(
        text = "+$value",
        modifier = Modifier
            .offset(x = (cellSize + gap) * column, y = (cellSize + gap) * row + offsetY.dp)
            .size(cellSize)
            .zIndex(5000f),
        color = NoblePalette.GoldLight.copy(alpha = alpha),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.6f),
                offset = Offset(0f, 1f),
                blurRadius = 2f,
            ),
        ),
    )
}

private fun tileRenderLayer(tile: BoardTileUi): Float = when {
    tile.isMerging -> 3_000f + tile.id.toFloat()
    tile.isMoving -> 2_000f + tile.id.toFloat()
    tile.isSpawning -> 1_000f + tile.id.toFloat()
    else -> tile.id.toFloat()
}

@Composable
private fun NobleBoardCell(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.52f),
                        NoblePalette.DeepWood.copy(alpha = 0.78f),
                        NoblePalette.DarkWalnut.copy(alpha = 0.92f),
                    ),
                ),
            )
            .border(1.dp, Color.Black.copy(alpha = 0.48f), RoundedCornerShape(10.dp))
            .drawBehind {
                drawRoundRect(
                    color = NoblePalette.Brass.copy(alpha = 0.13f),
                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                    size = Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx()),
                )
            },
    )
}

@Composable
fun NobleTile(
    tile: BoardTileUi,
    tileSize: Dp,
    modifier: Modifier = Modifier,
) {
    var visible by remember(tile.id) { mutableStateOf(false) }
    LaunchedEffect(tile.id, tile.isSpawning) {
        if (tile.isSpawning) {
            kotlinx.coroutines.delay(NEW_TILE_REVEAL_DELAY_MILLIS)
        }
        visible = true
    }
    val scale by animateFloatAsState(
        targetValue = when {
            !visible -> 0.72f
            tile.isMerging -> 1.12f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "noble-tile-scale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "noble-tile-alpha",
    )
    val description = if (tile.isFrozen) {
        localizedString(R.string.frozen_tile_description, tile.value, tile.frozenMovesRemaining)
    } else {
        localizedString(R.string.tile_description, tile.value)
    }
    val radius = 10.dp

    val glow = tileGlow(tile.value)

    Box(
        modifier = modifier
            .scale(scale)
            .semantics { contentDescription = description }
            .drawBehind {
                if (glow != null) {
                    drawRoundRect(
                        color = glow.copy(alpha = 0.35f * alpha),
                        topLeft = Offset(-3.dp.toPx(), -3.dp.toPx()),
                        size = Size(size.width + 6.dp.toPx(), size.height + 6.dp.toPx()),
                        cornerRadius = CornerRadius((radius + 2.dp).toPx()),
                    )
                    drawRoundRect(
                        color = glow.copy(alpha = 0.18f * alpha),
                        topLeft = Offset(-6.dp.toPx(), -6.dp.toPx()),
                        size = Size(size.width + 12.dp.toPx(), size.height + 12.dp.toPx()),
                        cornerRadius = CornerRadius((radius + 4.dp).toPx()),
                    )
                }
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.38f * alpha),
                    topLeft = Offset(3.dp.toPx(), 5.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(radius.toPx()),
                )
            }
            .clip(RoundedCornerShape(radius))
            .background(tileBrush(tile.value, alpha))
            .drawBehind {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.23f * alpha),
                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                    size = Size(size.width - 4.dp.toPx(), size.height * 0.34f),
                    cornerRadius = CornerRadius(radius.toPx()),
                )
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.16f * alpha),
                    topLeft = Offset(2.dp.toPx(), size.height * 0.66f),
                    size = Size(size.width - 4.dp.toPx(), size.height * 0.32f),
                    cornerRadius = CornerRadius(radius.toPx()),
                )
                if (tile.isMerging && glow != null) {
                    drawRoundRect(
                        color = glow.copy(alpha = 0.28f),
                        topLeft = Offset(0f, 0f),
                        size = size,
                        cornerRadius = CornerRadius(radius.toPx()),
                    )
                }
            }
            .border(2.dp, tileBorderColor(tile.value).copy(alpha = alpha), RoundedCornerShape(radius)),
        contentAlignment = Alignment.Center,
    ) {
        if (tile.isFrozen) {
            NobleFrozenTileOverlay(remainingMoves = tile.frozenMovesRemaining)
        }
        EngravedNumber(
            value = tile.value,
            tileSize = tileSize,
            color = numberColor(tile.value, tile.isFrozen),
        )
        if (tile.isFrozen) {
            Text(
                text = tile.frozenMovesRemaining.toString(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp),
                color = NoblePalette.FrozenTextShadow,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    shadow = Shadow(NoblePalette.FrostWhite.copy(alpha = 0.9f), Offset(0f, 1f), 1f),
                ),
            )
        }
    }
}

@Composable
fun NobleFrozenTileOverlay(
    remainingMoves: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        NoblePalette.FrostWhite.copy(alpha = 0.54f),
                        NoblePalette.IceBlue.copy(alpha = 0.38f),
                        NoblePalette.DeepIceShadow.copy(alpha = 0.22f),
                    ),
                ),
            )
            .border(2.dp, NoblePalette.FrostWhite.copy(alpha = 0.72f), RoundedCornerShape(10.dp)),
    ) {
        Image(
            painter = painterResource(R.drawable.noble_ice_overlay),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.72f,
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val frost = NoblePalette.FrostWhite.copy(alpha = 0.72f)
            val shadow = NoblePalette.DeepIceShadow.copy(alpha = 0.38f)
            val bright = NoblePalette.FrostWhite.copy(alpha = 0.9f)
            val w = size.width
            val h = size.height
            drawLine(frost, Offset(w * 0.18f, h * 0.2f), Offset(w * 0.5f, h * 0.48f), 2.1f)
            drawLine(frost, Offset(w * 0.5f, h * 0.48f), Offset(w * 0.82f, h * 0.36f), 2.1f)
            drawLine(frost, Offset(w * 0.48f, h * 0.5f), Offset(w * 0.62f, h * 0.8f), 1.7f)
            drawLine(shadow, Offset(w * 0.14f, h * 0.84f), Offset(w * 0.88f, h * 0.12f), 1.2f)
            drawLine(bright, Offset(w * 0.32f, h * 0.14f), Offset(w * 0.44f, h * 0.26f), 1.4f)
            drawLine(bright.copy(alpha = 0.6f), Offset(w * 0.72f, h * 0.52f), Offset(w * 0.88f, h * 0.68f), 1.3f)
            drawLine(frost.copy(alpha = 0.5f), Offset(w * 0.12f, h * 0.54f), Offset(w * 0.28f, h * 0.72f), 1.1f)
            drawLine(frost.copy(alpha = 0.55f), Offset(w * 0.68f, h * 0.14f), Offset(w * 0.82f, h * 0.28f), 1.0f)
            drawCircle(bright, radius = 3.dp.toPx(), center = Offset(w * 0.18f, h * 0.18f))
            drawCircle(frost.copy(alpha = 0.56f), radius = 2.dp.toPx(), center = Offset(w * 0.82f, h * 0.78f))
            drawCircle(bright.copy(alpha = 0.5f), radius = 1.8.dp.toPx(), center = Offset(w * 0.66f, h * 0.22f))
            drawCircle(frost.copy(alpha = 0.4f), radius = 1.5.dp.toPx(), center = Offset(w * 0.24f, h * 0.68f))
        }
        @Suppress("UNUSED_VARIABLE")
        val countdownKeptForSemantics = remainingMoves
    }
}

@Composable
private fun EngravedNumber(
    value: Int,
    tileSize: Dp,
    color: Color,
) {
    val fontSize = numberSize(value, tileSize)
    Text(
        text = value.toString(),
        modifier = Modifier.offset(y = (-1).dp),
        color = Color.White.copy(alpha = 0.28f),
        style = numberTextStyle(value, fontSize),
    )
    Text(
        text = value.toString(),
        modifier = Modifier.offset(y = 1.dp),
        color = Color.Black.copy(alpha = 0.34f),
        style = numberTextStyle(value, fontSize),
    )
    Text(
        text = value.toString(),
        color = color,
        style = numberTextStyle(value, fontSize).copy(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.34f),
                offset = Offset(0f, 1.5f),
                blurRadius = 0.8f,
            ),
        ),
    )
}

private fun tileBrush(value: Int, alpha: Float): Brush {
    val base = tileColor(value)
    return Brush.verticalGradient(
        listOf(
            lighten(base, 0.22f).copy(alpha = alpha),
            base.copy(alpha = alpha),
            darken(base, 0.18f).copy(alpha = alpha),
        ),
    )
}

private fun tileColor(value: Int): Color = when (value) {
    2 -> Color(0xFFF4E8C8)
    4 -> NoblePalette.Parchment
    8 -> Color(0xFFD59A3B)
    16 -> Color(0xFFC66E32)
    32 -> Color(0xFF9E5032)
    64 -> NoblePalette.Burgundy
    128 -> NoblePalette.Olive
    256 -> Color(0xFF89923B)
    512 -> NoblePalette.MutedBlue
    1024 -> Color(0xFF344C72)
    2048 -> Color(0xFF6C4A83)
    4096 -> NoblePalette.Gold
    else -> Color(0xFF4C315C)
}

private fun tileBorderColor(value: Int): Color = when {
    value >= 4096 -> NoblePalette.GoldLight
    value >= 512 -> NoblePalette.DarkParchment
    else -> NoblePalette.Brass
}

private fun tileGlow(value: Int): Color? = when {
    value >= 4096 -> NoblePalette.GoldLight
    value >= 1024 -> NoblePalette.DarkParchment.copy(alpha = 0.7f)
    else -> null
}

private fun numberColor(value: Int, frozen: Boolean): Color = when {
    frozen -> NoblePalette.FrozenTextShadow
    value <= 256 || value == 4096 -> NoblePalette.Ink
    else -> NoblePalette.Ivory
}

private fun numberTextStyle(value: Int, fontSize: Int): TextStyle = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Black,
    fontSize = fontSize.sp,
    letterSpacing = 0.sp,
)

private fun numberSize(value: Int, tileSize: Dp): Int {
    val base = when {
        value < 100 -> 29
        value < 1000 -> 25
        value < 10000 -> 21
        else -> 17
    }
    return if (tileSize < 52.dp) base - 3 else base
}

private fun lighten(color: Color, amount: Float): Color = Color(
    red = color.red + (1f - color.red) * amount,
    green = color.green + (1f - color.green) * amount,
    blue = color.blue + (1f - color.blue) * amount,
    alpha = color.alpha,
)

private fun darken(color: Color, amount: Float): Color = Color(
    red = color.red * (1f - amount),
    green = color.green * (1f - amount),
    blue = color.blue * (1f - amount),
    alpha = color.alpha,
)

private const val NEW_TILE_REVEAL_DELAY_MILLIS = 20L
