package id.co.mondo.livestudio.ui.screens.status

import android.os.Build
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import id.co.mondo.livestudio.R
import id.co.mondo.livestudio.domain.model.ShareModeType
import id.co.mondo.livestudio.domain.model.StreamStatus
import id.co.mondo.livestudio.ui.screens.home.components.AppPickerBottomSheet

@Composable
fun StreamStatusScreen(
    onStreamFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StreamStatusViewModel = hiltViewModel()
) {
    val status by viewModel.streamStatus.collectAsState()
    val showStopDialog by viewModel.showStopDialog.collectAsState()
    val showAppPicker by viewModel.showAppPicker.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val pickAppRequested by viewModel.pickAppRequested.collectAsState()

    // Permintaan dari tombol "Ganti Aplikasi" di notifikasi.
    LaunchedEffect(pickAppRequested) {
        if (pickAppRequested) {
            viewModel.onPickerRequestConsumed()
            viewModel.onOpenAppPicker()
        }
    }

    // Kembali ke Beranda saat siaran berakhir.
    LaunchedEffect(status) {
        if (status is StreamStatus.Stopped) {
            viewModel.onFinishedAcknowledged()
            onStreamFinished()
        }
    }

    val streamingData = status as? StreamStatus.Streaming
    val pausedData = status as? StreamStatus.ScreenPaused
    val reconnectingData = status as? StreamStatus.Reconnecting
    val errorData = status as? StreamStatus.Error

    val durationSeconds = streamingData?.durationSeconds ?: pausedData?.durationSeconds ?: 0L
    val bitrateKbps = streamingData?.bitrateKbps ?: pausedData?.bitrateKbps ?: 0
    val isMicMuted = streamingData?.isMicMuted ?: pausedData?.isMicMuted ?: false
    val isAppAudioMuted = streamingData?.isAppAudioMuted ?: pausedData?.isAppAudioMuted ?: false
    val shareMode = streamingData?.shareMode ?: pausedData?.shareMode ?: ShareModeType.ENTIRE_SCREEN
    val targetApp = streamingData?.targetApp ?: pausedData?.targetApp
    val canChangeApp = streamingData != null || pausedData != null
    val isAppAudioSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when {
                        streamingData != null -> LiveBadge()
                        pausedData != null -> TextBadge(stringResource(R.string.status_paused), Color(0xFFF9A825))
                        reconnectingData != null -> TextBadge(
                            stringResource(R.string.status_reconnecting, reconnectingData.attempt, reconnectingData.maxAttempts),
                            Color(0xFFF57C00)
                        )
                        errorData != null -> TextBadge(stringResource(R.string.status_error), Color(0xFFC62828))
                        else -> TextBadge(stringResource(R.string.status_connecting), Color(0xFF757575))
                    }

                    Text(
                        text = formatDuration(durationSeconds),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 42.sp,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val sharingLabel = if (shareMode == ShareModeType.SPECIFIC_APP && targetApp != null) {
                        "Aplikasi: ${targetApp.appName}"
                    } else {
                        stringResource(R.string.share_mode_entire_screen)
                    }
                    Text(
                        text = "${stringResource(R.string.label_sharing_target)}: $sharingLabel",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "${stringResource(R.string.label_bitrate)}: $bitrateKbps kbps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    if (errorData != null) {
                        Text(
                            text = errorData.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (errorData != null) {
                Button(
                    onClick = {
                        viewModel.onFinishedAcknowledged()
                        onStreamFinished()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.btn_back_home))
                }
                return@Column
            }

            if (pausedData != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF9A825).copy(alpha = 0.18f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.paused_hint),
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (canChangeApp) {
                if (shareMode == ShareModeType.SPECIFIC_APP && targetApp != null) {
                    Button(
                        onClick = { viewModel.onOpenCurrentTargetApp(targetApp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_open_target_app, targetApp.appName),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                OutlinedButton(
                    onClick = viewModel::onOpenAppPicker,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.btn_change_app))
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AudioRow(
                        label = stringResource(R.string.label_mic),
                        stateText = if (isMicMuted) stringResource(R.string.audio_muted) else stringResource(R.string.audio_active),
                        isMuted = isMicMuted,
                        enabled = true,
                        mutedIcon = Icons.Default.MicOff,
                        activeIcon = Icons.Default.Mic,
                        onCheckedChange = { checked -> viewModel.onToggleMic(!checked) }
                    )
                    AudioRow(
                        label = stringResource(R.string.label_app_audio),
                        stateText = when {
                            !isAppAudioSupported -> stringResource(R.string.audio_unsupported)
                            isAppAudioMuted -> stringResource(R.string.audio_muted)
                            else -> stringResource(R.string.audio_active)
                        },
                        isMuted = isAppAudioMuted,
                        enabled = isAppAudioSupported,
                        mutedIcon = Icons.Default.VolumeOff,
                        activeIcon = Icons.Default.VolumeUp,
                        onCheckedChange = { checked -> viewModel.onToggleAppAudio(!checked) }
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.stream_background_tip),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::onStopClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.btn_confirm_stop),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    if (showAppPicker) {
        AppPickerBottomSheet(
            apps = installedApps,
            selectedApp = targetApp,
            showEntireScreenOption = true,
            isEntireScreenSelected = shareMode == ShareModeType.ENTIRE_SCREEN,
            onSelectApp = viewModel::onSelectTargetApp,
            onSelectEntireScreen = viewModel::onSelectEntireScreen,
            onDismiss = viewModel::onDismissAppPicker
        )
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissStopDialog,
            title = { Text(stringResource(R.string.dialog_stop_title)) },
            text = { Text(stringResource(R.string.dialog_stop_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmStop) {
                    Text(
                        text = stringResource(R.string.btn_confirm_stop),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissStopDialog) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun LiveBadge() {
    val transition = rememberInfiniteTransition(label = "live_pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        modifier = Modifier
            .background(Color(0xFFD32F2F), RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .alpha(alpha)
                .background(Color.White, CircleShape)
        )
        Text(
            text = stringResource(R.string.status_live),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

@Composable
private fun TextBadge(text: String, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

@Composable
private fun AudioRow(
    label: String,
    stateText: String,
    isMuted: Boolean,
    enabled: Boolean,
    mutedIcon: ImageVector,
    activeIcon: ImageVector,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (isMuted || !enabled) mutedIcon else activeIcon,
            contentDescription = null,
            tint = if (isMuted || !enabled) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stateText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Switch(
            checked = enabled && !isMuted,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format("%02d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}
