package com.test.excel.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.test.excel.model.SampleData

@Composable
fun MainScreen(
    list: List<Pair<String, String>>,
    onSaveExcel: () -> Unit,
    onShareExcel: () -> Unit,
    onReadExcel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            onClick = onSaveExcel
        ) {
            Text(
                text = "액셀 저장하기"
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            onClick = onShareExcel
        ) {
            Text(
                text = "액셀 공유하기"
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            onClick = onReadExcel
        ) {
            Text(
                text = "액셀 불러오기"
            )
        }

        LazyColumn {
            items(list) {
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                ) {
                    Text(
                        text = "나라: ${it.first} | 수도: ${it.second}"
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun MainScreenPreview() {
    AndroidExcelTheme {
        MainScreen(
            list = SampleData.countriesAndCapitals,
            onSaveExcel = {},
            onShareExcel = {},
            onReadExcel = {}
        )
    }
}