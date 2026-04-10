package com.goldtip.vivoledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goldtip.vivoledger.ui.LedgerApp
import com.goldtip.vivoledger.ui.LedgerViewModel
import com.goldtip.vivoledger.ui.LedgerViewModelFactory
import com.goldtip.vivoledger.ui.theme.LocalAccountingToolTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LocalAccountingToolTheme {
                val viewModel: LedgerViewModel = viewModel(
                    factory = LedgerViewModelFactory(applicationContext)
                )
                LedgerApp(viewModel = viewModel)
            }
        }
    }
}
