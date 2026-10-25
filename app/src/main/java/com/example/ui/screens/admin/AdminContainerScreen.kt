package com.example.ui.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.GreenPrimary
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminContainerScreen(
    viewModel: AdminViewModel,
    onBackToStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToStore() }

    val activeTab by viewModel.activeTab.collectAsState()
    val unreadOrderCount by viewModel.unacknowledgedOrderCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "অ্যাডমিন: ${activeTab.titleBn}",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToStore,
                        modifier = Modifier.testTag("admin_back_to_store_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Store",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Quick return to store
                    IconButton(onClick = onBackToStore) {
                        Icon(imageVector = Icons.Default.Store, contentDescription = "View Storefront", tint = Color.White)
                    }

                    // Logout
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("admin_logout_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // Dashboard Tab
                NavigationBarItem(
                    selected = activeTab == AdminTab.DASHBOARD,
                    onClick = { viewModel.setActiveTab(AdminTab.DASHBOARD) },
                    icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("ড্যাশবোর্ড", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = GreenPrimary, indicatorColor = GreenPrimary.copy(alpha = 0.15f))
                )

                // Orders Tab with unread badge
                NavigationBarItem(
                    selected = activeTab == AdminTab.ORDERS,
                    onClick = { viewModel.setActiveTab(AdminTab.ORDERS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadOrderCount > 0) {
                                    Badge(containerColor = AmberSecondary) {
                                        Text(
                                            text = AppStrings.formatBengaliDigits(unreadOrderCount.toString()),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Receipt, contentDescription = "Orders")
                        }
                    },
                    label = { Text("অর্ডার", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = GreenPrimary, indicatorColor = GreenPrimary.copy(alpha = 0.15f))
                )

                // Products Tab
                NavigationBarItem(
                    selected = activeTab == AdminTab.PRODUCTS,
                    onClick = { viewModel.setActiveTab(AdminTab.PRODUCTS) },
                    icon = { Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = "Products") },
                    label = { Text("পণ্য", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = GreenPrimary, indicatorColor = GreenPrimary.copy(alpha = 0.15f))
                )

                // Inventory Tab
                NavigationBarItem(
                    selected = activeTab == AdminTab.INVENTORY,
                    onClick = { viewModel.setActiveTab(AdminTab.INVENTORY) },
                    icon = { Icon(imageVector = Icons.Default.Inventory, contentDescription = "Inventory") },
                    label = { Text("স্টক", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = GreenPrimary, indicatorColor = GreenPrimary.copy(alpha = 0.15f))
                )

                // Reports Tab
                NavigationBarItem(
                    selected = activeTab == AdminTab.REPORTS,
                    onClick = { viewModel.setActiveTab(AdminTab.REPORTS) },
                    icon = { Icon(imageVector = Icons.Default.Assessment, contentDescription = "Reports") },
                    label = { Text("রিপোর্ট", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = GreenPrimary, indicatorColor = GreenPrimary.copy(alpha = 0.15f))
                )

                // Settings Tab
                NavigationBarItem(
                    selected = activeTab == AdminTab.SETTINGS,
                    onClick = { viewModel.setActiveTab(AdminTab.SETTINGS) },
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("সেটিংস", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = GreenPrimary, indicatorColor = GreenPrimary.copy(alpha = 0.15f))
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                AdminTab.DASHBOARD -> AdminDashboardScreen(viewModel = viewModel)
                AdminTab.ORDERS -> AdminOrdersScreen(viewModel = viewModel)
                AdminTab.PRODUCTS -> AdminProductsScreen(viewModel = viewModel)
                AdminTab.INVENTORY -> AdminInventoryScreen(viewModel = viewModel)
                AdminTab.REPORTS -> AdminReportsScreen(viewModel = viewModel)
                AdminTab.SETTINGS -> AdminSettingsScreen(viewModel = viewModel)
                AdminTab.NOTIFICATIONS -> AdminSettingsScreen(viewModel = viewModel)
            }
        }
    }
}
