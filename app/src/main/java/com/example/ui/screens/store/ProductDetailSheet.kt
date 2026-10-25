package com.example.ui.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductEntity
import com.example.ui.components.ProductImageDisplay
import com.example.ui.components.StockBadge
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailSheet(
    product: ProductEntity,
    language: AppLanguage,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAddToCart: (quantity: Int) -> Unit,
    onBuyNow: (quantity: Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    val title = if (language == AppLanguage.BANGLA) product.titleBn else product.titleEn
    val desc = if (language == AppLanguage.BANGLA) product.descriptionBn else product.descriptionEn
    val category = if (language == AppLanguage.BANGLA) product.categoryBn else product.categoryEn
    val hasDiscount = product.discountPrice != null && product.discountPrice < product.sellingPrice

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Close button and Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                ProductImageDisplay(
                    imageResName = product.imageResName,
                    title = title,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                StockBadge(
                    stockQuantity = product.stockQuantity,
                    lowStockThreshold = product.lowStockThreshold,
                    language = language,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                )
            }

            // Main Content Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = category,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )

                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "SKU: ${product.sku}",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Price display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = AppStrings.formatPrice(product.effectivePrice, language),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GreenPrimary
                    )

                    if (hasDiscount) {
                        Text(
                            text = AppStrings.formatPrice(product.sellingPrice, language),
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )

                        val savings = (product.sellingPrice - (product.discountPrice ?: product.sellingPrice)).toInt()
                        Surface(
                            color = AmberSecondary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.BANGLA)
                                    "৳${AppStrings.formatBengaliDigits(savings.toString())} সঞ্চয়"
                                else
                                    "Save ৳$savings",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description Box
                Text(
                    text = if (language == AppLanguage.BANGLA) "পণ্যের বিবরণ" else "Product Description",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Delivery information guarantee card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.BANGLA)
                                    "ঢাকায় ২৪-৪৮ ঘণ্টা | ঢাকার বাইরে ২-৩ দিন"
                                else
                                    "Dhaka: 24-48 hrs | Nationwide: 2-3 days",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = AmberSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.BANGLA)
                                    "ক্যাশ অন ডেলিভারি (পণ্য দেখে মূল্য পরিশোধ)"
                                else
                                    "Cash on Delivery available nationwide",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quantity selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "পরিমাণ নির্ধারণ করুন:" else "Select Quantity:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                        }

                        Text(
                            text = if (language == AppLanguage.BANGLA)
                                AppStrings.formatBengaliDigits(quantity.toString())
                            else
                                quantity.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        IconButton(
                            onClick = { if (quantity < product.stockQuantity) quantity++ },
                            enabled = quantity < product.stockQuantity,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons: Add to Cart and Buy Now
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            onAddToCart(quantity)
                            onDismiss()
                        },
                        enabled = product.stockQuantity > 0,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_add_to_cart_btn")
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BANGLA) "কার্টে রাখুন" else "Add to Cart",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            onBuyNow(quantity)
                            onDismiss()
                        },
                        enabled = product.stockQuantity > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_buy_now_btn")
                    ) {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "এখনই কিনুন" else "Buy Now",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
