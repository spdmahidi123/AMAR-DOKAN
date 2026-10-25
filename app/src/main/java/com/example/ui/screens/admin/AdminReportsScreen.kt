package com.example.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.AdminViewModel

data class BestSellerItem(
    val productName: String,
    val sku: String,
    val unitsSold: Int,
    val totalRevenue: Double
)

@Composable
fun AdminReportsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val orders by viewModel.allOrdersWithItems.collectAsState()

    // Calculate best sellers from order items of non-cancelled orders
    val completedOrders = orders.filter { it.order.orderStatus != OrderStatus.CANCELLED.name }
    val allItems = completedOrders.flatMap { it.items }

    val bestSellers = allItems
        .groupBy { it.productId }
        .map { (_, items) ->
            val first = items.first()
            BestSellerItem(
                productName = first.productNameBn,
                sku = first.sku,
                unitsSold = items.sumOf { it.quantity },
                totalRevenue = items.sumOf { it.lineTotal }
            )
        }
        .sortedByDescending { it.unitsSold }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "বিক্রয় ও লাভ রিপোর্ট (Financial & Sales Analytics)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "প্রকৃত ডাটাবেজ রেকর্ড ও ক্রয়মূল্য সমন্বয় করে তৈরি",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Summary Metric Cards
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ReportMetricCard(
                    title = "মোট সংগৃহীত বিক্রয়",
                    amount = AppStrings.formatPrice(stats.totalSalesRevenue, AppLanguage.BANGLA),
                    subtitle = "সফল অর্ডারসমূহ থেকে",
                    color = GreenPrimary,
                    modifier = Modifier.weight(1f)
                )

                ReportMetricCard(
                    title = "মোট গ্রস প্রফিট (লাভ)",
                    amount = AppStrings.formatPrice(stats.estimatedGrossProfit, AppLanguage.BANGLA),
                    subtitle = "(বিক্রয়মূল্য - ক্রয়মূল্য)",
                    color = AmberSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ReportMetricCard(
                    title = "মজুদ মালের মোট মূল্য",
                    amount = AppStrings.formatPrice(stats.totalInventoryValue, AppLanguage.BANGLA),
                    subtitle = "ইনভেন্টরি অ্যাসেট ভ্যালু",
                    color = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f)
                )

                ReportMetricCard(
                    title = "অর্ডার সম্পন্ন হার",
                    amount = if (stats.totalOrders > 0) "${((stats.deliveredOrders + stats.confirmedOrders) * 100 / stats.totalOrders)}%" else "0%",
                    subtitle = "ডেলিভার্ড ও নিশ্চিত",
                    color = CrimsonAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Best Sellers Leaderboard Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Leaderboard, contentDescription = null, tint = GreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "সর্বাধিক বিক্রিত পণ্যসমূহ (Top Selling Products)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (bestSellers.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "পর্যাপ্ত বিক্রয় ডাটা নেই। কাস্টমার অর্ডার সম্পন্ন হলে এখানে শীর্ষ বিক্রিত পণ্য দেখা যাবে।",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            itemsIndexed(bestSellers) { index, item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (index < 3) AmberSecondary else Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#${index + 1}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (index < 3) Color.White else Color.DarkGray
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.productName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "SKU: ${item.sku} • বিক্রয়: ${AppStrings.formatBengaliDigits(item.unitsSold.toString())}টি",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Text(
                            text = AppStrings.formatPrice(item.totalRevenue, AppLanguage.BANGLA),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GreenPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportMetricCard(
    title: String,
    amount: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = amount, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
