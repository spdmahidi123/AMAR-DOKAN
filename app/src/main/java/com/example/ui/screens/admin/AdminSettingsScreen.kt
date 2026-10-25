package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminSettingsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val notificationLogs by viewModel.notificationLogs.collectAsState()

    var insideDhakaFee by remember(settings) { mutableStateOf(settings.insideDhakaDeliveryCharge.toInt().toString()) }
    var outsideDhakaFee by remember(settings) { mutableStateOf(settings.outsideDhakaDeliveryCharge.toInt().toString()) }
    var adminPin by remember(settings) { mutableStateOf(settings.adminPin) }
    var adminEmail by remember(settings) { mutableStateOf(settings.adminEmail) }
    var emailNotifications by remember(settings) { mutableStateOf(settings.emailNotificationsEnabled) }
    var soundAlerts by remember(settings) { mutableStateOf(settings.soundAlertsEnabled) }

    var savedMessage by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "স্টোর সেটিংস ও নোটিফিকেশন কনফিগারেশন",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        if (savedMessage) {
            Surface(
                color = StatusSuccess.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✓ সেটিংস সফলভাবে সংরক্ষিত হয়েছে!",
                    color = StatusSuccess,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Delivery Charge Configuration Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ডেলিভারি চার্জ নির্ধারণ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = insideDhakaFee,
                        onValueChange = { insideDhakaFee = it },
                        label = { Text("ঢাকার ভেতরে (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = outsideDhakaFee,
                        onValueChange = { outsideDhakaFee = it },
                        label = { Text("ঢাকার বাইরে (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Security & Admin PIN
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "নিরাপত্তা ও অ্যাক্সেস পিন",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = adminPin,
                    onValueChange = { adminPin = it },
                    label = { Text("অ্যাডমিন লগইন পিন (PIN)") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Notification & Audio Alert Options
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "অর্ডার নোটিফিকেশন অ্যালার্ট",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = adminEmail,
                    onValueChange = { adminEmail = it },
                    label = { Text("অ্যাডমিন ইমেইল ঠিকানা (অর্ডার অ্যালার্টের জন্য)") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "ইমেইল নোটিফিকেশন চালু", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "নতুন অর্ডার আসলে স্বয়ংক্রিয়ভাবে ইমেইল প্রেরণ", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(checked = emailNotifications, onCheckedChange = { emailNotifications = it })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "শব্দ ও ভাইব্রেশন অ্যালার্ট", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "নতুন অর্ডার আসলে সংকেত বাজবে", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(checked = soundAlerts, onCheckedChange = { soundAlerts = it })
                }
            }
        }

        // Save Button
        Button(
            onClick = {
                val inside = insideDhakaFee.toDoubleOrNull() ?: 60.0
                val outside = outsideDhakaFee.toDoubleOrNull() ?: 120.0
                viewModel.updateSettings(
                    insideDhakaFee = inside,
                    outsideDhakaFee = outside,
                    adminPin = adminPin,
                    adminEmail = adminEmail,
                    emailNotifications = emailNotifications,
                    soundAlerts = soundAlerts
                )
                savedMessage = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "সেটিংস সংরক্ষণ করুন", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Notification Delivery Logs Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "প্রেরিত অর্ডার নোটিফিকেশন অডিট লগ (${notificationLogs.size}টি)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (notificationLogs.isEmpty()) {
                    Text(
                        text = "কোনো নোটিফিকেশন লগ তৈরি হয়নি। নতুন অর্ডার প্রাপ্ত হলে এখানে সম্পূর্ণ ইমেইল ও নোটিফিকেশন অডিট রেকর্ড দেখা যাবে।",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                } else {
                    notificationLogs.take(5).forEach { log ->
                        val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "অর্ডার #${log.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = log.status, color = StatusSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(text = "প্রাপক: ${log.recipientEmail} • $dateFormatted", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = log.subject, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
