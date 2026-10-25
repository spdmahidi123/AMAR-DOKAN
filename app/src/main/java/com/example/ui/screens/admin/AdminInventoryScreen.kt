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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.ProductEntity
import com.example.ui.components.StockBadge
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminInventoryScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsState()
    val movements by viewModel.inventoryMovements.collectAsState()
    val isAdjustDialogOpen by viewModel.isAdjustInventoryDialogOpen.collectAsState()
    val adjustingProduct by viewModel.adjustingProduct.collectAsState()

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Stock Overview, 1: Movement Audit History
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = if (searchQuery.isBlank()) {
        products
    } else {
        val q = searchQuery.trim().lowercase()
        products.filter { it.titleBn.lowercase().contains(q) || it.sku.lowercase().contains(q) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("মজুদ স্টক পর্যবেক্ষণ", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("ইনভেন্টরি হিস্ট্রি ও অডিট", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedSubTab == 0) {
            // Stock Overview
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("পণ্য বা SKU দিয়ে খুঁজুন...", fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.titleBn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "SKU: ${product.sku} | বর্তমান মজুদ: ${AppStrings.formatBengaliDigits(product.stockQuantity.toString())}টি",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (product.isOutOfStock) StatusDanger else GreenPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                StockBadge(
                                    stockQuantity = product.stockQuantity,
                                    lowStockThreshold = product.lowStockThreshold,
                                    language = AppLanguage.BANGLA
                                )
                            }

                            Button(
                                onClick = { viewModel.openAdjustInventoryDialog(product) },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("স্টক পরিবর্তন", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        } else {
            // Movement Audit Log
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (movements.isEmpty()) {
                    item {
                        Text(
                            text = "কোনো ইনভেন্টরি মুভমেন্ট রেকর্ড নেই।",
                            color = Color.Gray,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                } else {
                    items(movements, key = { it.id }) { mov ->
                        val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(mov.timestamp))
                        val isPositive = mov.changeQuantity > 0

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mov.productName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "কারণ: ${mov.reason} • অ্যাডমিন: ${mov.adminName}",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "তারিখ: $dateFormatted",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = (if (isPositive) "+" else "") + mov.changeQuantity.toString(),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = if (isPositive) StatusSuccess else StatusDanger
                                    )
                                    Text(
                                        text = "পূর্ব: ${mov.previousStock} ➔ নতুন: ${mov.newStock}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Adjust Inventory Modal Dialog
    if (isAdjustDialogOpen && adjustingProduct != null) {
        AdjustStockDialog(
            product = adjustingProduct!!,
            onDismiss = { viewModel.closeAdjustInventoryDialog() },
            onSubmit = { delta, reason ->
                viewModel.submitInventoryAdjustment(adjustingProduct!!.id, delta, reason)
            }
        )
    }
}

@Composable
fun AdjustStockDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onSubmit: (delta: Int, reason: String) -> Unit
) {
    var deltaText by remember { mutableStateOf("") }
    var isAddition by remember { mutableStateOf(true) }
    var selectedReason by remember { mutableStateOf("নতুন চালান (New Stock Received)") }
    var isReasonDropdownOpen by remember { mutableStateOf(false) }

    val reasons = listOf(
        "নতুন চালান (New Stock Received)",
        "নষ্ট বা ক্ষতিগ্রস্ত পণ্য (Damaged Stock)",
        "কাস্টমার রিটার্ন (Customer Return)",
        "ইনভেন্টরি অডিট ও সংশোধন (Audit Correction)",
        "অন্যান্য সমন্বয় (Other Adjustment)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("স্টক সমন্বয়: ${product.titleBn}", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "বর্তমান মজুদ স্টক: ${AppStrings.formatBengaliDigits(product.stockQuantity.toString())}টি",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = GreenPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isAddition = true },
                        colors = if (isAddition) ButtonDefaults.outlinedButtonColors(containerColor = GreenPrimary.copy(alpha = 0.1f)) else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ স্টক যোগ", fontWeight = FontWeight.Bold, color = if (isAddition) GreenPrimary else Color.Gray)
                    }

                    OutlinedButton(
                        onClick = { isAddition = false },
                        colors = if (!isAddition) ButtonDefaults.outlinedButtonColors(containerColor = StatusDanger.copy(alpha = 0.1f)) else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("- স্টক হ্রাস", fontWeight = FontWeight.Bold, color = if (!isAddition) StatusDanger else Color.Gray)
                    }
                }

                OutlinedTextField(
                    value = deltaText,
                    onValueChange = { deltaText = it },
                    label = { Text("পরিমাণ (সংখ্যা)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    placeholder = { Text("যেমন: 10") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "সমন্বয়ের কারণ:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isReasonDropdownOpen = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedReason, fontSize = 12.sp)
                    }

                    DropdownMenu(
                        expanded = isReasonDropdownOpen,
                        onDismissRequest = { isReasonDropdownOpen = false }
                    ) {
                        reasons.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r, fontSize = 12.sp) },
                                onClick = {
                                    selectedReason = r
                                    isReasonDropdownOpen = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rawDelta = deltaText.toIntOrNull() ?: 0
                    if (rawDelta > 0) {
                        val finalDelta = if (isAddition) rawDelta else -rawDelta
                        onSubmit(finalDelta, selectedReason)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("সংরক্ষণ করুন", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
