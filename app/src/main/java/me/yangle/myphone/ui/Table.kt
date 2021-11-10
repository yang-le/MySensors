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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp

class DataProvider : PreviewParameterProvider<List<List<String>>> {
    private val tableData = (1..10).map {
        listOf("$it", "Item $it")
    }
    override val values = listOf(tableData).asSequence()
}

@Preview
@Composable
fun Table(
    @PreviewParameter(DataProvider::class) data: List<List<String>>,
    firstLineHeader: Boolean = true
) {
    if (firstLineHeader)
        Table(data.subList(1, data.size), data[0])
    else
        Table(data, null)
}

@Composable
fun Table(
    data: List<List<String>>,
    header: List<String>?
) {
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
