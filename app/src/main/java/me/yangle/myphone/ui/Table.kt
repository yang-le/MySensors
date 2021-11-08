package me.yangle.myphone.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Table(data: List<List<String>>, header: List<String>? = null) {
    Column {
        header?.let {
            Row {
                header.map {
                    Text(
                        it,
                        Modifier
                            .padding(8.dp)
                            .weight(1f)
                    )
                }
            }
            Divider()
        }
        LazyColumn {
            items(data) { row ->
                Row {
                    row.map {
                        Text(
                            it,
                            Modifier
                                .padding(8.dp)
                                .weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TablePreview() {
    val tableData = (1..100).map {
        listOf("$it", "Item $it")
    }
    Table(tableData, listOf("index", "item"))
}
