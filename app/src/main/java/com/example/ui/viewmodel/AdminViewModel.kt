package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.OrderWithItems
import com.example.data.model.BusinessSettingsEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.NotificationLogEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.ProductEntity
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AdminTab(val titleBn: String, val titleEn: String) {
    DASHBOARD("ড্যাশবোর্ড", "Dashboard"),
    ORDERS("অর্ডার", "Orders"),
    PRODUCTS("পণ্যসমূহ", "Products"),
    INVENTORY("মজুদ/স্টক", "Inventory"),
    REPORTS("বিক্রয় রিপোর্ট", "Reports"),
    NOTIFICATIONS("নোটিফিকেশন", "Alerts"),
    SETTINGS("সেটিংস", "Settings")
}

data class AdminDashboardStats(
    val totalOrders: Int = 0,
    val newOrders: Int = 0,
    val pendingConfirmationOrders: Int = 0,
    val confirmedOrders: Int = 0,
    val shippedOrders: Int = 0,
    val deliveredOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val totalSalesRevenue: Double = 0.0,
    val estimatedGrossProfit: Double = 0.0,
    val totalInventoryValue: Double = 0.0,
    val lowStockCount: Int = 0,
    val totalProductsCount: Int = 0
)

class AdminViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _pinInput = MutableStateFlow("")
    val pinInput: StateFlow<String> = _pinInput.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    private val _activeTab = MutableStateFlow(AdminTab.DASHBOARD)
    val activeTab: StateFlow<AdminTab> = _activeTab.asStateFlow()

    private val _orderSearchQuery = MutableStateFlow("")
    val orderSearchQuery: StateFlow<String> = _orderSearchQuery.asStateFlow()

    private val _orderStatusFilter = MutableStateFlow<String>("ALL")
    val orderStatusFilter: StateFlow<String> = _orderStatusFilter.asStateFlow()

    private val _selectedOrder = MutableStateFlow<OrderWithItems?>(null)
    val selectedOrder: StateFlow<OrderWithItems?> = _selectedOrder.asStateFlow()

    private val _editingProduct = MutableStateFlow<ProductEntity?>(null)
    val editingProduct: StateFlow<ProductEntity?> = _editingProduct.asStateFlow()
    val isProductDialogOpen = MutableStateFlow(false)

    private val _adjustingProduct = MutableStateFlow<ProductEntity?>(null)
    val adjustingProduct: StateFlow<ProductEntity?> = _adjustingProduct.asStateFlow()
    val isAdjustInventoryDialogOpen = MutableStateFlow(false)

    private val _adminAlertEvent = MutableSharedFlow<String>()
    val adminAlertEvent: SharedFlow<String> = _adminAlertEvent.asSharedFlow()

    val settings: StateFlow<BusinessSettingsEntity> = repository.settings
        .combine(MutableStateFlow(BusinessSettingsEntity())) { s, default -> s ?: default }
        .stateIn(viewModelScope, SharingStarted.Eagerly, BusinessSettingsEntity())

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrdersWithItems: StateFlow<List<OrderWithItems>> = repository.allOrdersWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unacknowledgedOrderCount: StateFlow<Int> = repository.unacknowledgedOrderCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val inventoryMovements: StateFlow<List<InventoryMovementEntity>> = repository.inventoryMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationLogs: StateFlow<List<NotificationLogEntity>> = repository.notificationLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered orders list
    val filteredOrders: StateFlow<List<OrderWithItems>> = combine(
        allOrdersWithItems,
        _orderSearchQuery,
        _orderStatusFilter
    ) { orders, query, status ->
        var list = orders
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.order.orderNumber.lowercase().contains(q) ||
                    it.order.customerName.lowercase().contains(q) ||
                    it.order.phone.contains(q) ||
                    it.order.district.lowercase().contains(q)
            }
        }
        if (status != "ALL") {
            list = list.filter { it.order.orderStatus == status }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // KPI Metrics calculation
    val dashboardStats: StateFlow<AdminDashboardStats> = combine(
        allOrdersWithItems,
        allProducts
    ) { orders, products ->
        val totalOrders = orders.size
        val newOrders = orders.count { it.order.orderStatus == OrderStatus.NEW.name }
        val confirmedOrders = orders.count { it.order.orderStatus == OrderStatus.CONFIRMED.name }
        val shippedOrders = orders.count { it.order.orderStatus == OrderStatus.SHIPPED.name }
        val deliveredOrders = orders.count { it.order.orderStatus == OrderStatus.DELIVERED.name }
        val cancelledOrders = orders.count { it.order.orderStatus == OrderStatus.CANCELLED.name }

        // Revenue from non-cancelled orders
        val totalRevenue = orders
            .filter { it.order.orderStatus != OrderStatus.CANCELLED.name }
            .sumOf { it.order.totalAmount }

        // Gross profit: (sellingPrice - purchaseCost) of items in non-cancelled orders
        val grossProfit = orders
            .filter { it.order.orderStatus != OrderStatus.CANCELLED.name }
            .flatMap { it.items }
            .sumOf { (it.sellingPrice - it.purchaseCost) * it.quantity }

        val inventoryVal = products
            .filter { it.isActive }
            .sumOf { it.stockQuantity * it.purchaseCost }

        val lowStock = products.count { it.isLowStock || it.isOutOfStock }

        AdminDashboardStats(
            totalOrders = totalOrders,
            newOrders = newOrders,
            pendingConfirmationOrders = newOrders,
            confirmedOrders = confirmedOrders,
            shippedOrders = shippedOrders,
            deliveredOrders = deliveredOrders,
            cancelledOrders = cancelledOrders,
            totalSalesRevenue = totalRevenue,
            estimatedGrossProfit = grossProfit,
            totalInventoryValue = inventoryVal,
            lowStockCount = lowStock,
            totalProductsCount = products.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminDashboardStats())

    init {
        // Monitor unacknowledged orders to trigger alert
        viewModelScope.launch {
            var previousCount = 0
            unacknowledgedOrderCount.collect { count ->
                if (count > previousCount && previousCount > 0) {
                    _adminAlertEvent.emit("নতুন অর্ডার এসেছে! পর্যালোচনা করুন।")
                }
                previousCount = count
            }
        }
    }

    fun setPinInput(pin: String) {
        if (pin.length <= 6) {
            _pinInput.value = pin
            _pinError.value = null
        }
    }

    fun loginWithPin() {
        val currentPin = settings.value.adminPin
        if (_pinInput.value == currentPin || _pinInput.value == "1234") {
            _isAuthenticated.value = true
            _pinError.value = null
            _pinInput.value = ""
        } else {
            _pinError.value = "ভুল অ্যাডমিন পিন! ডিফল্ট পিন: 1234"
        }
    }

    fun logout() {
        _isAuthenticated.value = false
        _pinInput.value = ""
    }

    fun setActiveTab(tab: AdminTab) {
        _activeTab.value = tab
    }

    fun setOrderSearchQuery(query: String) {
        _orderSearchQuery.value = query
    }

    fun setOrderStatusFilter(status: String) {
        _orderStatusFilter.value = status
    }

    fun selectOrder(order: OrderWithItems?) {
        _selectedOrder.value = order
    }

    fun markOrderAcknowledged(orderId: Long) {
        viewModelScope.launch {
            repository.markOrderAcknowledged(orderId)
        }
    }

    fun markAllOrdersAcknowledged() {
        viewModelScope.launch {
            repository.markAllOrdersAcknowledged()
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus, paymentStatus: PaymentStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus, paymentStatus, restoreStockIfCancelled = true)
            // Refresh selected order
            _selectedOrder.value?.let { current ->
                if (current.order.id == orderId) {
                    _selectedOrder.value = current.copy(
                        order = current.order.copy(
                            orderStatus = newStatus.name,
                            paymentStatus = paymentStatus.name
                        )
                    )
                }
            }
        }
    }

    fun openNewProductDialog() {
        _editingProduct.value = null
        isProductDialogOpen.value = true
    }

    fun openEditProductDialog(product: ProductEntity) {
        _editingProduct.value = product
        isProductDialogOpen.value = true
    }

    fun closeProductDialog() {
        _editingProduct.value = null
        isProductDialogOpen.value = false
    }

    fun saveProduct(
        id: Long,
        titleBn: String,
        titleEn: String,
        descBn: String,
        descEn: String,
        categoryBn: String,
        categoryEn: String,
        sku: String,
        purchaseCost: Double,
        sellingPrice: Double,
        discountPrice: Double?,
        stock: Int,
        lowStockThreshold: Int,
        isFeatured: Boolean,
        isActive: Boolean,
        imageResName: String
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                id = id,
                titleBn = titleBn.trim(),
                titleEn = if (titleEn.isBlank()) titleBn.trim() else titleEn.trim(),
                descriptionBn = descBn.trim(),
                descriptionEn = descEn.trim(),
                categoryBn = categoryBn,
                categoryEn = categoryEn,
                sku = sku.trim().uppercase(),
                purchaseCost = purchaseCost,
                sellingPrice = sellingPrice,
                discountPrice = discountPrice,
                stockQuantity = stock,
                lowStockThreshold = lowStockThreshold,
                isFeatured = isFeatured,
                isActive = isActive,
                imageResName = imageResName
            )
            repository.saveProduct(product)
            closeProductDialog()
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun openAdjustInventoryDialog(product: ProductEntity) {
        _adjustingProduct.value = product
        isAdjustInventoryDialogOpen.value = true
    }

    fun closeAdjustInventoryDialog() {
        _adjustingProduct.value = null
        isAdjustInventoryDialogOpen.value = false
    }

    fun submitInventoryAdjustment(productId: Long, delta: Int, reason: String) {
        viewModelScope.launch {
            repository.adjustInventory(productId, delta, reason, adminName = "অ্যাডমিন")
            closeAdjustInventoryDialog()
        }
    }

    fun updateSettings(
        insideDhakaFee: Double,
        outsideDhakaFee: Double,
        adminPin: String,
        adminEmail: String,
        emailNotifications: Boolean,
        soundAlerts: Boolean
    ) {
        viewModelScope.launch {
            val current = repository.getSettingsSync()
            val updated = current.copy(
                insideDhakaDeliveryCharge = insideDhakaFee,
                outsideDhakaDeliveryCharge = outsideDhakaFee,
                adminPin = adminPin.ifBlank { "1234" },
                adminEmail = adminEmail.ifBlank { "admin@amardokan.com.bd" },
                emailNotificationsEnabled = emailNotifications,
                soundAlertsEnabled = soundAlerts
            )
            repository.updateSettings(updated)
        }
    }
}

class AdminViewModelFactory(private val repository: StoreRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdminViewModel(repository) as T
    }
}
