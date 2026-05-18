package com.example.noblenumbers.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noblenumbers.R
import com.example.noblenumbers.ui.components.NobleButton
import com.example.noblenumbers.ui.components.NobleButtonStyle
import com.example.noblenumbers.ui.components.NobleWoodPanel
import com.example.noblenumbers.ui.localizedString
import com.example.noblenumbers.ui.theme.NoblePalette

@Composable
fun RecordsScreen(
    recentScores: List<Int>,
    onClose: () -> Unit,
) {
    WoodScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = localizedString(R.string.records).uppercase(),
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth(),
                color = NoblePalette.GoldLight,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    shadow = Shadow(Color.Black.copy(alpha = 0.8f), blurRadius = 4f),
                ),
            )
            Spacer(Modifier.height(24.dp))
            NobleWoodPanel(
                modifier = Modifier
                    .widthIn(max = 390.dp)
                    .fillMaxWidth(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (recentScores.isEmpty()) {
                        Text(
                            text = localizedString(R.string.records_empty),
                            modifier = Modifier.fillMaxWidth(),
                            color = NoblePalette.Parchment,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    } else {
                        recentScores.forEachIndexed { index, score ->
                            Text(
                                text = "${index + 1}. $score",
                                modifier = Modifier.fillMaxWidth(),
                                color = NoblePalette.GoldLight,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            NobleButton(
                text = localizedString(R.string.close),
                onClick = onClose,
                modifier = Modifier
                    .widthIn(max = 390.dp)
                    .fillMaxWidth(),
                style = NobleButtonStyle.Secondary,
            )
        }
    }
}
