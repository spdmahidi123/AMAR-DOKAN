package com.example.ui.screens.store

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusSuccess

@Composable
fun OrderSuccessScreen(
    order: OrderEntity,
    language: AppLanguage,
    onBackToStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToStore() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("order_success_screen")
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Big Green Checkmark
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(StatusSuccess),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (language == AppLanguage.BANGLA) "অর্ডার সফল হয়েছে!" else "Order Successful!",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = GreenPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (language == AppLanguage.BANGLA)
                "ধন্যবাদ! আপনার অর্ডারটি সাফল্যের সাথে গ্রহণ করা হয়েছে।"
            else
                "Thank you! Your order has been placed successfully.",
            fontSize = 13.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Order Number Badge
        Surface(
            color = GreenPrimary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.border(1.dp, GreenPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.BANGLA) "অর্ডার নং: " else "Order ID: ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = order.orderNumber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Summary details card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (language == AppLanguage.BANGLA) "ডেলিভারি ও পেমেন্ট বিবরণী" else "Delivery & Payment Summary",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                InfoRow(label = if (language == AppLanguage.BANGLA) "গ্রাহকের নাম" else "Customer", value = order.customerName)
                InfoRow(label = if (language == AppLanguage.BANGLA) "মোবাইল নম্বর" else "Phone", value = order.phone)
                InfoRow(label = if (language == AppLanguage.BANGLA) "জেলা ও এলাকা" else "District & Area", value = "${order.district}, ${order.area}")
                InfoRow(label = if (language == AppLanguage.BANGLA) "সম্পূর্ণ ঠিকানা" else "Address", value = order.deliveryAddress)

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE2E8F0))

                InfoRow(label = if (language == AppLanguage.BANGLA) "পেমেন্ট মাধ্যম" else "Payment Method", value = order.paymentMethod)
                InfoRow(
                    label = if (language == AppLanguage.BANGLA) "সর্বমোট প্রদেয়" else "Total Payable",
                    value = AppStrings.formatPrice(order.totalAmount, language),
                    isHighlight = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Support Notice
        Card(
            colors = CardDefaults.cardColors(containerColor = AmberSecondary.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AmberSecondary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = AmberSecondary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "প্যাকেজিং শুরু করার আগে আমাদের কাস্টমার কেয়ার প্রতিনিধি আপনার নম্বরে কল করে অর্ডার নিশ্চিত করবেন।"
                    else
                        "Our customer support will call you shortly to confirm the delivery schedule.",
                    fontSize = 12.sp,
                    color = Color(0xFF78350F),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Return to Store Button
        Button(
            onClick = onBackToStore,
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("continue_shopping_btn")
        ) {
            Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (language == AppLanguage.BANGLA) "আরও কেনাকাটা করুন" else "Continue Shopping",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.DarkGray)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) GreenPrimary else Color.Black
        )
    }
}
