package com.example.ui.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.ui.components.ProductImageDisplay
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.viewmodel.DeliveryZone
import com.example.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartSheet(
    viewModel: StoreViewModel,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onProceedToCheckout: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()
    val deliveryZone by viewModel.deliveryZone.collectAsState()
    val deliveryFee by viewModel.currentDeliveryFee.collectAsState()
    val total by viewModel.cartTotal.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val cartItems = cart.values.toList()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.cart(language),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (cartItems.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearCart() }) {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "খালি করুন" else "Clear All",
                            color = StatusDanger,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            if (cartItems.isEmpty()) {
                // Empty Cart State
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveShoppingCart,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (language == AppLanguage.BANGLA) "আপনার কার্ট বর্তমানে খালি" else "Your cart is currently empty",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (language == AppLanguage.BANGLA) "পছন্দের পণ্য কার্টে যুক্ত করুন" else "Add items from the store to proceed",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "কেনাকাটা শুরু করুন" else "Start Shopping",
                            color = Color.White
                        )
                    }
                }
            } else {
                // Cart Items List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(cartItems, key = { it.product.id }) { item ->
                        CartItemRow(
                            item = item,
                            language = language,
                            onQuantityChange = { newQty -> viewModel.updateCartQuantity(item.product.id, newQty) },
                            onRemove = { viewModel.removeFromCart(item.product.id) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // Delivery Zone selection
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "ডেলিভারি লোকেশন নির্বাচন করুন:" else "Select Delivery Location:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Inside Dhaka Option
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (deliveryZone == DeliveryZone.INSIDE_DHAKA) GreenPrimary.copy(alpha = 0.1f) else Color(0xFFF8FAFC),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    1.dp,
                                    if (deliveryZone == DeliveryZone.INSIDE_DHAKA) GreenPrimary else Color(0xFFCBD5E1),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setDeliveryZone(DeliveryZone.INSIDE_DHAKA) }
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = deliveryZone == DeliveryZone.INSIDE_DHAKA,
                                    onClick = { viewModel.setDeliveryZone(DeliveryZone.INSIDE_DHAKA) },
                                    colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = AppStrings.insideDhaka(language),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = AppStrings.formatPrice(settings.insideDhakaDeliveryCharge, language),
                                        fontSize = 11.sp,
                                        color = GreenPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Outside Dhaka Option
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (deliveryZone == DeliveryZone.OUTSIDE_DHAKA) GreenPrimary.copy(alpha = 0.1f) else Color(0xFFF8FAFC),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    1.dp,
                                    if (deliveryZone == DeliveryZone.OUTSIDE_DHAKA) GreenPrimary else Color(0xFFCBD5E1),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setDeliveryZone(DeliveryZone.OUTSIDE_DHAKA) }
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = deliveryZone == DeliveryZone.OUTSIDE_DHAKA,
                                    onClick = { viewModel.setDeliveryZone(DeliveryZone.OUTSIDE_DHAKA) },
                                    colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = AppStrings.outsideDhaka(language),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = AppStrings.formatPrice(settings.outsideDhakaDeliveryCharge, language),
                                        fontSize = 11.sp,
                                        color = GreenPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Summary & Checkout Button
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = AppStrings.subtotal(language), fontSize = 13.sp, color = Color.DarkGray)
                            Text(text = AppStrings.formatPrice(subtotal, language), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = AppStrings.deliveryCharge(language), fontSize = 13.sp, color = Color.DarkGray)
                            Text(text = AppStrings.formatPrice(deliveryFee, language), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFCBD5E1))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = AppStrings.totalAmount(language), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(text = AppStrings.formatPrice(total, language), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = GreenPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onDismiss()
                        onProceedToCheckout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 16.dp)
                        .testTag("proceed_to_checkout_btn")
                ) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "অর্ডার করতে এগিয়ে যান" else "Proceed to Checkout",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    language: AppLanguage,
    onQuantityChange: (newQty: Int) -> Unit,
    onRemove: () -> Unit
) {
    val title = if (language == AppLanguage.BANGLA) item.product.titleBn else item.product.titleEn

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                ProductImageDisplay(imageResName = item.product.imageResName, title = title)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = AppStrings.formatPrice(item.product.effectivePrice, language),
                    fontSize = 13.sp,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                    .padding(2.dp)
            ) {
                IconButton(
                    onClick = { onQuantityChange(item.quantity - 1) },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                }

                Text(
                    text = if (language == AppLanguage.BANGLA)
                        AppStrings.formatBengaliDigits(item.quantity.toString())
                    else
                        item.quantity.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                IconButton(
                    onClick = { onQuantityChange(item.quantity + 1) },
                    enabled = item.quantity < item.product.stockQuantity,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                }
            }

            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusDanger, modifier = Modifier.size(18.dp))
            }
        }
    }
}
