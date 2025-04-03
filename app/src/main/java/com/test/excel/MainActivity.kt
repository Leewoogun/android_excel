package com.test.excel

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.test.excel.model.SampleData
import com.test.excel.ui.theme.AndroidExcelTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MainViewModel by viewModels()
            val dialogEffect by viewModel.mainDialogEffect.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val intentUtil = IntentUtil(context)

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // Android 9 이하
                val permissionArray = arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissionArray, 1)
                }
            }

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