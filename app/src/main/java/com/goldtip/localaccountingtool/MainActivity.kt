package com.goldtip.localaccountingtool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goldtip.localaccountingtool.ui.LedgerApp
import com.goldtip.localaccountingtool.ui.LedgerViewModel
import com.goldtip.localaccountingtool.ui.LedgerViewModelFactory
import com.goldtip.localaccountingtool.ui.theme.LocalAccountingToolTheme

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
