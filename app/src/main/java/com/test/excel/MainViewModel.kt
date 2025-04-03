package com.test.excel

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.excel.model.SampleData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
): ViewModel() {
    private val list = SampleData.countriesAndCapitals

    private val _excel = MutableStateFlow<Workbook?>(null)
    val excel = _excel.asStateFlow()

    private val _mainDialogEffect = MutableStateFlow<MainDialogEffect>(MainDialogEffect.Idle)
    val mainDialogEffect = _mainDialogEffect.asStateFlow()

    private val _mainUiEffect = MutableSharedFlow<MainUiEffect>()
    val mainUiEffect = _mainUiEffect.asSharedFlow()

    /**
     * 안드로이드 29이상 부터는 Scoped Storage 정책을 따름
     */
    fun saveExcel() {
        viewModelScope.launch {
            createExcel()
            val excel = _excel.value ?: return@launch

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Files.FileColumns.DISPLAY_NAME, "countries.xlsx")
                    put(MediaStore.Files.FileColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                }

                val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                uri?.let {
                    runCatching {
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            excel.write(outputStream)
                        }
                    }.onSuccess {
                        Toast.makeText(context, "저장 되었습니다.", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(context, "저장이 실패 하였습니다.", Toast.LENGTH_SHORT).show()
                        Log.e("MainViewModel", "error: ${it.message}")
                    }
                }
            } else {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "countries.xlsx")

                runCatching {
                    file.outputStream().use { outputStream ->
                        excel.write(outputStream)
                    }
                }.onSuccess {
                    Toast.makeText(context, "저장 되었습니다.", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Log.e("MainViewModel", "error: ${it.message}")
                    Toast.makeText(context, "저장이 실패 하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun shareExcel() {
        viewModelScope.launch {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "countries.xlsx")

            if (!file.exists()) {
                Toast.makeText(context, "파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            _mainUiEffect.emit(MainUiEffect.ShareExcel(file))
        }
    }

    fun readExcel() {
        viewModelScope.launch {
            val fileName = "countries.xlsx"
            val dataList = mutableListOf<Pair<String, String>>() // 읽어온 데이터를 저장할 리스트

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentUri = MediaStore.Files.getContentUri("external")

                val projection = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME
                )

                val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} = ?"
                val selectionArgs = arrayOf(fileName)

                runCatching {
                    context.contentResolver.query(contentUri, projection, selection, selectionArgs, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                            val fileId = cursor.getLong(idColumn)
                            val uri = ContentUris.withAppendedId(contentUri, fileId)

                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                dataList.addAll(parseExcel(inputStream))
                            }
                        }
                    }
                }.onFailure {
                    Toast.makeText(context, "파일 읽기 실패", Toast.LENGTH_SHORT).show()
                    Log.e("MainViewModel", "error: ${it.message}")
                }

            } else {
                runCatching {
                    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName)
                    if (file.exists()) {
                        file.inputStream().use { inputStream ->
                            dataList.addAll(parseExcel(inputStream))
                        }
                    }
                }.onFailure {
                    Toast.makeText(context, "파일 읽기 실패", Toast.LENGTH_SHORT).show()
                    Log.e("MainViewModel", "error: ${it.message}")
                }
            }

            if (dataList.isNotEmpty()) {
                _mainDialogEffect.update { MainDialogEffect.CountryDialog(dataList.toList()) }
            }
        }
    }

    private fun parseExcel(inputStream: InputStream): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        val workbook: Workbook = XSSFWorkbook(inputStream)
        val sheet: Sheet = workbook.getSheetAt(0) // 첫 번째 시트 사용

        for (row in sheet) {
            if (row.rowNum == 0) continue // 첫 번째 행(헤더)은 건너뜀

            val country = row.getCell(0)?.stringCellValue ?: ""
            val capital = row.getCell(1)?.stringCellValue ?: ""
            list.add(country to capital)
        }

        workbook.close()
        return list
    }

    private fun createExcel() {
        val workbook: Workbook = XSSFWorkbook() // XLSX 포맷 사용
        val sheet: Sheet = workbook.createSheet("countrySheet") // 시트 이름 추가

        // 🚀 첫 번째 행(0번 행)에 헤더 추가
        val headerRow: Row = sheet.createRow(0) // 첫 번째 행 생성
        headerRow.createCell(0).setCellValue("나라") // 첫 번째 열
        headerRow.createCell(1).setCellValue("수도") // 두 번째 열

        list.forEachIndexed { index, pair ->
            val row: Row = sheet.createRow(index + 1) // 1번 행부터 데이터 입력

            row.createCell(0).setCellValue(pair.first) // 나라 데이터
            row.createCell(1).setCellValue(pair.second) // 수도 데이터
        }

        _excel.update { workbook }
    }

    fun dismissDialog() {
        _mainDialogEffect.update { MainDialogEffect.Idle }
    }
}

@Stable
sealed interface MainDialogEffect {

    @Immutable
    data object Idle: MainDialogEffect

    @Immutable
    data class CountryDialog(
        val list: List<Pair<String, String>>
    ): MainDialogEffect
}

@Stable
sealed interface MainUiEffect {
    @Immutable
    data class ShareExcel(
        val file: File
    ): MainUiEffect
}