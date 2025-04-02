package com.test.excel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class IntentUtil(
    private val context: Context
) {
    fun shareExcel(file: File) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(intent, "엑셀 파일 공유하기"))
            }.onFailure {
                withContext(Dispatchers.Main) {
                    Log.e("IntentUtil", "error: ${it.message}")
                    Toast.makeText(context, "엑셀 공유 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}