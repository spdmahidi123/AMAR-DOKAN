package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusChip
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.AdminViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val orders by viewModel.allOrdersWithItems.collectAsState()
    val unreadCount by viewModel.unacknowledgedOrderCount.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Welcome / Alert Banner if there are new unacknowledged orders
        if (unreadCount > 0) {
            item {
                Surface(
                    color = AmberSecondary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AmberSecondary, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AmberSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "নতুন ${AppStrings.formatBengaliDigits(unreadCount.toString())}টি অর্ডার এসেছে!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF78350F)
                            )
                            Text(
                                text = "পর্যালোচনা ও প্যাকেজিং শুরু করার জন্য অর্ডার তালিকায় যান।",
                                fontSize = 12.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                        Button(
                            onClick = { viewModel.setActiveTab(AdminTab.ORDERS) },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = "দেখুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. High Level Financial KPIs (Revenue & Profit)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                KpiCard(
                    title = "মোট বিক্রয় (রেভিনিউ)",
                    value = AppStrings.formatPrice(stats.totalSalesRevenue, AppLanguage.BANGLA),
                    subtitle = "${AppStrings.formatBengaliDigits(stats.totalOrders.toString())}টি অর্ডার",
                    icon = Icons.Default.AttachMoney,
                    iconBgColor = GreenPrimary,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "আনুমানিক গ্রস লাভ",
                    value = AppStrings.formatPrice(stats.estimatedGrossProfit, AppLanguage.BANGLA),
                    subtitle = "ক্রয়মূল্য ও বিক্রিমূল্য থেকে",
                    icon = Icons.Default.TrendingUp,
                    iconBgColor = AmberSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Operational Order KPIs (New, Shipped, Delivered, Cancelled)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MiniStatCard(
                    label = "নতুন অর্ডার",
                    count = stats.newOrders,
                    textColor = AmberSecondary,
                    bgColor = AmberSecondary.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setOrderStatusFilter("NEW")
                        viewModel.setActiveTab(AdminTab.ORDERS)
                    }
                )

                MiniStatCard(
                    label = "নিশ্চিত",
                    count = stats.confirmedOrders,
                    textColor = StatusInfo,
                    bgColor = StatusInfo.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setOrderStatusFilter("CONFIRMED")
                        viewModel.setActiveTab(AdminTab.ORDERS)
                    }
                )

                MiniStatCard(
                    label = "ডেলিভার্ড",
                    count = stats.deliveredOrders,
                    textColor = StatusSuccess,
                    bgColor = StatusSuccess.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setOrderStatusFilter("DELIVERED")
                        viewModel.setActiveTab(AdminTab.ORDERS)
                    }
                )

                MiniStatCard(
                    label = "বাতিল",
                    count = stats.cancelledOrders,
                    textColor = StatusDanger,
                    bgColor = StatusDanger.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setOrderStatusFilter("CANCELLED")
                        viewModel.setActiveTab(AdminTab.ORDERS)
                    }
                )
            }
        }

        // 4. Low-Stock Warning Alert Banner (if any)
        if (stats.lowStockCount > 0) {
            item {
                Surface(
                    color = StatusDanger.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, StatusDanger.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = StatusDanger)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "স্টক সতর্কতা: ${AppStrings.formatBengaliDigits(stats.lowStockCount.toString())}টি পণ্যের স্টক শেষ বা সীমিত!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusDanger
                            )
                            Text(
                                text = "দ্রুত ইনভেন্টরি আপডেট করুন যাতে কাস্টমার অর্ডার বাতিল না হয়।",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.setActiveTab(AdminTab.INVENTORY) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDanger),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("স্টক দেখুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5. Quick Shortcut Actions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "দ্রুত অ্যাকশন (Quick Actions)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ActionChip(
                            title = "পণ্য যোগ",
                            icon = Icons.Default.Add,
                            color = GreenPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.setActiveTab(AdminTab.PRODUCTS)
                                viewModel.openNewProductDialog()
                            }
                        )

                        ActionChip(
                            title = "ইনভেন্টরি",
                            icon = Icons.Default.Inventory,
                            color = AmberSecondary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setActiveTab(AdminTab.INVENTORY) }
                        )

                        ActionChip(
                            title = "অর্ডারসমূহ",
                            icon = Icons.Default.Receipt,
                            color = StatusInfo,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setActiveTab(AdminTab.ORDERS) }
                        )

                        ActionChip(
                            title = "রিপোর্ট",
                            icon = Icons.Default.Assessment,
                            color = CrimsonAccent,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setActiveTab(AdminTab.REPORTS) }
                        )
                    }
                }
            }
        }

        // 6. Recent Orders Section Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "সাম্প্রতিক অর্ডারসমূহ (${AppStrings.formatBengaliDigits(orders.size.toString())}টি)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "সব দেখুন",
                    fontSize = 12.sp,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.setActiveTab(AdminTab.ORDERS) }
                )
            }
        }

        // 7. Recent Orders items
        items(orders.take(5), key = { it.order.id }) { orderWithItems ->
            val order = orderWithItems.order
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.selectOrder(orderWithItems)
                        viewModel.setActiveTab(AdminTab.ORDERS)
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = order.orderNumber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusChip(status = order.orderStatus, language = AppLanguage.BANGLA)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${order.customerName} • ${order.phone} • ${order.district}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )

                        Text(
                            text = "${AppStrings.formatBengaliDigits(orderWithItems.items.size.toString())}টি আইটেম • সর্বমোট: ${AppStrings.formatPrice(order.totalAmount, AppLanguage.BANGLA)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GreenPrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Details",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    count: Int,
    textColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Text(
                text = AppStrings.formatBengaliDigits(count.toString()),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
private fun ActionChip(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
