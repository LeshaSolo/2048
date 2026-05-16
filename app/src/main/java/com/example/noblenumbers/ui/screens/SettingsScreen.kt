package com.example.noblenumbers.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.noblenumbers.R
import com.example.noblenumbers.data.AppSettings
import com.example.noblenumbers.ui.components.NobleButton
import com.example.noblenumbers.ui.components.NobleButtonStyle
import com.example.noblenumbers.ui.components.NobleParchmentPanel
import com.example.noblenumbers.ui.components.NobleSettingsRow
import com.example.noblenumbers.ui.components.NobleTitlePlaque
import com.example.noblenumbers.ui.localizedString
import com.example.noblenumbers.ui.theme.NoblePalette

private const val PRIVACY_POLICY_URL = "https://example.com/privacy" // TODO replace before Google Play release.

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSoundChanged: (Boolean) -> Unit,
    onVibrationChanged: (Boolean) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    WoodScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            NobleTitlePlaque(
                title = localizedString(R.string.settings),
                modifier = Modifier.widthIn(max = 420.dp),
            )
            Spacer(Modifier.height(22.dp))
            NobleParchmentPanel(
                modifier = Modifier
                    .widthIn(max = 430.dp)
                    .fillMaxWidth(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingSwitchRow(
                        label = localizedString(R.string.sound),
                        checked = settings.soundEnabled,
                        onCheckedChange = onSoundChanged,
                    )
                    SettingSwitchRow(
                        label = localizedString(R.string.vibration),
                        checked = settings.vibrationEnabled,
                        onCheckedChange = onVibrationChanged,
                    )
                    Text(
                        text = localizedString(R.string.language),
                        color = NoblePalette.Ink,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    LanguageRow(
                        label = localizedString(R.string.english),
                        selected = settings.languageTag == "en",
                        onClick = { onLanguageChanged("en") },
                    )
                    LanguageRow(
                        label = localizedString(R.string.russian),
                        selected = settings.languageTag == "ru",
                        onClick = { onLanguageChanged("ru") },
                    )
                    NobleSettingsRow(
                        label = localizedString(R.string.privacy_policy),
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
                        },
                    ) {
                        Text(
                            text = ">",
                            color = NoblePalette.Brass,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            NobleButton(
                text = localizedString(R.string.close),
                onClick = onClose,
                modifier = Modifier
                    .widthIn(max = 430.dp)
                    .fillMaxWidth(),
                style = NobleButtonStyle.Secondary,
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    NobleSettingsRow(label = label) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NoblePalette.GoldLight,
                checkedTrackColor = NoblePalette.PrimaryGreen,
                uncheckedThumbColor = NoblePalette.DarkParchment,
                uncheckedTrackColor = NoblePalette.DisabledBrownGray,
            ),
        )
    }
}

@Composable
private fun LanguageRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NobleSettingsRow(label = label, onClick = onClick) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = NoblePalette.PrimaryGreen,
                unselectedColor = NoblePalette.Brass,
            ),
        )
    }
}
