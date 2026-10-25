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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.OrderWithItems
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentStatus
import com.example.ui.components.StatusChip
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminOrdersScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.filteredOrders.collectAsState()
    val searchQuery by viewModel.orderSearchQuery.collectAsState()
    val statusFilter by viewModel.orderStatusFilter.collectAsState()
    val selectedOrder by viewModel.selectedOrder.collectAsState()

    val statusTabs = listOf(
        "ALL" to "সব অর্ডার",
        OrderStatus.NEW.name to "নতুন (${OrderStatus.NEW.labelBn})",
        OrderStatus.CONFIRMED.name to OrderStatus.CONFIRMED.labelBn,
        OrderStatus.PROCESSING.name to OrderStatus.PROCESSING.labelBn,
        OrderStatus.SHIPPED.name to OrderStatus.SHIPPED.labelBn,
        OrderStatus.DELIVERED.name to OrderStatus.DELIVERED.labelBn,
        OrderStatus.CANCELLED.name to OrderStatus.CANCELLED.labelBn
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setOrderSearchQuery(it) },
            placeholder = { Text("অর্ডার নম্বর বা ফোন নম্বর দিয়ে খুঁজুন...", fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setOrderSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(statusTabs) { (key, label) ->
                FilterChip(
                    selected = statusFilter == key,
                    onClick = { viewModel.setOrderStatusFilter(key) },
                    label = { Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Orders List
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "কোনো অর্ডার পাওয়া যায়নি।",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(orders, key = { it.order.id }) { orderWithItems ->
                    val order = orderWithItems.order
                    val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(order.createdAt))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectOrder(orderWithItems) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = order.orderNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusChip(status = order.orderStatus, language = AppLanguage.BANGLA)
                                }
                                StatusChip(status = order.paymentStatus, language = AppLanguage.BANGLA)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "গ্রাহক: ${order.customerName} (${order.phone})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "ঠিকানা: ${order.deliveryAddress}, ${order.area}, ${order.district}",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE2E8F0))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${AppStrings.formatBengaliDigits(orderWithItems.items.size.toString())}টি পণ্য • তারিখ: $dateFormatted",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )

                                Text(
                                    text = AppStrings.formatPrice(order.totalAmount, AppLanguage.BANGLA),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Order Detail Modal Dialog
    if (selectedOrder != null) {
        OrderDetailDialog(
            orderWithItems = selectedOrder!!,
            onDismiss = { viewModel.selectOrder(null) },
            onUpdateStatus = { newStatus, paymentStatus ->
                viewModel.updateOrderStatus(selectedOrder!!.order.id, newStatus, paymentStatus)
            },
            onAcknowledge = {
                viewModel.markOrderAcknowledged(selectedOrder!!.order.id)
            }
        )
    }
}

@Composable
fun OrderDetailDialog(
    orderWithItems: OrderWithItems,
    onDismiss: () -> Unit,
    onUpdateStatus: (OrderStatus, PaymentStatus) -> Unit,
    onAcknowledge: () -> Unit
) {
    val order = orderWithItems.order
    var currentStatus by remember { mutableStateOf(OrderStatus.valueOf(order.orderStatus)) }
    var currentPayment by remember { mutableStateOf(PaymentStatus.valueOf(order.paymentStatus)) }
    var isStatusMenuOpen by remember { mutableStateOf(false) }
    var isPaymentMenuOpen by remember { mutableStateOf(false) }

    val dateFormatted = SimpleDateFormat("dd MMMM, yyyy - hh:mm a", Locale.getDefault()).format(Date(order.createdAt))

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onUpdateStatus(currentStatus, currentPayment)
                    onAcknowledge()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("পরিবর্তন সংরক্ষণ করুন", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "অর্ডার #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                StatusChip(status = order.orderStatus, language = AppLanguage.BANGLA)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "তারিখ: $dateFormatted", fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(10.dp))

                // Customer box
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "গ্রাহক: ${order.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "ফোন: ${order.phone}", fontSize = 12.sp, color = GreenPrimary, fontWeight = FontWeight.SemiBold)
                        Text(text = "ঠিকানা: ${order.deliveryAddress}", fontSize = 12.sp)
                        Text(text = "জেলা ও এলাকা: ${order.district}, ${order.area}", fontSize = 12.sp)
                        if (order.orderNotes.isNotBlank()) {
                            Text(text = "নোট: ${order.orderNotes}", fontSize = 11.sp, color = Color(0xFF92400E))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Items list
                Text(text = "অর্ডারকৃত পণ্যসমূহ:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))

                orderWithItems.items.forEach { item ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "${item.productNameBn} (x${item.quantity})", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(text = "SKU: ${item.sku} | ক্রয়: ৳${item.purchaseCost.toInt()}", fontSize = 10.sp, color = Color.Gray)
                        }
                        Text(
                            text = AppStrings.formatPrice(item.lineTotal, AppLanguage.BANGLA),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "ডেলিভারি চার্জ:", fontSize = 12.sp)
                    Text(text = AppStrings.formatPrice(order.deliveryCharge, AppLanguage.BANGLA), fontSize = 12.sp)
                }

                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "সর্বমোট মূল্য:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = AppStrings.formatPrice(order.totalAmount, AppLanguage.BANGLA),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GreenPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status dropdowns
                Text(text = "অর্ডার স্ট্যাটাস পরিবর্তন করুন:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isStatusMenuOpen = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("স্ট্যাটাস: ${currentStatus.labelBn}")
                    }
                    DropdownMenu(
                        expanded = isStatusMenuOpen,
                        onDismissRequest = { isStatusMenuOpen = false }
                    ) {
                        OrderStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.labelBn) },
                                onClick = {
                                    currentStatus = status
                                    isStatusMenuOpen = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Payment status dropdown
                Text(text = "পেমেন্ট স্ট্যাটাস:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isPaymentMenuOpen = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("পেমেন্ট: ${currentPayment.labelBn}")
                    }
                    DropdownMenu(
                        expanded = isPaymentMenuOpen,
                        onDismissRequest = { isPaymentMenuOpen = false }
                    ) {
                        PaymentStatus.entries.forEach { pay ->
                            DropdownMenuItem(
                                text = { Text(pay.labelBn) },
                                onClick = {
                                    currentPayment = pay
                                    isPaymentMenuOpen = false
                                }
                            )
                        }
                    }
                }

                if (currentStatus == OrderStatus.CANCELLED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ নোটিশ: অর্ডার বাতিল করলে আইটেমগুলোর স্টক পুনরায় ইনভেন্টরিতে যোগ হবে।",
                        fontSize = 11.sp,
                        color = StatusDanger
                    )
                }
            }
        }
    )
}
