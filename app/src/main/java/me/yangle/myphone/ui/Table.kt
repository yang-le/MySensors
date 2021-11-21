package me.yangle.myphone.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Surface
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
    firstLineHeader: Boolean = true,
    onClick: ((row: List<String>) -> Unit)? = null
) {
    if (firstLineHeader)
        Table(data.subList(1, data.size), data[0], onClick)
    else
        Table(data, null, onClick)
}

@Composable
fun Table(
    data: List<List<String>>,
    header: List<String>?,
    onClick: ((row: List<String>) -> Unit)? = null
) = Table(mapOf(header to data), onClick)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Table(
    grouped: Map<List<String>?, List<List<String>>>,
    onClick: ((row: List<String>) -> Unit)? = null
) {
    LazyColumn {
        grouped.forEach { (header, data) ->
            header?.let {
                stickyHeader {
                    Surface {
                        Column {
                            Row(
                                modifier = onClick?.let {
                                    Modifier.clickable { it(header) }
                                } ?: Modifier
                            ) {
                                header.map {
                                    Text(
                                        text = it,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .weight(1f)
                                    )
                                }
                            }
                            Divider()
                        }
                    }
                }
            }
            items(data) { row ->
                Row(
                    modifier = onClick?.let {
                        Modifier.clickable { it(row) }
                    } ?: Modifier
                ) {
                    row.map {
                        Text(
                            text = it,
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(1f)
                        )
                    }
                }
            }
        }
    }
}
