package com.example.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ProductEntity
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.GreenPrimary

@Composable
fun ProductImageDisplay(
    imageResName: String,
    title: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Check if matching generated local drawable resource exists
    val localResId = when (imageResName) {
        "product_jamdani" -> R.drawable.product_jamdani
        "product_panjabi" -> R.drawable.product_panjabi
        "product_honey" -> R.drawable.product_honey
        "store_hero_banner" -> R.drawable.store_hero_banner
        else -> 0
    }

    Box(
        modifier = modifier
            .background(Color(0xFFF1F5F9)),
        contentAlignment = Alignment.Center
    ) {
        if (localResId != 0) {
            Image(
                painter = painterResource(id = localResId),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (imageResName.startsWith("http://") || imageResName.startsWith("https://")) {
            AsyncImage(
                model = imageResName,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Elegant illustrated placeholder
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Amar Dokan",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    language: AppLanguage,
    onProductClick: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = if (language == AppLanguage.BANGLA) product.titleBn else product.titleEn
    val category = if (language == AppLanguage.BANGLA) product.categoryBn else product.categoryEn
    val hasDiscount = product.discountPrice != null && product.discountPrice < product.sellingPrice

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onProductClick() }
            .testTag("product_card_${product.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image Box with discount badge and stock status
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                ProductImageDisplay(
                    imageResName = product.imageResName,
                    title = title,
                    modifier = Modifier.fillMaxSize()
                )

                // Discount Pill
                if (hasDiscount) {
                    val discountSavings = (product.sellingPrice - (product.discountPrice ?: product.sellingPrice)).toInt()
                    Surface(
                        color = CrimsonAccent,
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 8.dp, bottomEnd = 8.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (language == AppLanguage.BANGLA)
                                    "৳${AppStrings.formatBengaliDigits(discountSavings.toString())} ছাড়"
                                else
                                    "৳$discountSavings OFF",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Stock Badge on bottom-right of image
                StockBadge(
                    stockQuantity = product.stockQuantity,
                    lowStockThreshold = product.lowStockThreshold,
                    language = language,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                )
            }

            // Info details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = AppStrings.formatPrice(product.effectivePrice, language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GreenPrimary
                    )

                    if (hasDiscount) {
                        Text(
                            text = AppStrings.formatPrice(product.sellingPrice, language),
                            style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add to Cart button
                Button(
                    onClick = onAddToCart,
                    enabled = product.stockQuantity > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        disabledContainerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("add_to_cart_btn_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Add to Cart",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (product.stockQuantity <= 0) {
                            if (language == AppLanguage.BANGLA) "স্টক নেই" else "Out of Stock"
                        } else {
                            if (language == AppLanguage.BANGLA) "কার্টে যোগ করুন" else "Add to Cart"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
