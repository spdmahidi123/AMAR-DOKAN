package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.CartItem
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.ProductEntity
import com.example.data.repository.StoreRepository
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: StoreRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = StoreRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Amar Dokan", appName)
    }

    @Test
    fun testBengaliPriceFormatting() {
        val priceBn = AppStrings.formatPrice(1250.0, AppLanguage.BANGLA)
        assertEquals("৳১,২৫০", priceBn)

        val priceEn = AppStrings.formatPrice(1250.0, AppLanguage.ENGLISH)
        assertEquals("৳1,250", priceEn)
    }

    @Test
    fun testOrderPlacementAndStockDeduction() = runTest {
        // Insert a product with stock 10
        val product = ProductEntity(
            titleBn = "ঢাকাই জামদানি",
            titleEn = "Dhakai Jamdani",
            descriptionBn = "বিবরণ",
            descriptionEn = "Description",
            categoryBn = "পোশাক",
            categoryEn = "Fashion",
            sku = "TEST-JAM",
            purchaseCost = 3000.0,
            sellingPrice = 4000.0,
            discountPrice = 3800.0,
            stockQuantity = 10,
            lowStockThreshold = 2
        )
        val productId = database.productDao().insertProduct(product)
        val inserted = database.productDao().getProductByIdSync(productId)
        assertNotNull(inserted)
        assertEquals(10, inserted!!.stockQuantity)

        // Place order for 3 items
        val cartItems = listOf(
            CartItem(product = inserted, quantity = 3)
        )
        val result = repository.placeOrder(
            customerName = "রহিম আহমেদ",
            phone = "01711223344",
            deliveryAddress = "ধানমন্ডি, ঢাকা",
            district = "ঢাকা",
            area = "ধানমন্ডি",
            orderNotes = "জরুরি ডেলিভারি",
            cartItems = cartItems,
            deliveryCharge = 60.0
        )

        assertTrue("Order should be placed successfully", result.isSuccess)
        val createdOrder = result.getOrNull()
        assertNotNull(createdOrder)
        assertTrue(createdOrder!!.orderNumber.startsWith("BD-2026-"))
        assertEquals(3800.0 * 3 + 60.0, createdOrder.totalAmount, 0.01)

        // Verify stock deducted to 7
        val updatedProduct = database.productDao().getProductByIdSync(productId)
        assertEquals(7, updatedProduct!!.stockQuantity)

        // Verify inventory movement was logged
        val movements = database.inventoryDao().getAllMovements().first()
        assertEquals(1, movements.size)
        assertEquals(-3, movements[0].changeQuantity)
        assertEquals(10, movements[0].previousStock)
        assertEquals(7, movements[0].newStock)
    }

    @Test
    fun testOrderCancellationRestoresStock() = runTest {
        val product = ProductEntity(
            titleBn = "খাঁটি মধু",
            titleEn = "Pure Honey",
            descriptionBn = "বিবরণ",
            descriptionEn = "Description",
            categoryBn = "মুদি",
            categoryEn = "Groceries",
            sku = "TEST-HON",
            purchaseCost = 400.0,
            sellingPrice = 600.0,
            stockQuantity = 5
        )
        val productId = database.productDao().insertProduct(product)
        val inserted = database.productDao().getProductByIdSync(productId)!!

        val result = repository.placeOrder(
            customerName = "করিম",
            phone = "01811223344",
            deliveryAddress = "গুলশান, ঢাকা",
            district = "ঢাকা",
            area = "গুলশান",
            orderNotes = "",
            cartItems = listOf(CartItem(product = inserted, quantity = 2)),
            deliveryCharge = 60.0
        )
        assertTrue(result.isSuccess)
        val order = result.getOrNull()!!
        assertEquals(3, database.productDao().getProductByIdSync(productId)!!.stockQuantity)

        // Cancel order and verify stock restores to 5
        repository.updateOrderStatus(
            orderId = order.id,
            newStatus = OrderStatus.CANCELLED,
            paymentStatus = PaymentStatus.UNPAID,
            restoreStockIfCancelled = true
        )

        val restoredProduct = database.productDao().getProductByIdSync(productId)!!
        assertEquals(5, restoredProduct.stockQuantity)
    }
}
