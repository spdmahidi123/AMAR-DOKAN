package com.example.ui.screens.store

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CategoryEntity
import com.example.data.model.ProductEntity
import com.example.ui.components.ProductCard
import com.example.ui.components.SectionHeader
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.GreenPrimary
import com.example.ui.viewmodel.SortOption
import com.example.ui.viewmodel.StoreViewModel

@Composable
fun HomeScreen(
    viewModel: StoreViewModel,
    onNavigateToCheckout: () -> Unit,
    onAdminSecretAccess: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val onlyInStock by viewModel.onlyInStock.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()
    val featuredProducts by viewModel.featuredProducts.collectAsState()

    var isSortMenuExpanded by remember { mutableStateOf(false) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(bottom = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .testTag("home_products_grid")
    ) {
        // 1. Hero Promo Banner
        item(span = { GridItemSpan(2) }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.store_hero_banner),
                        contentDescription = "Store Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay for high text contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xCC0D5C3A)
                                    ),
                                    startY = 30f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    ) {
                        Surface(
                            color = AmberSecondary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.BANGLA) "সেরা দেশীয় সম্ভার" else "100% Authentic BD",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = AppStrings.heroTitle(language),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = AppStrings.heroSubtitle(language),
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // 2. Search Bar
        item(span = { GridItemSpan(2) }) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { input ->
                    if (input.trim().equals("#admin", ignoreCase = true) || input.trim().equals("/admin", ignoreCase = true)) {
                        viewModel.setSearchQuery("")
                        onAdminSecretAccess?.invoke()
                    } else {
                        viewModel.setSearchQuery(input)
                    }
                },
                placeholder = {
                    Text(
                        text = AppStrings.searchPlaceholder(language),
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = GreenPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .testTag("search_input")
            )
        }

        // 3. Category Filter Chips (Horizontal Row)
        item(span = { GridItemSpan(2) }) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == "সব পণ্য" || selectedCategory == "All",
                        onClick = { viewModel.selectCategory("সব পণ্য") },
                        label = {
                            Text(
                                text = if (language == AppLanguage.BANGLA) "সব পণ্য" else "All",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                items(categories.filter { it.nameBn != "সব পণ্য" }) { category ->
                    val catName = if (language == AppLanguage.BANGLA) category.nameBn else category.nameEn
                    val isSelected = selectedCategory == category.nameBn || selectedCategory == category.nameEn
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(category.nameBn) },
                        label = { Text(text = catName, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // 4. Quick Filters & Sort Bar
        item(span = { GridItemSpan(2) }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                // In Stock Filter Chip
                FilterChip(
                    selected = onlyInStock,
                    onClick = { viewModel.toggleInStockFilter() },
                    label = {
                        Text(
                            text = if (language == AppLanguage.BANGLA) "শুধুমাত্র ইন-স্টক" else "In Stock Only",
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        if (onlyInStock) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberSecondary.copy(alpha = 0.2f),
                        selectedLabelColor = AmberSecondary
                    )
                )

                // Sort Dropdown button
                Box {
                    AssistChip(
                        onClick = { isSortMenuExpanded = true },
                        label = {
                            val sortLabel = when (sortOption) {
                                SortOption.NEWEST -> if (language == AppLanguage.BANGLA) "নতুন" else "Newest"
                                SortOption.PRICE_LOW_TO_HIGH -> if (language == AppLanguage.BANGLA) "কম দাম" else "Price: Low"
                                SortOption.PRICE_HIGH_TO_LOW -> if (language == AppLanguage.BANGLA) "বেশি দাম" else "Price: High"
                                SortOption.POPULAR -> if (language == AppLanguage.BANGLA) "জনপ্রিয়" else "Popular"
                            }
                            Text(text = "সর্ট: $sortLabel", fontSize = 12.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Sort",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = isSortMenuExpanded,
                        onDismissRequest = { isSortMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (language == AppLanguage.BANGLA) "নতুন পণ্য (Newest)" else "Newest Arrivals") },
                            onClick = {
                                viewModel.setSortOption(SortOption.NEWEST)
                                isSortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (language == AppLanguage.BANGLA) "মূল্য: কম থেকে বেশি" else "Price: Low to High") },
                            onClick = {
                                viewModel.setSortOption(SortOption.PRICE_LOW_TO_HIGH)
                                isSortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (language == AppLanguage.BANGLA) "মূল্য: বেশি থেকে কম" else "Price: High to Low") },
                            onClick = {
                                viewModel.setSortOption(SortOption.PRICE_HIGH_TO_LOW)
                                isSortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (language == AppLanguage.BANGLA) "জনপ্রিয় পণ্য" else "Popular Products") },
                            onClick = {
                                viewModel.setSortOption(SortOption.POPULAR)
                                isSortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Section header
        item(span = { GridItemSpan(2) }) {
            SectionHeader(
                title = if (selectedCategory == "সব পণ্য" || selectedCategory == "All") {
                    AppStrings.allProductsTitle(language)
                } else {
                    selectedCategory
                },
                subtitle = if (language == AppLanguage.BANGLA) {
                    "মোট ${AppStrings.formatBengaliDigits(products.size.toString())}টি পণ্য পাওয়া গেছে"
                } else {
                    "${products.size} products found"
                }
            )
        }

        // 5. Products Grid List
        if (products.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SentimentDissatisfied,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == AppLanguage.BANGLA) "কোনো পণ্য খুঁজে পাওয়া যায়নি" else "No products found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Text(
                        text = if (language == AppLanguage.BANGLA) "অন্য কিওয়ার্ড দিয়ে খুঁজুন বা ফিল্টার পরিবর্তন করুন" else "Try another search keyword or clear filters",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    language = language,
                    onProductClick = { viewModel.viewProductDetails(product) },
                    onAddToCart = { viewModel.addToCart(product) }
                )
            }
        }
    }
}
