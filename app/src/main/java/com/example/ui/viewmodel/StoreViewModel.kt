package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.BusinessSettingsEntity
import com.example.data.model.CartItem
import com.example.data.model.CategoryEntity
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.repository.StoreRepository
import com.example.ui.localization.AppLanguage
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

enum class SortOption {
    NEWEST,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    POPULAR
}

enum class DeliveryZone {
    INSIDE_DHAKA,
    OUTSIDE_DHAKA
}

sealed interface CheckoutState {
    data object Idle : CheckoutState
    data object Submitting : CheckoutState
    data class Success(val order: OrderEntity) : CheckoutState
    data class Error(val message: String) : CheckoutState
}

class StoreViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _language = MutableStateFlow(AppLanguage.BANGLA)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("সব পণ্য")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _onlyInStock = MutableStateFlow(false)
    val onlyInStock: StateFlow<Boolean> = _onlyInStock.asStateFlow()

    private val _cart = MutableStateFlow<Map<Long, CartItem>>(emptyMap())
    val cart: StateFlow<Map<Long, CartItem>> = _cart.asStateFlow()

    private val _deliveryZone = MutableStateFlow(DeliveryZone.INSIDE_DHAKA)
    val deliveryZone: StateFlow<DeliveryZone> = _deliveryZone.asStateFlow()

    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val settings: StateFlow<BusinessSettingsEntity> = repository.settings
        .combine(MutableStateFlow(BusinessSettingsEntity())) { s, default -> s ?: default }
        .stateIn(viewModelScope, SharingStarted.Eagerly, BusinessSettingsEntity())

    val categories: StateFlow<List<CategoryEntity>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredProducts: StateFlow<List<ProductEntity>> = repository.featuredProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered & Sorted products stream
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        repository.activeProducts,
        _searchQuery,
        _selectedCategory,
        _sortOption,
        _onlyInStock
    ) { products, query, category, sort, inStockOnly ->
        var list = products

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.titleBn.lowercase().contains(q) ||
                    it.titleEn.lowercase().contains(q) ||
                    it.sku.lowercase().contains(q) ||
                    it.categoryBn.lowercase().contains(q) ||
                    it.categoryEn.lowercase().contains(q)
            }
        }

        if (category != "সব পণ্য" && category != "All") {
            list = list.filter { it.categoryBn == category || it.categoryEn == category }
        }

        if (inStockOnly) {
            list = list.filter { it.stockQuantity > 0 }
        }

        when (sort) {
            SortOption.NEWEST -> list.sortedByDescending { it.createdAt }
            SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.effectivePrice }
            SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.effectivePrice }
            SortOption.POPULAR -> list.sortedByDescending { it.isFeatured }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItemCount: StateFlow<Int> = _cart
        .combine(MutableStateFlow(0)) { cartMap, _ -> cartMap.values.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartSubtotal: StateFlow<Double> = _cart
        .combine(MutableStateFlow(0.0)) { cartMap, _ -> cartMap.values.sumOf { it.lineTotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentDeliveryFee: StateFlow<Double> = combine(_deliveryZone, settings) { zone, currentSettings ->
        if (zone == DeliveryZone.INSIDE_DHAKA) {
            currentSettings.insideDhakaDeliveryCharge
        } else {
            currentSettings.outsideDhakaDeliveryCharge
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60.0)

    val cartTotal: StateFlow<Double> = combine(cartSubtotal, currentDeliveryFee) { sub, fee ->
        sub + fee
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60.0)

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.BANGLA) AppLanguage.ENGLISH else AppLanguage.BANGLA
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryName: String) {
        _selectedCategory.value = categoryName
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun toggleInStockFilter() {
        _onlyInStock.value = !_onlyInStock.value
    }

    fun viewProductDetails(product: ProductEntity) {
        _selectedProduct.value = product
    }

    fun dismissProductDetails() {
        _selectedProduct.value = null
    }

    fun setDeliveryZone(zone: DeliveryZone) {
        _deliveryZone.value = zone
    }

    fun addToCart(product: ProductEntity, quantity: Int = 1) {
        if (product.stockQuantity <= 0) {
            viewModelScope.launch {
                _toastEvent.emit("এই পণ্যটি বর্তমানে স্টকে নেই!")
            }
            return
        }

        val currentMap = _cart.value.toMutableMap()
        val existing = currentMap[product.id]
        val currentQty = existing?.quantity ?: 0
        val targetQty = currentQty + quantity

        if (targetQty > product.stockQuantity) {
            viewModelScope.launch {
                _toastEvent.emit("মজুদ স্টকের চেয়ে বেশি অর্ডার করা সম্ভব নয় (সর্বোচ্চ: ${product.stockQuantity}টি)")
            }
            return
        }

        currentMap[product.id] = CartItem(product = product, quantity = targetQty)
        _cart.value = currentMap

        viewModelScope.launch {
            _toastEvent.emit("'${product.titleBn}' কার্টে যোগ হয়েছে")
        }
    }

    fun updateCartQuantity(productId: Long, newQuantity: Int) {
        val currentMap = _cart.value.toMutableMap()
        val item = currentMap[productId] ?: return

        if (newQuantity <= 0) {
            currentMap.remove(productId)
        } else if (newQuantity <= item.product.stockQuantity) {
            currentMap[productId] = item.copy(quantity = newQuantity)
        } else {
            viewModelScope.launch {
                _toastEvent.emit("স্টকে শুধুমাত্র ${item.product.stockQuantity}টি অবশিষ্ট আছে")
            }
            return
        }
        _cart.value = currentMap
    }

    fun removeFromCart(productId: Long) {
        val currentMap = _cart.value.toMutableMap()
        currentMap.remove(productId)
        _cart.value = currentMap
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    fun submitOrder(
        customerName: String,
        phone: String,
        deliveryAddress: String,
        district: String,
        area: String,
        orderNotes: String
    ) {
        if (_cart.value.isEmpty()) {
            _checkoutState.value = CheckoutState.Error("আপনার কার্ট খালি!")
            return
        }

        if (customerName.isBlank()) {
            _checkoutState.value = CheckoutState.Error("দয়া করে আপনার পূর্ণ নাম লিখুন")
            return
        }

        val cleanedPhone = phone.trim().replace(" ", "").replace("-", "")
        if (cleanedPhone.length < 11 || !cleanedPhone.startsWith("01")) {
            _checkoutState.value = CheckoutState.Error("সঠিক বাংলাদেশি মোবাইল নম্বর দিন (যেমন: 017XXXXXXXX)")
            return
        }

        if (deliveryAddress.isBlank()) {
            _checkoutState.value = CheckoutState.Error("সম্পূর্ণ ডেলিভারি ঠিকানা লিখুন")
            return
        }

        if (district.isBlank()) {
            _checkoutState.value = CheckoutState.Error("জেলা নির্বাচন করুন বা লিখুন")
            return
        }

        viewModelScope.launch {
            _checkoutState.value = CheckoutState.Submitting
            val cartList = _cart.value.values.toList()
            val fee = currentDeliveryFee.value

            val result = repository.placeOrder(
                customerName = customerName,
                phone = cleanedPhone,
                deliveryAddress = deliveryAddress,
                district = district,
                area = if (area.isBlank()) district else area,
                orderNotes = orderNotes,
                cartItems = cartList,
                deliveryCharge = fee
            )

            result.fold(
                onSuccess = { order ->
                    clearCart()
                    _checkoutState.value = CheckoutState.Success(order)
                },
                onFailure = { error ->
                    _checkoutState.value = CheckoutState.Error(error.message ?: "অর্ডার সম্পন্ন করতে সমস্যা হয়েছে।")
                }
            )
        }
    }

    fun resetCheckoutState() {
        _checkoutState.value = CheckoutState.Idle
    }
}

class StoreViewModelFactory(private val repository: StoreRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StoreViewModel(repository) as T
    }
}
