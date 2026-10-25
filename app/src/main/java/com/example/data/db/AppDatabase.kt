package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BusinessSettingsEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.NotificationLogEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemEntity
import com.example.data.model.ProductEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        InventoryMovementEntity::class,
        BusinessSettingsEntity::class,
        NotificationLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun orderDao(): OrderDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "amar_dokan_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            // Seed Categories
            val categories = listOf(
                CategoryEntity(nameBn = "সব পণ্য", nameEn = "All", iconName = "store", displayOrder = 0),
                CategoryEntity(nameBn = "পোশাক ও ফ্যাশন", nameEn = "Fashion", iconName = "checkroom", displayOrder = 1),
                CategoryEntity(nameBn = "খাদ্য ও মুদি", nameEn = "Groceries", iconName = "shopping_basket", displayOrder = 2),
                CategoryEntity(nameBn = "চামড়াজাত পণ্য", nameEn = "Leather", iconName = "work", displayOrder = 3),
                CategoryEntity(nameBn = "গৃহ ও সাজসজ্জা", nameEn = "Home & Living", iconName = "weekend", displayOrder = 4),
                CategoryEntity(nameBn = "ইলেকট্রনিক্স", nameEn = "Electronics", iconName = "devices", displayOrder = 5)
            )
            db.categoryDao().insertCategories(categories)

            // Seed Products
            val products = listOf(
                ProductEntity(
                    titleBn = "ঐতিহ্যবাহী লাল-সোনালী ঢাকাই জামদানি শাড়ি",
                    titleEn = "Authentic Red & Gold Dhakai Jamdani Saree",
                    descriptionBn = "রূপগঞ্জের দক্ষ তাঁতিদের হাতে বোনা ১০০ কাউন্টের সুতি ঢাকাই জামদানি শাড়ি। উৎসব ও বিশেষ অনুষ্ঠানের জন্য অতুলনীয় সৌন্দর্য।",
                    descriptionEn = "Handwoven 100-count cotton Dhakai Jamdani saree by master weavers of Rupganj. Elegant festive red & golden motifs.",
                    categoryBn = "পোশাক ও ফ্যাশন",
                    categoryEn = "Fashion",
                    sku = "JAM-001",
                    purchaseCost = 3200.0,
                    sellingPrice = 4500.0,
                    discountPrice = 3950.0,
                    stockQuantity = 12,
                    lowStockThreshold = 4,
                    isFeatured = true,
                    isActive = true,
                    imageResName = "product_jamdani"
                ),
                ProductEntity(
                    titleBn = "প্রিমিয়াম এমারেল্ড কটন সেমি-ফিটেড পাঞ্জাবি",
                    titleEn = "Premium Emerald Cotton Designer Panjabi",
                    descriptionBn = "উচ্চমানের ১০০% অর্গানিক সুতি কাপড়ে তৈরি। কলার ও হাতায় সূক্ষ্ম সোনালী এমব্রয়ডারি করা। গরমে আরামদায়ক ও দীর্ঘস্থায়ী।",
                    descriptionEn = "Crafted from 100% fine organic cotton with subtle embroidery detailing on the mandarin collar and cuffs.",
                    categoryBn = "পোশাক ও ফ্যাশন",
                    categoryEn = "Fashion",
                    sku = "PAN-102",
                    purchaseCost = 1100.0,
                    sellingPrice = 1850.0,
                    discountPrice = 1550.0,
                    stockQuantity = 25,
                    lowStockThreshold = 5,
                    isFeatured = true,
                    isActive = true,
                    imageResName = "product_panjabi"
                ),
                ProductEntity(
                    titleBn = "সুন্দরবনের খাঁটি বুনো পদ্ম মধু (৫০০ গ্রাম)",
                    titleEn = "Pure Wild Sundarban Raw Honey (500g)",
                    descriptionBn = "মৌয়ালদের মাধ্যমে সরাসরি সুন্দরবনের গভীর ম্যানগ্রোভ বন থেকে সংগৃহীত। কোনো প্রকার কৃত্রিম চিনি বা রাসায়নিকমুক্ত ১০০% খাঁটি কাঁচা মধু।",
                    descriptionEn = "Raw, unpasteurized natural wild forest honey collected straight from the mangrove depths of Sundarbans.",
                    categoryBn = "খাদ্য ও মুদি",
                    categoryEn = "Groceries",
                    sku = "HON-201",
                    purchaseCost = 450.0,
                    sellingPrice = 750.0,
                    discountPrice = 650.0,
                    stockQuantity = 30,
                    lowStockThreshold = 6,
                    isFeatured = true,
                    isActive = true,
                    imageResName = "product_honey"
                ),
                ProductEntity(
                    titleBn = "পদ্মার তাজা নদীর রূপালী ইলিশ (১.২ কেজি)",
                    titleEn = "Fresh Padma River Silver Hilsa Fish (1.2kg)",
                    descriptionBn = "চাঁদপুরের মোহনা থেকে আহরিত পদ্মার আসল রূপালী ইলিশ। অসাধারণ স্বাদ ও সুবাস। বরফ দিয়ে বিশেষ প্যাকেজিংয়ে অক্ষত অবস্থায় সরবরাহ।",
                    descriptionEn = "Fresh silver Hilsa caught from Chandpur Padma waters. Authentic aroma and melt-in-mouth richness. Premium iced cold chain packaging.",
                    categoryBn = "খাদ্য ও মুদি",
                    categoryEn = "Groceries",
                    sku = "HIL-301",
                    purchaseCost = 1500.0,
                    sellingPrice = 2100.0,
                    discountPrice = 1950.0,
                    stockQuantity = 8,
                    lowStockThreshold = 3,
                    isFeatured = true,
                    isActive = true,
                    imageResName = ""
                ),
                ProductEntity(
                    titleBn = "সিলেটের সুগন্ধি ব্ল্যাক টি পাতা (৪০০ গ্রাম)",
                    titleEn = "Sylhet Premium Golden Broken Pekoe Tea (400g)",
                    descriptionBn = "শ্রীমঙ্গলের পাহাড়ি চা বাগান থেকে তোলা সেরা চা পাতা। গাঢ় লিকার এবং সতেজ ঘ্রাণে সকালের শুরু হোক অনন্য।",
                    descriptionEn = "Finest fresh leaves hand-picked from the misty hill gardens of Sreemangal, Sylhet. Robust liquor and soothing aroma.",
                    categoryBn = "খাদ্য ও মুদি",
                    categoryEn = "Groceries",
                    sku = "TEA-401",
                    purchaseCost = 210.0,
                    sellingPrice = 380.0,
                    discountPrice = 330.0,
                    stockQuantity = 45,
                    lowStockThreshold = 8,
                    isFeatured = false,
                    isActive = true,
                    imageResName = ""
                ),
                ProductEntity(
                    titleBn = "খাঁটি দেশীয় চামড়ার প্রিমিয়াম মেনস ওয়ালেট",
                    titleEn = "Genuine Full-Grain Leather Bi-Fold Wallet",
                    descriptionBn = "হাজারীবাগের সেরা ট্যানারির ফুল-গ্রেইন চামড়া থেকে নিখুঁত সেলাইয়ে প্রস্তুত। আরএফআইডি ব্লকিং প্রযুক্তি ও একাধিক কার্ড স্লটযুক্ত।",
                    descriptionEn = "Handcrafted from durable full-grain domestic leather with RFID shielding and rich vegetable tanning.",
                    categoryBn = "চামড়াজাত পণ্য",
                    categoryEn = "Leather",
                    sku = "LEA-501",
                    purchaseCost = 600.0,
                    sellingPrice = 1150.0,
                    discountPrice = 950.0,
                    stockQuantity = 18,
                    lowStockThreshold = 5,
                    isFeatured = true,
                    isActive = true,
                    imageResName = ""
                ),
                ProductEntity(
                    titleBn = "হাতে তৈরি ঐতিহ্যবাহী নকশিকাঁথা (কিং সাইজ)",
                    titleEn = "Hand-Embroidered Traditional Nakshi Kantha (King Size)",
                    descriptionBn = "যশোরের ঐতিহ্যবাহী নকশিকাঁথা। নিখুঁত সূচিশিল্পে বাংলার লোকগাঁথা ফুটিয়ে তোলা হয়েছে। উপহার ও বসার ঘরের শোভা বর্ধনে দারুণ।",
                    descriptionEn = "Heritage King-sized quilt hand-stitched by rural women artisans of Jashore with traditional folk motifs.",
                    categoryBn = "গৃহ ও সাজসজ্জা",
                    categoryEn = "Home & Living",
                    sku = "NAK-601",
                    purchaseCost = 2200.0,
                    sellingPrice = 3500.0,
                    discountPrice = 2990.0,
                    stockQuantity = 7,
                    lowStockThreshold = 2,
                    isFeatured = true,
                    isActive = true,
                    imageResName = ""
                ),
                ProductEntity(
                    titleBn = "অ্যামোলেড ডিসপ্লে স্মার্টওয়াচ বিডি স্পেশাল",
                    titleEn = "AMOLED Calling Smartwatch (Bangla Supported)",
                    descriptionBn = "বাংলা ফন্ট সাপোর্ট, ব্লুটুথ কলিং, হার্ট রেট ও স্লিপ ট্র্যাকারসহ দীর্ঘ ৭ দিনের ব্যাটারি লাইফ। আইপি৬৮ ওয়াটারপ্রুফ।",
                    descriptionEn = "1.43\" AMOLED vibrant screen, Bluetooth calling, health tracking with Bangla font notification support.",
                    categoryBn = "ইলেকট্রনিক্স",
                    categoryEn = "Electronics",
                    sku = "WAT-701",
                    purchaseCost = 1850.0,
                    sellingPrice = 2850.0,
                    discountPrice = 2450.0,
                    stockQuantity = 14,
                    lowStockThreshold = 4,
                    isFeatured = false,
                    isActive = true,
                    imageResName = ""
                ),
                ProductEntity(
                    titleBn = "দিনাজপুরের সুগন্ধি কাটারিভোগ চাল (৫ কেজি)",
                    titleEn = "Dinajpur Fragrant Kataribhog Aromatic Rice (5kg)",
                    descriptionBn = "দিনাজপুরের নিজস্ব জমিতে উৎপাদিত সুগন্ধি কাটারিভোগ চাল। পোলাও, বিরিয়ানি ও পায়েস তৈরির জন্য উৎকৃষ্ট মান।",
                    descriptionEn = "Authentic Dinajpur aromatic Kataribhog grain, naturally aged for special polao and biryani delicacies.",
                    categoryBn = "খাদ্য ও মুদি",
                    categoryEn = "Groceries",
                    sku = "RIC-801",
                    purchaseCost = 480.0,
                    sellingPrice = 700.0,
                    discountPrice = 640.0,
                    stockQuantity = 35,
                    lowStockThreshold = 6,
                    isFeatured = false,
                    isActive = true,
                    imageResName = ""
                ),
                ProductEntity(
                    titleBn = "খাঁটি সরিষার তেল কাঠের ঘানি ভাঙা (১ লিটার)",
                    titleEn = "Wood-Cold-Pressed Pure Mustard Oil (1L)",
                    descriptionBn = "প্রাকৃতিক কাঠের ঘানিতে ধীরে ভাঙা ঝাঁঝালো খাঁটি সরিষার তেল। ভর্তা ও ঐতিহ্যবাহী রান্নায় অতুলনীয় সুবাস।",
                    descriptionEn = "Pungent, wood-pressed golden virgin mustard oil cold pressed to retain all natural nutrients and intense aroma.",
                    categoryBn = "খাদ্য ও মুদি",
                    categoryEn = "Groceries",
                    sku = "OIL-901",
                    purchaseCost = 250.0,
                    sellingPrice = 380.0,
                    discountPrice = 340.0,
                    stockQuantity = 3, // Low stock demo!
                    lowStockThreshold = 5,
                    isFeatured = false,
                    isActive = true,
                    imageResName = ""
                )
            )
            db.productDao().insertProducts(products)

            // Seed Business Settings
            val settings = BusinessSettingsEntity(
                id = 1,
                storeNameBn = "আমার দোকান",
                storeNameEn = "Amar Dokan",
                insideDhakaDeliveryCharge = 60.0,
                outsideDhakaDeliveryCharge = 120.0,
                adminPin = "1234",
                adminEmail = "admin@amardokan.com.bd",
                emailNotificationsEnabled = true,
                soundAlertsEnabled = true
            )
            db.settingsDao().insertOrUpdate(settings)

            // Seed Initial Demo Order for Admin Dashboard immediately on first launch
            val initialOrder = OrderEntity(
                orderNumber = "BD-2026-1001",
                customerName = "তানভীর আহমেদ",
                phone = "01712345678",
                deliveryAddress = "বাড়ি নং ১২, রোড ৫, ধানমন্ডি",
                district = "ঢাকা",
                area = "ধানমন্ডি",
                orderNotes = "সন্ধ্যার পর ডেলিভারি দিলে ভালো হয়।",
                subtotal = 3950.0,
                deliveryCharge = 60.0,
                discount = 0.0,
                totalAmount = 4010.0,
                paymentMethod = "ক্যাশ অন ডেলিভারি (COD)",
                paymentStatus = "UNPAID",
                orderStatus = "CONFIRMED",
                isAcknowledged = true,
                createdAt = System.currentTimeMillis() - 7200000L
            )
            val orderId = db.orderDao().insertOrder(initialOrder)
            db.orderDao().insertOrderItems(
                listOf(
                    OrderItemEntity(
                        orderId = orderId,
                        productId = 1,
                        productNameBn = "ঐতিহ্যবাহী লাল-সোনালী ঢাকাই জামদানি শাড়ি",
                        productNameEn = "Authentic Red & Gold Dhakai Jamdani Saree",
                        sku = "JAM-001",
                        purchaseCost = 3200.0,
                        sellingPrice = 3950.0,
                        quantity = 1,
                        lineTotal = 3950.0
                    )
                )
            )

            // Seed Initial Inventory Movement
            db.inventoryDao().insertMovement(
                InventoryMovementEntity(
                    productId = 1,
                    productName = "ঐতিহ্যবাহী লাল-সোনালী ঢাকাই জামদানি শাড়ি",
                    changeQuantity = -1,
                    previousStock = 13,
                    newStock = 12,
                    reason = "কাস্টমার অর্ডার #BD-2026-1001",
                    adminName = "সিস্টেম স্বয়ংক্রিয়",
                    timestamp = System.currentTimeMillis() - 7200000L
                )
            )
        }
    }
}
