package com.example.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductEntity
import com.example.ui.components.ProductImageDisplay
import com.example.ui.components.StockBadge
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.viewmodel.AdminViewModel

@Composable
fun AdminProductsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsState()
    val isProductDialogOpen by viewModel.isProductDialogOpen.collectAsState()
    val editingProduct by viewModel.editingProduct.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    val filteredList = if (searchQuery.isBlank()) {
        products
    } else {
        val q = searchQuery.trim().lowercase()
        products.filter {
            it.titleBn.lowercase().contains(q) ||
                it.titleEn.lowercase().contains(q) ||
                it.sku.lowercase().contains(q)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openNewProductDialog() },
                containerColor = GreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
                .padding(16.dp)
        ) {
            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("পণ্যের নাম বা SKU দিয়ে খুঁজুন...", fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "মোট পণ্য: ${AppStrings.formatBengaliDigits(filteredList.size.toString())}টি",
                fontSize = 13.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { product ->
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
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                ProductImageDisplay(imageResName = product.imageResName, title = product.titleBn)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.titleBn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "SKU: ${product.sku} | ক্যাটাগরি: ${product.categoryBn}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "বিক্রয়: ৳${product.effectivePrice.toInt()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary
                                    )
                                    Text(
                                        text = "ক্রয়: ৳${product.purchaseCost.toInt()}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                StockBadge(
                                    stockQuantity = product.stockQuantity,
                                    lowStockThreshold = product.lowStockThreshold,
                                    language = AppLanguage.BANGLA
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row {
                                    IconButton(
                                        onClick = { viewModel.openEditProductDialog(product) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = GreenPrimary, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = { productToDelete = product },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusDanger, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Product Add/Edit Dialog
    if (isProductDialogOpen) {
        ProductFormDialog(
            existing = editingProduct,
            onDismiss = { viewModel.closeProductDialog() },
            onSave = { titleBn, titleEn, descBn, descEn, catBn, catEn, sku, cost, price, disc, stock, lowStock, featured, active, img ->
                viewModel.saveProduct(
                    id = editingProduct?.id ?: 0L,
                    titleBn = titleBn,
                    titleEn = titleEn,
                    descBn = descBn,
                    descEn = descEn,
                    categoryBn = catBn,
                    categoryEn = catEn,
                    sku = sku,
                    purchaseCost = cost,
                    sellingPrice = price,
                    discountPrice = disc,
                    stock = stock,
                    lowStockThreshold = lowStock,
                    isFeatured = featured,
                    isActive = active,
                    imageResName = img
                )
            }
        )
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("পণ্য মুছে ফেলতে চান?") },
            text = { Text("'${productToDelete?.titleBn}' পণ্যটি মুছে ফেলবেন? এটি অপরিবর্তনযোগ্য।") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(productToDelete!!)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
                ) {
                    Text("মুছে ফেলুন", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun ProductFormDialog(
    existing: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (
        titleBn: String,
        titleEn: String,
        descBn: String,
        descEn: String,
        catBn: String,
        catEn: String,
        sku: String,
        cost: Double,
        price: Double,
        disc: Double?,
        stock: Int,
        lowStock: Int,
        featured: Boolean,
        active: Boolean,
        imageResName: String
    ) -> Unit
) {
    var titleBn by remember { mutableStateOf(existing?.titleBn ?: "") }
    var titleEn by remember { mutableStateOf(existing?.titleEn ?: "") }
    var descBn by remember { mutableStateOf(existing?.descriptionBn ?: "") }
    var descEn by remember { mutableStateOf(existing?.descriptionEn ?: "") }
    var categoryBn by remember { mutableStateOf(existing?.categoryBn ?: "পোশাক ও ফ্যাশন") }
    var sku by remember { mutableStateOf(existing?.sku ?: "") }
    var purchaseCost by remember { mutableStateOf(existing?.purchaseCost?.toInt()?.toString() ?: "500") }
    var sellingPrice by remember { mutableStateOf(existing?.sellingPrice?.toInt()?.toString() ?: "800") }
    var discountPrice by remember { mutableStateOf(existing?.discountPrice?.toInt()?.toString() ?: "") }
    var stockQuantity by remember { mutableStateOf(existing?.stockQuantity?.toString() ?: "20") }
    var lowStockThreshold by remember { mutableStateOf(existing?.lowStockThreshold?.toString() ?: "5") }
    var isFeatured by remember { mutableStateOf(existing?.isFeatured ?: false) }
    var isActive by remember { mutableStateOf(existing?.isActive ?: true) }
    var imageResName by remember { mutableStateOf(existing?.imageResName ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "পোশাক ও ফ্যাশন" to "Fashion",
        "খাদ্য ও মুদি" to "Groceries",
        "চামড়াজাত পণ্য" to "Leather",
        "গৃহ ও সাজসজ্জা" to "Home & Living",
        "ইলেকট্রনিক্স" to "Electronics"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existing == null) "নতুন পণ্য যুক্ত করুন" else "পণ্য সম্পাদনা",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = StatusDanger, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = titleBn,
                    onValueChange = { titleBn = it },
                    label = { Text("পণ্যের নাম (বাংলা) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = titleEn,
                    onValueChange = { titleEn = it },
                    label = { Text("Product Name (English)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("SKU / পণ্য কোড * (যেমন: PAN-202)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = purchaseCost,
                        onValueChange = { purchaseCost = it },
                        label = { Text("ক্রয়মূল্য (৳) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { sellingPrice = it },
                        label = { Text("বিক্রয়মূল্য (৳) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = discountPrice,
                        onValueChange = { discountPrice = it },
                        label = { Text("ছাড়মূল্য (ঐচ্ছিক)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = stockQuantity,
                        onValueChange = { stockQuantity = it },
                        label = { Text("মজুদ স্টক *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = lowStockThreshold,
                    onValueChange = { lowStockThreshold = it },
                    label = { Text("কম স্টক সতর্কতা থ্রেশহোল্ড") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descBn,
                    onValueChange = { descBn = it },
                    label = { Text("পণ্যের বিস্তারিত বিবরণ (বাংলা)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imageResName,
                    onValueChange = { imageResName = it },
                    label = { Text("ছবির নাম বা URL (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: product_jamdani অথবা https://...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ফিচার্ড কালেকশনে প্রদর্শন করুন", fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("পণ্যটি সক্রিয় (Active) রাখুন", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleBn.isBlank() || sku.isBlank()) {
                        errorMessage = "পণ্যের নাম এবং SKU অবশ্যই পূরণ করতে হবে।"
                        return@Button
                    }
                    val cost = purchaseCost.toDoubleOrNull() ?: 0.0
                    val price = sellingPrice.toDoubleOrNull() ?: 0.0
                    val disc = discountPrice.toDoubleOrNull()
                    val stock = stockQuantity.toIntOrNull() ?: 0
                    val lowStock = lowStockThreshold.toIntOrNull() ?: 5

                    if (price <= 0 || cost < 0 || stock < 0) {
                        errorMessage = "মূল্য ও স্টক সঠিক সংখ্যা হতে হবে (ঋণাত্মক হতে পারবে না)।"
                        return@Button
                    }

                    val cat = categories.firstOrNull { it.first == categoryBn } ?: ("পোশাক ও ফ্যাশন" to "Fashion")

                    onSave(
                        titleBn,
                        titleEn.ifBlank { titleBn },
                        descBn,
                        descEn.ifBlank { descBn },
                        cat.first,
                        cat.second,
                        sku,
                        cost,
                        price,
                        disc,
                        stock,
                        lowStock,
                        isFeatured,
                        isActive,
                        imageResName
                    )
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
