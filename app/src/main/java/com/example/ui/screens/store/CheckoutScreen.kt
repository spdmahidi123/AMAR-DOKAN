package com.example.ui.screens.store

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.CheckoutState
import com.example.ui.viewmodel.DeliveryZone
import com.example.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: StoreViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val language by viewModel.language.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()
    val deliveryFee by viewModel.currentDeliveryFee.collectAsState()
    val total by viewModel.cartTotal.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()

    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("ঢাকা") }
    var area by remember { mutableStateOf("") }
    var orderNotes by remember { mutableStateOf("") }

    val popularDistricts = listOf(
        "ঢাকা", "চট্টগ্রাম", "সিলেট", "রাজশাহী", "খুলনা", "বরিশাল",
        "রংপুর", "ময়মনসিংহ", "কুমিল্লা", "গাজীপুর", "নারায়ণগঞ্জ", "বগুড়া"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "অর্ডার চেকআউট" else "Order Checkout",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Error banner if any
            if (checkoutState is CheckoutState.Error) {
                Surface(
                    color = StatusDanger.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.dp, StatusDanger.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = StatusDanger)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = (checkoutState as CheckoutState.Error).message,
                            color = StatusDanger,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Customer Contact Information Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "১. গ্রাহকের তথ্য ও ডেলিভারি ঠিকানা" else "1. Customer & Delivery Address",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text(if (language == AppLanguage.BANGLA) "আপনার পূর্ণ নাম *" else "Full Name *") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 11) phone = it },
                        label = { Text(if (language == AppLanguage.BANGLA) "মোবাইল নম্বর (১১ ডিজিট) *" else "Phone Number (01XXXXXXXXX) *") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        placeholder = { Text("017XXXXXXXX") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_phone_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (language == AppLanguage.BANGLA) "জেলা নির্বাচন করুন:" else "Select District:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(popularDistricts) { d ->
                            FilterChip(
                                selected = district == d,
                                onClick = {
                                    district = d
                                    if (d == "ঢাকা") {
                                        viewModel.setDeliveryZone(DeliveryZone.INSIDE_DHAKA)
                                    } else {
                                        viewModel.setDeliveryZone(DeliveryZone.OUTSIDE_DHAKA)
                                    }
                                },
                                label = { Text(d, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text(if (language == AppLanguage.BANGLA) "থানা / উপজেলা / এলাকা" else "Area / Thana") },
                        leadingIcon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_area_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text(if (language == AppLanguage.BANGLA) "সম্পূর্ণ ঠিকানা (বাসা/রোড নম্বর) *" else "Detailed Street Address *") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_address_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = orderNotes,
                        onValueChange = { orderNotes = it },
                        label = { Text(if (language == AppLanguage.BANGLA) "বিশেষ কোনো নির্দেশনা (ঐচ্ছিক)" else "Order Notes (Optional)") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Notes, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Payment Option Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "২. পেমেন্ট মেথড" else "2. Payment Method",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = GreenPrimary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, GreenPrimary, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.BANGLA) "ক্যাশ অন ডেলিভারি (COD)" else "Cash on Delivery (COD)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (language == AppLanguage.BANGLA) "পণ্য হাতে পেয়ে ডেলিভারিম্যানকে মূল্য পরিশোধ করুন।" else "Pay cash to the delivery agent upon receiving the package.",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Order Summary & Financials
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "অর্ডার সারাংশ" else "Order Summary",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    cart.values.forEach { item ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            val itemName = if (language == AppLanguage.BANGLA) item.product.titleBn else item.product.titleEn
                            Text(
                                text = "• $itemName x ${AppStrings.formatQuantity(item.quantity, language)}",
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = AppStrings.formatPrice(item.lineTotal, language),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFCBD5E1))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = AppStrings.subtotal(language), fontSize = 13.sp)
                        Text(text = AppStrings.formatPrice(subtotal, language), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(text = AppStrings.deliveryCharge(language), fontSize = 13.sp)
                        Text(text = AppStrings.formatPrice(deliveryFee, language), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFCBD5E1))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = AppStrings.totalAmount(language), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = AppStrings.formatPrice(total, language),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GreenPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Button
            Button(
                onClick = {
                    viewModel.submitOrder(
                        customerName = customerName,
                        phone = phone,
                        deliveryAddress = deliveryAddress,
                        district = district,
                        area = area,
                        orderNotes = orderNotes
                    )
                },
                enabled = checkoutState !is CheckoutState.Submitting,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_order_button")
            ) {
                if (checkoutState is CheckoutState.Submitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "অর্ডার প্রসেস হচ্ছে...", color = Color.White)
                } else {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "অর্ডার নিশ্চিত করুন (ক্যাশ অন ডেলিভারি)" else "Confirm Order (Cash on Delivery)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
