package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class OrderStatus(val labelBn: String, val labelEn: String) {
    NEW("নতুন", "New"),
    CONFIRMED("নিশ্চিত", "Confirmed"),
    PROCESSING("প্রসেসিং", "Processing"),
    SHIPPED("ডেলিভারির পথে", "Shipped"),
    DELIVERED("ডেলিভার্ড", "Delivered"),
    CANCELLED("বাতিল", "Cancelled")
}

enum class PaymentStatus(val labelBn: String, val labelEn: String) {
    UNPAID("পরিশোধিত নয়", "Unpaid"),
    PAID("পরিশোধিত", "Paid")
}

@Entity(
    tableName = "products",
    indices = [Index(value = ["sku"], unique = true), Index(value = ["categoryEn"])]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val descriptionEn: String,
    val categoryBn: String,
    val categoryEn: String,
    val sku: String,
    val purchaseCost: Double,
    val sellingPrice: Double,
    val discountPrice: Double? = null,
    val stockQuantity: Int,
    val lowStockThreshold: Int = 5,
    val isFeatured: Boolean = false,
    val isActive: Boolean = true,
    val imageResName: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val effectivePrice: Double
        get() = discountPrice ?: sellingPrice

    val isLowStock: Boolean
        get() = stockQuantity in 1..lowStockThreshold

    val isOutOfStock: Boolean
        get() = stockQuantity <= 0
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameBn: String,
    val nameEn: String,
    val iconName: String,
    val displayOrder: Int = 0
)

@Entity(
    tableName = "orders",
    indices = [Index(value = ["orderNumber"], unique = true), Index(value = ["createdAt"])]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val customerName: String,
    val phone: String,
    val deliveryAddress: String,
    val district: String,
    val area: String,
    val orderNotes: String = "",
    val subtotal: Double,
    val deliveryCharge: Double,
    val discount: Double = 0.0,
    val totalAmount: Double,
    val paymentMethod: String = "ক্যাশ অন ডেলিভারি (COD)",
    val paymentStatus: String = PaymentStatus.UNPAID.name,
    val orderStatus: String = OrderStatus.NEW.name,
    val isAcknowledged: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"]), Index(value = ["productId"])]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productNameBn: String,
    val productNameEn: String,
    val sku: String,
    val purchaseCost: Double,
    val sellingPrice: Double,
    val quantity: Int,
    val lineTotal: Double
)

@Entity(
    tableName = "inventory_movements",
    indices = [Index(value = ["productId"]), Index(value = ["timestamp"])]
)
data class InventoryMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val changeQuantity: Int, // e.g. -2 for customer sale, +20 for restock
    val previousStock: Int,
    val newStock: Int,
    val reason: String, // "কাস্টমার অর্ডার", "নতুন চালান", "ক্ষতিগ্রস্ত পণ্য", "রিটার্ন", etc.
    val adminName: String = "Admin",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_settings")
data class BusinessSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val storeNameBn: String = "আমার দোকান",
    val storeNameEn: String = "Amar Dokan",
    val insideDhakaDeliveryCharge: Double = 60.0,
    val outsideDhakaDeliveryCharge: Double = 120.0,
    val adminPin: String = "1234",
    val adminEmail: String = "admin@amardokan.com.bd",
    val emailNotificationsEnabled: Boolean = true,
    val soundAlertsEnabled: Boolean = true
)

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val orderNumber: String,
    val recipientEmail: String,
    val subject: String,
    val messageBody: String,
    val status: String = "SENT", // "SENT" or "FAILED"
    val timestamp: Long = System.currentTimeMillis()
)

data class CartItem(
    val product: ProductEntity,
    val quantity: Int
) {
    val lineTotal: Double
        get() = product.effectivePrice * quantity
}
