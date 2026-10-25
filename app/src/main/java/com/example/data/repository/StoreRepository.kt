package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.OrderWithItems
import com.example.data.model.BusinessSettingsEntity
import com.example.data.model.CartItem
import com.example.data.model.CategoryEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.NotificationLogEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.random.Random

class StoreRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val categoryDao = database.categoryDao()
    private val orderDao = database.orderDao()
    private val inventoryDao = database.inventoryDao()
    private val settingsDao = database.settingsDao()

    // Reactive streams
    val activeProducts: Flow<List<ProductEntity>> = productDao.getActiveProducts()
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val featuredProducts: Flow<List<ProductEntity>> = productDao.getFeaturedProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    val categories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allOrdersWithItems: Flow<List<OrderWithItems>> = orderDao.getAllOrdersWithItems()
    val unacknowledgedOrderCount: Flow<Int> = orderDao.getUnacknowledgedCount()
    val inventoryMovements: Flow<List<InventoryMovementEntity>> = inventoryDao.getAllMovements()
    val settings: Flow<BusinessSettingsEntity?> = settingsDao.getSettings()
    val notificationLogs: Flow<List<NotificationLogEntity>> = settingsDao.getAllNotificationLogs()

    fun getProduct(id: Long): Flow<ProductEntity?> = productDao.getProductById(id)

    /**
     * Atomic checkout: checks stock, creates order, inserts order items,
     * decrements inventory, logs movement, and queues admin notification.
     */
    suspend fun placeOrder(
        customerName: String,
        phone: String,
        deliveryAddress: String,
        district: String,
        area: String,
        orderNotes: String,
        cartItems: List<CartItem>,
        deliveryCharge: Double
    ): Result<OrderEntity> = withContext(Dispatchers.IO) {
        if (cartItems.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("কার্ট খালি রয়েছে।"))
        }

        // 1. Stock check: ensure every requested product has sufficient stock
        for (item in cartItems) {
            val currentProduct = productDao.getProductByIdSync(item.product.id)
                ?: return@withContext Result.failure(IllegalArgumentException("পণ্য '${item.product.titleBn}' পাওয়া যায়নি।"))

            if (currentProduct.stockQuantity < item.quantity) {
                return@withContext Result.failure(
                    IllegalStateException(
                        "দুঃখিত, '${currentProduct.titleBn}'-এর পর্যাপ্ত স্টক নেই (মজুদ: ${currentProduct.stockQuantity}টি, চাওয়া হয়েছে: ${item.quantity}টি)।"
                    )
                )
            }
        }

        val subtotal = cartItems.sumOf { it.lineTotal }
        val totalAmount = subtotal + deliveryCharge
        val randomNum = Random.nextInt(1000, 9999)
        val orderNumber = "BD-2026-$randomNum"

        val order = OrderEntity(
            orderNumber = orderNumber,
            customerName = customerName.trim(),
            phone = phone.trim(),
            deliveryAddress = deliveryAddress.trim(),
            district = district.trim(),
            area = area.trim(),
            orderNotes = orderNotes.trim(),
            subtotal = subtotal,
            deliveryCharge = deliveryCharge,
            discount = 0.0,
            totalAmount = totalAmount,
            paymentMethod = "ক্যাশ অন ডেলিভারি (COD)",
            paymentStatus = PaymentStatus.UNPAID.name,
            orderStatus = OrderStatus.NEW.name,
            isAcknowledged = false,
            createdAt = System.currentTimeMillis()
        )

        val orderId = orderDao.insertOrder(order)
        val createdOrder = order.copy(id = orderId)

        // 2. Insert items and decrement stock
        val orderItems = cartItems.map { item ->
            val product = productDao.getProductByIdSync(item.product.id)!!
            val newStock = product.stockQuantity - item.quantity

            // Update stock
            productDao.updateStock(product.id, newStock)

            // Log inventory movement
            inventoryDao.insertMovement(
                InventoryMovementEntity(
                    productId = product.id,
                    productName = product.titleBn,
                    changeQuantity = -item.quantity,
                    previousStock = product.stockQuantity,
                    newStock = newStock,
                    reason = "কাস্টমার অর্ডার #$orderNumber",
                    adminName = "সিস্টেম স্বয়ংক্রিয়"
                )
            )

            OrderItemEntity(
                orderId = orderId,
                productId = product.id,
                productNameBn = product.titleBn,
                productNameEn = product.titleEn,
                sku = product.sku,
                purchaseCost = product.purchaseCost,
                sellingPrice = product.effectivePrice,
                quantity = item.quantity,
                lineTotal = item.lineTotal
            )
        }
        orderDao.insertOrderItems(orderItems)

        // 3. Queue admin notification log
        val currentSettings = settingsDao.getSettingsSync() ?: BusinessSettingsEntity()
        if (currentSettings.emailNotificationsEnabled) {
            val itemSummary = cartItems.joinToString("\n") { "• ${it.product.titleBn} x ${it.quantity} = ৳${it.lineTotal.toInt()}" }
            val emailBody = """
                নতুন অর্ডার এসেছে!
                অর্ডার নম্বর: $orderNumber
                গ্রাহক: $customerName ($phone)
                ঠিকানা: $deliveryAddress, $area, $district
                আইটেমসমূহ:
                $itemSummary
                ডেলিভারি চার্জ: ৳${deliveryCharge.toInt()}
                সর্বমোট মূল্য: ৳${totalAmount.toInt()}
                পেমেন্ট মেথড: ক্যাশ অন ডেলিভারি
            """.trimIndent()

            settingsDao.insertNotificationLog(
                NotificationLogEntity(
                    orderId = orderId,
                    orderNumber = orderNumber,
                    recipientEmail = currentSettings.adminEmail,
                    subject = "নতুন অর্ডার প্রাপ্তি #$orderNumber - ${currentSettings.storeNameBn}",
                    messageBody = emailBody,
                    status = "SENT"
                )
            )
        }

        Result.success(createdOrder)
    }

    suspend fun updateOrderStatus(
        orderId: Long,
        newStatus: OrderStatus,
        paymentStatus: PaymentStatus,
        restoreStockIfCancelled: Boolean = true
    ) = withContext(Dispatchers.IO) {
        if (newStatus == OrderStatus.CANCELLED && restoreStockIfCancelled) {
            val items = orderDao.getOrderItemsSync(orderId)
            for (item in items) {
                val product = productDao.getProductByIdSync(item.productId)
                if (product != null) {
                    val restoredStock = product.stockQuantity + item.quantity
                    productDao.updateStock(product.id, restoredStock)
                    inventoryDao.insertMovement(
                        InventoryMovementEntity(
                            productId = product.id,
                            productName = product.titleBn,
                            changeQuantity = item.quantity,
                            previousStock = product.stockQuantity,
                            newStock = restoredStock,
                            reason = "অর্ডার বাতিল হওয়ায় স্টক পুনরুদ্ধার #$orderId",
                            adminName = "অ্যাডমিন"
                        )
                    )
                }
            }
        }
        orderDao.updateOrderStatus(orderId, newStatus.name, paymentStatus.name)
    }

    suspend fun adjustInventory(
        productId: Long,
        deltaQuantity: Int,
        reason: String,
        adminName: String
    ) = withContext(Dispatchers.IO) {
        val product = productDao.getProductByIdSync(productId) ?: return@withContext
        val newStock = (product.stockQuantity + deltaQuantity).coerceAtLeast(0)
        productDao.updateStock(productId, newStock)
        inventoryDao.insertMovement(
            InventoryMovementEntity(
                productId = product.id,
                productName = product.titleBn,
                changeQuantity = deltaQuantity,
                previousStock = product.stockQuantity,
                newStock = newStock,
                reason = reason,
                adminName = adminName
            )
        )
    }

    suspend fun saveProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        if (product.id == 0L) {
            val newId = productDao.insertProduct(product)
            inventoryDao.insertMovement(
                InventoryMovementEntity(
                    productId = newId,
                    productName = product.titleBn,
                    changeQuantity = product.stockQuantity,
                    previousStock = 0,
                    newStock = product.stockQuantity,
                    reason = "নতুন পণ্য অন্তর্ভুক্তি (প্রারম্ভিক স্টক)",
                    adminName = "অ্যাডমিন"
                )
            )
        } else {
            productDao.updateProduct(product)
        }
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun markAllOrdersAcknowledged() = withContext(Dispatchers.IO) {
        orderDao.markAllAcknowledged()
    }

    suspend fun markOrderAcknowledged(orderId: Long) = withContext(Dispatchers.IO) {
        orderDao.markAcknowledged(orderId)
    }

    suspend fun updateSettings(settings: BusinessSettingsEntity) = withContext(Dispatchers.IO) {
        settingsDao.insertOrUpdate(settings)
    }

    suspend fun getSettingsSync(): BusinessSettingsEntity = withContext(Dispatchers.IO) {
        settingsDao.getSettingsSync() ?: BusinessSettingsEntity()
    }
}
