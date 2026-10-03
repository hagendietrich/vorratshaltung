package com.github.vorratshaltung

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.github.vorratshaltung.data.local.VorratshaltungDatabase
import com.github.vorratshaltung.data.repository.InventoryRepository
import com.github.vorratshaltung.ui.shelf.ShelfScreen
import com.github.vorratshaltung.ui.shelf.ShelfViewModel
import com.github.vorratshaltung.ui.shelf.ShelfViewModelFactory
import com.github.vorratshaltung.ui.theme.VorratshaltungTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ShelfViewModel by viewModels {
        val db = VorratshaltungDatabase.getDatabase(applicationContext)
        val repo = InventoryRepository(
            storageLocationDao = db.storageLocationDao(),
            productDao = db.productDao(),
            inventoryItemDao = db.inventoryItemDao()
        )
        ShelfViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VorratshaltungTheme {
                ShelfScreen(
                    viewModel = viewModel,
                    onCompartmentClick = { compartmentId ->
                        Toast.makeText(this, "Fach ID $compartmentId geklickt (Phase 3)", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
