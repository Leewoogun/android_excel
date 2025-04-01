package com.test.excel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.test.excel.model.SampleData
import com.test.excel.ui.theme.AndroidExcelTheme
import com.test.excel.ui.theme.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel by viewModels()

            AndroidExcelTheme {
                MainScreen(
                    list = SampleData.countriesAndCapitals,
                    onSaveExcel = viewModel::saveExcel,
                    onShareExcel = viewModel::shareExcel,
                    onReadExcel = viewModel::readExcel
                )
            }
        }
    }
}
