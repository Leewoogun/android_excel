package com.test.excel

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.excel.model.SampleData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
): ViewModel() {
    private val list = SampleData.countriesAndCapitals

    private val _excel = MutableStateFlow<Workbook?>(null)
    val excel = _excel.asStateFlow()

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

    }

    fun readExcel() {

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
}