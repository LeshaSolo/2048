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
import com.example.noblenumbers.app.AppUpdateUiState
import com.example.noblenumbers.app.UpdateStatus
import com.example.noblenumbers.data.AppSettings
import com.example.noblenumbers.updates.UpdateError
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
    updateState: AppUpdateUiState,
    onSoundChanged: (Boolean) -> Unit,
    onVibrationChanged: (Boolean) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onCheckUpdates: () -> Unit,
    onDownloadAndInstallUpdate: () -> Unit,
    onInstallDownloadedUpdate: () -> Unit,
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
                    Text(
                        text = localizedString(R.string.updates),
                        color = NoblePalette.Ink,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    UpdateStatusText(updateState)
                    UpdateActionButton(
                        updateState = updateState,
                        onCheckUpdates = onCheckUpdates,
                        onDownloadAndInstallUpdate = onDownloadAndInstallUpdate,
                        onInstallDownloadedUpdate = onInstallDownloadedUpdate,
                    )
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
private fun UpdateStatusText(
    updateState: AppUpdateUiState,
) {
    val message = when (updateState.status) {
        UpdateStatus.Idle -> localizedString(R.string.update_idle)
        UpdateStatus.Checking -> localizedString(R.string.update_checking)
        UpdateStatus.NoUpdate -> localizedString(R.string.update_no_updates)
        UpdateStatus.Available -> localizedString(
            R.string.update_available,
            updateState.availableUpdate?.label.orEmpty(),
        )
        UpdateStatus.Downloading -> localizedString(R.string.update_downloading)
        UpdateStatus.PermissionRequired -> localizedString(R.string.update_permission_required)
        UpdateStatus.InstallerStarted -> localizedString(R.string.update_installer_started)
        UpdateStatus.Error -> localizedString(updateState.error.updateErrorMessageRes())
    }
    Text(
        text = message,
        color = NoblePalette.Ink,
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun UpdateActionButton(
    updateState: AppUpdateUiState,
    onCheckUpdates: () -> Unit,
    onDownloadAndInstallUpdate: () -> Unit,
    onInstallDownloadedUpdate: () -> Unit,
) {
    val busy = updateState.status == UpdateStatus.Checking ||
        updateState.status == UpdateStatus.Downloading
    val actionText = when (updateState.status) {
        UpdateStatus.Available -> localizedString(R.string.download_and_install_update)
        UpdateStatus.PermissionRequired -> localizedString(R.string.install_downloaded_update)
        else -> localizedString(R.string.check_updates)
    }
    val action = when (updateState.status) {
        UpdateStatus.Available -> onDownloadAndInstallUpdate
        UpdateStatus.PermissionRequired -> onInstallDownloadedUpdate
        else -> onCheckUpdates
    }
    NobleButton(
        text = actionText,
        onClick = action,
        modifier = Modifier.fillMaxWidth(),
        enabled = !busy,
        style = NobleButtonStyle.Secondary,
    )
}

private fun UpdateError?.updateErrorMessageRes(): Int = when (this) {
    UpdateError.ReleaseVersionMissing -> R.string.update_error_version_missing
    UpdateError.ApkAssetMissing -> R.string.update_error_apk_missing
    UpdateError.DownloadFailed -> R.string.update_error_download
    UpdateError.InstallPermissionRequired -> R.string.update_permission_required
    UpdateError.InstallerLaunchFailed -> R.string.update_error_installer
    UpdateError.Network,
    null,
    -> R.string.update_error_network
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
