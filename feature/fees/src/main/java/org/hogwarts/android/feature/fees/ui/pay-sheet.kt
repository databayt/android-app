package org.hogwarts.android.feature.fees.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.hogwarts.android.core.designsystem.kit.FormAlert
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme
import org.hogwarts.android.feature.fees.R
import org.hogwarts.android.feature.fees.domain.Gateway

/**
 * `PayFeeDialog` + `GatewayPicker` as a phone sheet: the title, the fee, the
 * prompt, then one card per payable rail. A redirect rail starts the hosted
 * checkout; a wallet rail goes to the web page that takes its transfer proof.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PayFeeSheet(sheet: PaySheet, onChoose: (Gateway) -> Unit, onDismiss: () -> Unit) {
    val colors = HogwartsTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.background,
        contentColor = colors.foreground,
    ) {
        PaySheetContent(sheet, onChoose)
    }
}

@Composable
internal fun PaySheetContent(sheet: PaySheet, onChoose: (Gateway) -> Unit) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.gateways_title), style = type.section, color = colors.foreground)
            Text(sheet.label, style = type.body, color = colors.mutedForeground)
        }
        Text(stringResource(R.string.gateways_choose_method), style = type.body, color = colors.mutedForeground, modifier = Modifier.padding(top = 4.dp))
        if (sheet.failed) {
            FormAlert(message = stringResource(R.string.gateways_payment_failed))
        }
        sheet.gateways.forEach { gateway ->
            val busy = sheet.loading == gateway
            GatewayRow(
                gateway = gateway,
                onClick = if (sheet.loading == null) ({ onChoose(gateway) }) else null,
                trailing = if (busy) ({ CircularProgressIndicator(Modifier.size(20.dp), color = colors.mutedForeground, strokeWidth = 2.dp) }) else null,
            )
        }
        if (sheet.loading != null) {
            Text(
                stringResource(R.string.gateways_redirecting),
                style = type.caption,
                color = colors.mutedForeground,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}
