package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.BusinessSettingsEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.NotificationLogEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemEntity
import com.example.data.model.ProductEntity
import kotlinx.coroutines.flow.Flow

data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItemEntity>
)

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND isFeatured = 1 ORDER BY createdAt DESC")
    fun getFeaturedProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductByIdSync(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE stockQuantity <= lowStockThreshold AND isActive = 1")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET stockQuantity = :newStock WHERE id = :productId")
    suspend fun updateStock(productId: Long, newStock: Int)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY displayOrder ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: Long): Flow<OrderEntity?>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderWithItems(orderId: Long): Flow<OrderWithItems?>

    @Transaction
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrdersWithItems(): Flow<List<OrderWithItems>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItemsSync(orderId: Long): List<OrderItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("UPDATE orders SET orderStatus = :status, paymentStatus = :paymentStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String, paymentStatus: String)

    @Query("UPDATE orders SET isAcknowledged = 1 WHERE id = :orderId")
    suspend fun markAcknowledged(orderId: Long)

    @Query("UPDATE orders SET isAcknowledged = 1")
    suspend fun markAllAcknowledged()

    @Query("SELECT COUNT(*) FROM orders WHERE isAcknowledged = 0")
    fun getUnacknowledgedCount(): Flow<Int>
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<InventoryMovementEntity>>

    @Query("SELECT * FROM inventory_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsForProduct(productId: Long): Flow<List<InventoryMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: InventoryMovementEntity): Long
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM business_settings WHERE id = 1")
    fun getSettings(): Flow<BusinessSettingsEntity?>

    @Query("SELECT * FROM business_settings WHERE id = 1")
    suspend fun getSettingsSync(): BusinessSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: BusinessSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationLog(log: NotificationLogEntity): Long

    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC LIMIT 50")
    fun getAllNotificationLogs(): Flow<List<NotificationLogEntity>>
}
