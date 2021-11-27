package me.yangle.myphone

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext

@Composable
fun About() {
    val libs = Libs.Builder().withContext(LocalContext.current).build()
    LazyColumn {
        items(libs.libraries) {
            Card(
                Modifier
                    .fillParentMaxWidth()
                    .padding(8.dp),
                RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colors.secondaryVariant)
            ) {
                Column(Modifier.padding(8.dp)) {
                    Row(
                        Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            it.name,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.h6
                        )
                        it.artifactVersion?.let { Text(it, style = MaterialTheme.typography.body2) }
                    }
                    if (it.developers.isNotEmpty()) {
                        it.developers[0].name?.let {
                            Text(
                                it,
                                Modifier.padding(start = 8.dp, bottom = 8.dp),
                                style = MaterialTheme.typography.subtitle1
                            )
                        }
                    }
                    Divider()
                    Text(it.uniqueId, Modifier.padding(8.dp))
                    it.description?.let { Text(it, Modifier.padding(8.dp)) }
                    Divider()
                    if (it.licenses.isNotEmpty()) {
                        Text(
                            it.licenses.first().name,
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }
    }
}