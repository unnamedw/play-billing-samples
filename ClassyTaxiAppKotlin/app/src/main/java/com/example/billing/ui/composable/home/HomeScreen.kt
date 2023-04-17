package com.example.billing.ui.composable.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.billing.R
import com.example.billing.gpbl.BillingClientLifecycle
import com.example.billing.ui.BillingViewModel
import com.example.billing.ui.FirebaseUserViewModel
import com.example.billing.ui.SubscriptionStatusViewModel
import com.example.billing.ui.composable.subscriptions.SubscriptionScreen
import com.example.billing.ui.theme.ClassyTaxiAppKotlinTheme
import com.firebase.ui.auth.AuthUI
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassyTaxiApp(
    billingViewModel: BillingViewModel,
    subscriptionViewModel: SubscriptionStatusViewModel,
    billingClientLifecycle: BillingClientLifecycle,
    authenticationViewModel: FirebaseUserViewModel,
    modifier: Modifier = Modifier,
) {
    ClassyTaxiAppKotlinTheme {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()

        ModalNavigationDrawer(
            modifier = modifier,
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerContainerColor = Color.White,
                    content = {
                        MenuDrawer(items = {
                            DrawerMenuContent(
                                billingClientLifecycle = billingClientLifecycle,
                                subscriptionViewModel = subscriptionViewModel,
                                authenticationViewModel = authenticationViewModel,
                            )
                        })
                    })
            },
            content = {
                Scaffold(
                    topBar = {
                        ClassyTaxiTopBar {
                            coroutineScope.launch { drawerState.open() }
                        }
                    },
                ) { contentPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        MainNavigation(billingViewModel, subscriptionViewModel)
                    }
                }
            },
        )
    }
}

@Composable
private fun MainNavigation(
    billingViewModel: BillingViewModel,
    subscriptionStatusViewModel: SubscriptionStatusViewModel
) {
    SubscriptionScreen(
        billingViewModel = billingViewModel,
        subscriptionStatusViewModel = subscriptionStatusViewModel
    )
}

@Composable
fun ClassyTaxiTopBar(
    onMenuButtonClick: () -> Unit,
) {
    Surface(
        color = Color.White,
        shadowElevation = dimensionResource(id = R.dimen.top_bar_elevation)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(dimensionResource(id = R.dimen.top_bar_height))
                .padding(dimensionResource(id = R.dimen.top_bar_row_padding))
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onMenuButtonClick,
            ) {
                Icon(
                    Icons.Filled.Menu,
                    contentDescription = stringResource(
                        id = R.string.menu_content_description_text
                    ),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

    }
    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_height)))
}

@Composable
private fun MenuDrawerHeader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.menu_drawer_header_padding)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.tv_banner),
            contentDescription = stringResource(id = R.string.menu_drawer_content_description),
        )
    }
}

@Composable
fun MenuDrawer(
    items: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        MenuDrawerHeader()
        items()
    }
}

@Composable
private fun DrawerMenuContent(
    billingClientLifecycle: BillingClientLifecycle,
    subscriptionViewModel: SubscriptionStatusViewModel,
    authenticationViewModel: FirebaseUserViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val menuButtonList = listOf(
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.menu_drawer_body_button_padding)),
            onClick = {
                billingClientLifecycle.queryPurchases()
                subscriptionViewModel.manualRefresh()
            }) {
            Text(
                text = stringResource(id = R.string.refresh_menu_text),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.menu_drawer_body_button_padding)),
            onClick = {
                subscriptionViewModel.unregisterInstanceId()
                AuthUI.getInstance().signOut(context).addOnCompleteListener {
                    authenticationViewModel.updateFirebaseUser()
                }
            }) {
            Text(
                text = stringResource(id = R.string.sign_out),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    )

    LazyColumn(modifier = modifier) {
        items(menuButtonList.size) {
            menuButtonList[it]
        }
    }
}
