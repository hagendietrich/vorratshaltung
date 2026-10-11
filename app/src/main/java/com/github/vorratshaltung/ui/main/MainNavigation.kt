package com.github.vorratshaltung.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.vorratshaltung.data.repository.InventoryRepository
import com.github.vorratshaltung.ui.compartment.CompartmentDetailScreen
import com.github.vorratshaltung.ui.compartment.CompartmentDetailViewModel
import com.github.vorratshaltung.ui.compartment.CompartmentDetailViewModelFactory
import com.github.vorratshaltung.ui.inventory.AllInventoryScreen
import com.github.vorratshaltung.ui.inventory.AllInventoryViewModel
import com.github.vorratshaltung.ui.inventory.AllInventoryViewModelFactory
import com.github.vorratshaltung.ui.products.ProductCatalogScreen
import com.github.vorratshaltung.ui.products.ProductCatalogViewModel
import com.github.vorratshaltung.ui.products.ProductCatalogViewModelFactory
import com.github.vorratshaltung.ui.shelf.ShelfScreen
import com.github.vorratshaltung.ui.shelf.ShelfViewModel
import com.github.vorratshaltung.ui.shelf.ShelfViewModelFactory
import com.github.vorratshaltung.ui.shopping.ShoppingListScreen
import com.github.vorratshaltung.ui.shopping.ShoppingListViewModel
import com.github.vorratshaltung.ui.shopping.ShoppingListViewModelFactory

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Shelf : Screen("shelf", "Regale", Icons.Default.Inventory)
    object AllItems : Screen("all_items", "Bestand", Icons.Default.Storage)
    object Shopping : Screen("shopping", "Einkaufsliste", Icons.Default.ShoppingCart)
    object Products : Screen("products", "Produkte", Icons.Default.List)
}

@Composable
fun MainNavigation(
    repository: InventoryRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Shelf, Screen.AllItems, Screen.Shopping, Screen.Products)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            if (currentRoute == Screen.Shelf.route || currentRoute == Screen.AllItems.route || currentRoute == Screen.Shopping.route || currentRoute == Screen.Products.route) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Shelf.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Shelf.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Shelf.route) {
                val shelfViewModel: ShelfViewModel = viewModel(
                    factory = ShelfViewModelFactory(repository)
                )
                ShelfScreen(
                    viewModel = shelfViewModel,
                    onCompartmentClick = { compartmentId ->
                        navController.navigate("compartment/$compartmentId")
                    }
                )
            }

            composable(Screen.AllItems.route) {
                val allInventoryViewModel: AllInventoryViewModel = viewModel(
                    factory = AllInventoryViewModelFactory(repository)
                )
                AllInventoryScreen(viewModel = allInventoryViewModel)
            }

            composable(Screen.Shopping.route) {
                val shoppingViewModel: ShoppingListViewModel = viewModel(
                    factory = ShoppingListViewModelFactory(repository)
                )
                ShoppingListScreen(viewModel = shoppingViewModel)
            }

            composable(Screen.Products.route) {
                val catalogViewModel: ProductCatalogViewModel = viewModel(
                    factory = ProductCatalogViewModelFactory(repository)
                )
                ProductCatalogScreen(viewModel = catalogViewModel)
            }

            composable(
                route = "compartment/{compartmentId}",
                arguments = listOf(navArgument("compartmentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val compartmentId = backStackEntry.arguments?.getLong("compartmentId") ?: 0L
                val detailViewModel: CompartmentDetailViewModel = viewModel(
                    factory = CompartmentDetailViewModelFactory(compartmentId, repository)
                )
                CompartmentDetailScreen(
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
