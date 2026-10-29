package id.co.mondo.livestudio.ui.screens.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.co.mondo.livestudio.R
import id.co.mondo.livestudio.core.util.AppHelper
import id.co.mondo.livestudio.domain.model.BroadcastConfig
import id.co.mondo.livestudio.domain.model.PrivacyOption
import id.co.mondo.livestudio.domain.model.ShareModeType
import id.co.mondo.livestudio.domain.model.TargetApp
import id.co.mondo.livestudio.domain.model.VideoQualityProfile
import id.co.mondo.livestudio.ui.screens.home.components.AccountRequirementBottomSheet
import id.co.mondo.livestudio.ui.screens.home.components.AppPickerBottomSheet
import id.co.mondo.livestudio.ui.screens.home.components.toImageBitmap

@OptIn(ExperimentalLayoutApi::class)
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

    val mediaProjectionManager = remember {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val pending = state.pendingBroadcast
        if (result.resultCode == Activity.RESULT_OK && result.data != null && pending != null) {
            viewModel.onScreenCapturePermissionGranted(result.resultCode, result.data!!, pending)
            onStartBroadcast(pending)
        } else {
            viewModel.onScreenCaptureCancelled()
        }
    }

    // Begitu siaran Facebook berhasil dibuat, langsung luncurkan dialog izin rekam layar sistem.
    LaunchedEffect(state.pendingBroadcast) {
        if (state.pendingBroadcast != null) {
            val captureIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val config = android.media.projection.MediaProjectionConfig.createConfigForDefaultDisplay()
                mediaProjectionManager.createScreenCaptureIntent(config)
            } else {
                mediaProjectionManager.createScreenCaptureIntent()
            }
            mediaProjectionLauncher.launch(captureIntent)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onDismissError()
        }
    }

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

            // Pilihan Mode Berbagi Layar: Seluruh Layar vs Aplikasi
            ShareModeSection(
                shareMode = state.shareMode,
                selectedApp = state.selectedApp,
                hasError = state.targetAppError,
                onSelectMode = viewModel::onShareModeSelected,
                onOpenAppPicker = viewModel::onOpenAppPicker
            )

            // Pilihan Privasi
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.label_privacy),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrivacyOption.entries.forEach { option ->
                        val labelRes = when (option) {
                            PrivacyOption.PUBLIC -> R.string.privacy_public
                            PrivacyOption.FRIENDS -> R.string.privacy_friends
                        }
                        FilterChip(
                            selected = state.privacy == option,
                            onClick = { viewModel.onPrivacySelected(option) },
                            label = { Text(stringResource(labelRes)) }
                        )
                    }
                }
            }

            // Pilihan Kualitas Video
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.label_quality),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VideoQualityProfile.entries.forEach { profile ->
                        FilterChip(
                            selected = state.quality == profile,
                            onClick = { viewModel.onQualitySelected(profile) },
                            label = { Text(profile.label) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.onStartLiveClicked()
                },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.creating_broadcast),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
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

    if (state.showAppPicker) {
        AppPickerBottomSheet(
            apps = state.installedApps,
            selectedApp = state.selectedApp,
            showEntireScreenOption = false,
            onSelectApp = viewModel::onTargetAppSelected,
            onDismiss = viewModel::onDismissAppPicker
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
private fun ShareModeSection(
    shareMode: ShareModeType,
    selectedApp: TargetApp?,
    hasError: Boolean,
    onSelectMode: (ShareModeType) -> Unit,
    onOpenAppPicker: () -> Unit
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.label_share_mode),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShareModeCard(
                modifier = Modifier.weight(1f),
                selected = shareMode == ShareModeType.ENTIRE_SCREEN,
                icon = Icons.Default.PhoneAndroid,
                title = stringResource(R.string.share_mode_entire_screen),
                onClick = { onSelectMode(ShareModeType.ENTIRE_SCREEN) }
            )
            ShareModeCard(
                modifier = Modifier.weight(1f),
                selected = shareMode == ShareModeType.SPECIFIC_APP,
                icon = Icons.Default.Apps,
                title = stringResource(R.string.share_mode_specific_app),
                onClick = { onSelectMode(ShareModeType.SPECIFIC_APP) }
            )
        }

        if (shareMode == ShareModeType.ENTIRE_SCREEN) {
            Text(
                text = stringResource(R.string.share_mode_entire_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        } else {
            Text(
                text = stringResource(R.string.share_mode_app_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            if (selectedApp != null) {
                val iconBitmap = remember(selectedApp.packageName) {
                    AppHelper.getAppIcon(context, selectedApp.packageName)?.toImageBitmap()
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenAppPicker() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (iconBitmap != null) {
                            Image(
                                bitmap = iconBitmap,
                                contentDescription = selectedApp.appName,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedApp.appName.firstOrNull()?.uppercase() ?: "?",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedApp.appName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.app_picker_last_used),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        OutlinedButton(onClick = onOpenAppPicker) {
                            Text(stringResource(R.string.btn_change_target_app))
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onOpenAppPicker,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.btn_select_target_app))
                }
            }

            if (hasError) {
                Text(
                    text = stringResource(R.string.error_target_app_required),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ShareModeCard(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
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

private const val FACEBOOK_PACKAGE = "com.facebook.katana"
