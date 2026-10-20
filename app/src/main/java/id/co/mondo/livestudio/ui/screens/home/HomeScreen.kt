package id.co.mondo.livestudio.ui.screens.home

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.co.mondo.livestudio.R
import id.co.mondo.livestudio.domain.model.BroadcastConfig
import id.co.mondo.livestudio.domain.model.PrivacyOption
import id.co.mondo.livestudio.ui.screens.home.components.AccountRequirementBottomSheet

@Composable
fun HomeScreen(
    onStartBroadcast: (BroadcastConfig) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onDismissError()
        }
    }

    // Judul ditampung sebagai TextFieldValue agar seluruh teks bisa diblok saat fokus.
    var titleField by remember { mutableStateOf(TextFieldValue(state.title)) }
    LaunchedEffect(state.title) {
        if (titleField.text != state.title) titleField = TextFieldValue(state.title)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ProfileHeader(
                userName = state.userName,
                onLogout = viewModel::onLogoutClicked
            )

            Text(
                text = stringResource(R.string.prepare_live_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedTextField(
                value = titleField,
                onValueChange = {
                    titleField = it
                    viewModel.onTitleChanged(it.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focus ->
                        if (focus.isFocused) {
                            titleField = titleField.copy(selection = TextRange(0, titleField.text.length))
                        }
                    },
                label = { Text(stringResource(R.string.label_broadcast_title)) },
                placeholder = { Text(stringResource(R.string.hint_broadcast_title)) },
                isError = state.titleError,
                supportingText = if (state.titleError) {
                    { Text(stringResource(R.string.error_title_required)) }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.label_privacy),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrivacyOption.entries.forEach { option ->
                        FilterChip(
                            selected = state.privacy == option,
                            onClick = { viewModel.onPrivacySelected(option) },
                            label = {
                                Text(
                                    text = when (option) {
                                        PrivacyOption.PUBLIC -> stringResource(R.string.privacy_public)
                                        PrivacyOption.FRIENDS -> stringResource(R.string.privacy_friends)
                                    }
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.onStartLiveClicked()
                },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.btn_start_live),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }

    state.createdBroadcast?.let { config ->
        AlertDialog(
            onDismissRequest = viewModel::onBroadcastDialogDismissed,
            title = { Text(stringResource(R.string.broadcast_ready_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.broadcast_ready_message))
                    Text(
                        text = "${stringResource(R.string.broadcast_id_label)}: ${config.liveVideoId}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${stringResource(R.string.broadcast_server_label)}: ${maskStreamUrl(config.rtmpsUrl)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onBroadcastDialogDismissed()
                    onStartBroadcast(config)
                }) { Text(stringResource(R.string.btn_continue)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onBroadcastDialogDismissed) {
                    Text(stringResource(R.string.btn_close))
                }
            }
        )
    }

    if (state.showRequirementSheet) {
        AccountRequirementBottomSheet(
            onDismiss = viewModel::onRequirementSheetDismissed,
            onRetry = {
                viewModel.onRequirementSheetDismissed()
                viewModel.onStartLiveClicked()
            },
            onOpenFacebook = {
                val intent = context.packageManager.getLaunchIntentForPackage(FACEBOOK_PACKAGE)
                    ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/"))
                context.startActivity(intent)
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    userName: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.trim().firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_greeting, userName.ifBlank { "Admin" }),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        IconButton(onClick = onLogout) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = stringResource(R.string.btn_logout)
            )
        }
    }
}

/** Sembunyikan stream key (bagian setelah `/rtmp/`) agar tidak tampil di layar. */
private fun maskStreamUrl(url: String): String {
    val marker = "/rtmp/"
    val index = url.indexOf(marker)
    return if (index >= 0) url.substring(0, index + marker.length) + "\u2022\u2022\u2022\u2022" else url
}

private const val FACEBOOK_PACKAGE = "com.facebook.katana"