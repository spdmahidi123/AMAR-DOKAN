package com.example

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.AppDatabase
import com.example.data.model.OrderEntity
import com.example.data.repository.StoreRepository
import com.example.ui.components.StoreHeader
import com.example.ui.localization.AppLanguage
import com.example.ui.screens.admin.AdminContainerScreen
import com.example.ui.screens.admin.AdminLoginScreen
import com.example.ui.screens.store.CartSheet
import com.example.ui.screens.store.CheckoutScreen
import com.example.ui.screens.store.HomeScreen
import com.example.ui.screens.store.OrderSuccessScreen
import com.example.ui.screens.store.ProductDetailSheet
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.AdminViewModelFactory
import com.example.ui.viewmodel.CheckoutState
import com.example.ui.viewmodel.StoreViewModel
import com.example.ui.viewmodel.StoreViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AppDestination {
    STORE_HOME,
    CHECKOUT,
    ORDER_SUCCESS,
    ADMIN_LOGIN,
    ADMIN_PANEL
}

class MainActivity : ComponentActivity() {

    private var toneGenerator: ToneGenerator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        } catch (_: Exception) {
            toneGenerator = null
        }

        val database = AppDatabase.getDatabase(this, lifecycleScope)
        val repository = StoreRepository(database)

        setContent {
            MyApplicationTheme {
                MainApp(
                    repository = repository,
                    onPlayAlertTone = { playOrderAlertTone() }
                )
            }
        }
    }

    private fun playOrderAlertTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 250)

            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(200)
            }
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGenerator?.release()
        toneGenerator = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    repository: StoreRepository,
    onPlayAlertTone: () -> Unit
) {
    val storeViewModel: StoreViewModel = viewModel(factory = StoreViewModelFactory(repository))
    val adminViewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(repository))

    val language by storeViewModel.language.collectAsState()
    val settings by storeViewModel.settings.collectAsState()
    val cartItemCount by storeViewModel.cartItemCount.collectAsState()
    val selectedProduct by storeViewModel.selectedProduct.collectAsState()
    val checkoutState by storeViewModel.checkoutState.collectAsState()
    val isAdminAuthenticated by adminViewModel.isAuthenticated.collectAsState()

    val intentUri = (androidx.compose.ui.platform.LocalContext.current as? ComponentActivity)?.intent?.data
    val initialDestination = remember(intentUri) {
        if (intentUri != null && (intentUri.host == "admin" || intentUri.path?.contains("/admin") == true)) {
            AppDestination.ADMIN_LOGIN
        } else {
            AppDestination.STORE_HOME
        }
    }
    var currentDestination by remember { mutableStateOf(initialDestination) }
    var placedOrder by remember { mutableStateOf<OrderEntity?>(null) }
    var isCartOpen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cartSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // SnackBar listener for store events
    LaunchedEffect(Unit) {
        storeViewModel.toastEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Audio / Vibrate alert on new order event
    LaunchedEffect(Unit) {
        adminViewModel.adminAlertEvent.collectLatest { msg ->
            onPlayAlertTone()
            snackbarHostState.showSnackbar(msg)
        }
    }

    // React to successful checkout
    LaunchedEffect(checkoutState) {
        if (checkoutState is CheckoutState.Success) {
            placedOrder = (checkoutState as CheckoutState.Success).order
            currentDestination = AppDestination.ORDER_SUCCESS
            storeViewModel.resetCheckoutState()
        }
    }

    // Root Navigation Routing
    when (currentDestination) {
        AppDestination.STORE_HOME -> {
            Scaffold(
                topBar = {
                    StoreHeader(
                        storeName = if (language == AppLanguage.BANGLA) settings.storeNameBn else settings.storeNameEn,
                        language = language,
                        cartItemCount = cartItemCount,
                        onLanguageToggle = { storeViewModel.toggleLanguage() },
                        onCartClick = { isCartOpen = true },
                        onAdminSecretAccess = {
                            currentDestination = if (isAdminAuthenticated) {
                                AppDestination.ADMIN_PANEL
                            } else {
                                AppDestination.ADMIN_LOGIN
                            }
                        }
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    HomeScreen(
                        viewModel = storeViewModel,
                        onNavigateToCheckout = { currentDestination = AppDestination.CHECKOUT },
                        onAdminSecretAccess = {
                            currentDestination = if (isAdminAuthenticated) {
                                AppDestination.ADMIN_PANEL
                            } else {
                                AppDestination.ADMIN_LOGIN
                            }
                        }
                    )
                }

                // Product Detail Sheet
                if (selectedProduct != null) {
                    ProductDetailSheet(
                        product = selectedProduct!!,
                        language = language,
                        sheetState = detailSheetState,
                        onDismiss = { storeViewModel.dismissProductDetails() },
                        onAddToCart = { qty -> storeViewModel.addToCart(selectedProduct!!, qty) },
                        onBuyNow = { qty ->
                            storeViewModel.addToCart(selectedProduct!!, qty)
                            currentDestination = AppDestination.CHECKOUT
                        }
                    )
                }

                // Shopping Cart Sheet
                if (isCartOpen) {
                    CartSheet(
                        viewModel = storeViewModel,
                        sheetState = cartSheetState,
                        onDismiss = { isCartOpen = false },
                        onProceedToCheckout = {
                            isCartOpen = false
                            currentDestination = AppDestination.CHECKOUT
                        }
                    )
                }
            }
        }

        AppDestination.CHECKOUT -> {
            CheckoutScreen(
                viewModel = storeViewModel,
                onBack = { currentDestination = AppDestination.STORE_HOME }
            )
        }

        AppDestination.ORDER_SUCCESS -> {
            if (placedOrder != null) {
                OrderSuccessScreen(
                    order = placedOrder!!,
                    language = language,
                    onBackToStore = {
                        placedOrder = null
                        currentDestination = AppDestination.STORE_HOME
                    }
                )
            } else {
                currentDestination = AppDestination.STORE_HOME
            }
        }

        AppDestination.ADMIN_LOGIN -> {
            if (isAdminAuthenticated) {
                currentDestination = AppDestination.ADMIN_PANEL
            } else {
                AdminLoginScreen(
                    viewModel = adminViewModel,
                    onBackToStore = { currentDestination = AppDestination.STORE_HOME }
                )
            }
        }

        AppDestination.ADMIN_PANEL -> {
            if (!isAdminAuthenticated) {
                currentDestination = AppDestination.ADMIN_LOGIN
            } else {
                AdminContainerScreen(
                    viewModel = adminViewModel,
                    onBackToStore = { currentDestination = AppDestination.STORE_HOME }
                )
            }
        }
    }
}
