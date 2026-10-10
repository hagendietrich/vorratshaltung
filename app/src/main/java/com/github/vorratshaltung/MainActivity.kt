package com.github.vorratshaltung

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.github.vorratshaltung.data.local.VorratshaltungDatabase
import com.github.vorratshaltung.data.repository.InventoryRepository
import com.github.vorratshaltung.ui.main.MainNavigation
import com.github.vorratshaltung.ui.theme.VorratshaltungTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = VorratshaltungDatabase.getDatabase(applicationContext)
        val repository = InventoryRepository(
            storageLocationDao = db.storageLocationDao(),
            productDao = db.productDao(),
            inventoryItemDao = db.inventoryItemDao()
        )

        setContent {
            VorratshaltungTheme {
                MainNavigation(repository = repository)
            }
        }
    }
}
