package com.niks0n.mypetsinfo.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mypetsinfo.shared.generated.resources.Res
import mypetsinfo.shared.generated.resources.news_fact_cat_body
import mypetsinfo.shared.generated.resources.news_fact_cat_title
import mypetsinfo.shared.generated.resources.news_fact_dog_body
import mypetsinfo.shared.generated.resources.news_fact_dog_title
import mypetsinfo.shared.generated.resources.news_fact_elephant_body
import mypetsinfo.shared.generated.resources.news_fact_elephant_title
import mypetsinfo.shared.generated.resources.news_fact_octopus_body
import mypetsinfo.shared.generated.resources.news_fact_octopus_title
import mypetsinfo.shared.generated.resources.news_intro_body
import mypetsinfo.shared.generated.resources.news_intro_title
import org.jetbrains.compose.resources.stringResource

private data class NewsFact(
    val title: String,
    val body: String,
)

@Composable
internal fun NewsScreen() {
    val facts = listOf(
        NewsFact(
            title = stringResource(Res.string.news_fact_cat_title),
            body = stringResource(Res.string.news_fact_cat_body),
        ),
        NewsFact(
            title = stringResource(Res.string.news_fact_dog_title),
            body = stringResource(Res.string.news_fact_dog_body),
        ),
        NewsFact(
            title = stringResource(Res.string.news_fact_octopus_title),
            body = stringResource(Res.string.news_fact_octopus_body),
        ),
        NewsFact(
            title = stringResource(Res.string.news_fact_elephant_title),
            body = stringResource(Res.string.news_fact_elephant_body),
        ),
    )

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.news_intro_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(Res.string.news_intro_body),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        items(facts, key = { it.title }) { fact ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(18.dp),
                ) {
                    Text(
                        text = fact.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = fact.body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
