package com.test.excel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.test.excel.model.SampleData
import com.test.excel.ui.theme.AndroidExcelTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel by viewModels()
            val dialogEffect by viewModel.mainDialogEffect.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val intentUtil = IntentUtil(context)

            AndroidExcelTheme {
                MainScreen(
                    list = SampleData.countriesAndCapitals,
                    onSaveExcel = viewModel::saveExcel,
                    onShareExcel = viewModel::shareExcel,
                    onReadExcel = viewModel::readExcel
                )

                DialogContent(
                    effect = dialogEffect,
                    onDismissRequest = viewModel::dismissDialog
                )

                LaunchedEffect(Unit) {
                    viewModel.mainUiEffect.collect {
                        when (it) {
                            is MainUiEffect.ShareExcel -> {
                                intentUtil.shareExcel(it.file)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogContent(
    effect: MainDialogEffect,
    onDismissRequest: () -> Unit
) {
    when (effect) {
        MainDialogEffect.Idle -> {}
        is MainDialogEffect.CountryDialog -> {
            CountryDialog(
                list = effect.list,
                onDismissRequest = onDismissRequest
            )
        }
    }
}