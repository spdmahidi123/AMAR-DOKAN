package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentStatus
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

@Composable
fun StockBadge(
    stockQuantity: Int,
    lowStockThreshold: Int,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text) = when {
        stockQuantity <= 0 -> Triple(
            StatusDanger.copy(alpha = 0.12f),
            StatusDanger,
            if (language == AppLanguage.BANGLA) "স্টক শেষ" else "Out of Stock"
        )
        stockQuantity <= lowStockThreshold -> Triple(
            StatusWarning.copy(alpha = 0.15f),
            StatusWarning,
            if (language == AppLanguage.BANGLA) "সীমিত স্টক (${AppStrings.formatBengaliDigits(stockQuantity.toString())}টি)" else "Low Stock ($stockQuantity)"
        )
        else -> Triple(
            StatusSuccess.copy(alpha = 0.12f),
            StatusSuccess,
            if (language == AppLanguage.BANGLA) "ইন-স্টক (${AppStrings.formatBengaliDigits(stockQuantity.toString())}টি)" else "In Stock ($stockQuantity)"
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun StatusChip(
    status: String,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        OrderStatus.NEW.name -> Triple(
            AmberSecondary.copy(alpha = 0.15f),
            AmberSecondary,
            if (language == AppLanguage.BANGLA) OrderStatus.NEW.labelBn else OrderStatus.NEW.labelEn
        )
        OrderStatus.CONFIRMED.name -> Triple(
            StatusInfo.copy(alpha = 0.15f),
            StatusInfo,
            if (language == AppLanguage.BANGLA) OrderStatus.CONFIRMED.labelBn else OrderStatus.CONFIRMED.labelEn
        )
        OrderStatus.PROCESSING.name -> Triple(
            Color(0xFF8B5CF6).copy(alpha = 0.15f),
            Color(0xFF7C3AED),
            if (language == AppLanguage.BANGLA) OrderStatus.PROCESSING.labelBn else OrderStatus.PROCESSING.labelEn
        )
        OrderStatus.SHIPPED.name -> Triple(
            Color(0xFF0284C7).copy(alpha = 0.15f),
            Color(0xFF0369A1),
            if (language == AppLanguage.BANGLA) OrderStatus.SHIPPED.labelBn else OrderStatus.SHIPPED.labelEn
        )
        OrderStatus.DELIVERED.name -> Triple(
            StatusSuccess.copy(alpha = 0.15f),
            StatusSuccess,
            if (language == AppLanguage.BANGLA) OrderStatus.DELIVERED.labelBn else OrderStatus.DELIVERED.labelEn
        )
        OrderStatus.CANCELLED.name -> Triple(
            StatusDanger.copy(alpha = 0.15f),
            StatusDanger,
            if (language == AppLanguage.BANGLA) OrderStatus.CANCELLED.labelBn else OrderStatus.CANCELLED.labelEn
        )
        PaymentStatus.PAID.name -> Triple(
            StatusSuccess.copy(alpha = 0.15f),
            StatusSuccess,
            if (language == AppLanguage.BANGLA) PaymentStatus.PAID.labelBn else PaymentStatus.PAID.labelEn
        )
        PaymentStatus.UNPAID.name -> Triple(
            StatusWarning.copy(alpha = 0.15f),
            StatusWarning,
            if (language == AppLanguage.BANGLA) PaymentStatus.UNPAID.labelBn else PaymentStatus.UNPAID.labelEn
        )
        else -> Triple(
            Color.LightGray.copy(alpha = 0.3f),
            Color.DarkGray,
            status
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreHeader(
    storeName: String,
    language: AppLanguage,
    cartItemCount: Int,
    onLanguageToggle: () -> Unit,
    onCartClick: () -> Unit,
    onAdminSecretAccess: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var secretTapCount by remember { mutableIntStateOf(0) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GreenPrimary,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    secretTapCount++
                    if (secretTapCount >= 5) {
                        secretTapCount = 0
                        onAdminSecretAccess?.invoke()
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AmberSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = storeName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (language == AppLanguage.BANGLA) "অনলাইন দেশীয় শপ" else "Authentic BD Store",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        },
        actions = {
            // Language switch button
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(end = 4.dp)
                    .clickable { onLanguageToggle() }
                    .testTag("lang_toggle_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.BANGLA) "EN" else "বাংলা",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Cart with live badge
            IconButton(
                onClick = onCartClick,
                modifier = Modifier.testTag("cart_icon_btn")
            ) {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge(
                                containerColor = AmberSecondary,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BANGLA)
                                        AppStrings.formatBengaliDigits(cartItemCount.toString())
                                    else
                                        cartItemCount.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Shopping Cart",
                        tint = Color.White
                    )
                }
            }
        },
        modifier = modifier
    )
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
