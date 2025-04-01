package com.test.excel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun CountryDialog(
    list: List<Pair<String, String>>,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        LazyColumn(
            modifier = Modifier
                .background(Color.White)
        ) {
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