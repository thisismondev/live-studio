package id.co.mondo.livestudio.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.co.mondo.livestudio.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountRequirementBottomSheet(
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onOpenFacebook: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.requirement_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = stringResource(R.string.requirement_intro),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(text = stringResource(R.string.requirement_step_1), style = MaterialTheme.typography.bodyMedium)
            Text(text = stringResource(R.string.requirement_step_2), style = MaterialTheme.typography.bodyMedium)
            Text(text = stringResource(R.string.requirement_step_3), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(R.string.requirement_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onOpenFacebook, modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.btn_open_facebook))
                }
                Button(onClick = onRetry, modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.btn_retry))
                }
            }
        }
    }
}