package com.example.subscriptions.ui.composable.subscriptions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.subscriptions.R
import com.example.subscriptions.ui.BillingViewModel
import com.example.subscriptions.ui.composable.ClassyTaxiScreenHeader
import com.example.subscriptions.ui.composable.resetSelectedButton

@Composable
fun SubscriptionSettingsScreen(
    billingViewModel: BillingViewModel,
    modifier: Modifier = Modifier,
) {
    val selectedSettingsButton = remember { mutableStateOf(false) }

    Surface(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ClassyTaxiScreenHeader(
                content = {
                    SettingsButtons(
                        selectedSettingsButton = selectedSettingsButton
                    )
                },
                textResource = R.string.google_play_billing_settings_message
            )
        }
    }

    LaunchedEffect(key1 = selectedSettingsButton.value) {
        if (selectedSettingsButton.value) {
            billingViewModel.openPlayStoreSubscriptions()
            resetSelectedButton(
                selectedIntButton = null,
                selectedBooleanButton = selectedSettingsButton
            )
        }
    }

}

@Composable
private fun SettingsButtons(
    selectedSettingsButton: MutableState<Boolean>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_height)))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { selectedSettingsButton.value = true }
        ) {
            Text(
                text = stringResource(R.string.tab_text_settings),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_height)))
    }
}