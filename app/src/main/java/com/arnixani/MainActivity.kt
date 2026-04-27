package com.arnixani

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                ArnixAniApp()
            }
        }
    }
}

private enum class BottomScreen(val title: String) {
    Catalog("Каталог"),
    Favorites("Избранное"),
    Settings("Настройки")
}

data class AnimeItem(
    val title: String,
    val genres: List<String>,
    val description: String,
    val episodes: Int?,
    val sourceUrl: String
)

private val demoAnime = listOf(
    AnimeItem(
        title = "Fullmetal Alchemist: Brotherhood",
        genres = listOf("Adventure", "Fantasy", "Action"),
        description = "Демо-контент. Подключите Kodik API для реального каталога.",
        episodes = 64,
        sourceUrl = "https://kodikapi.com"
    ),
    AnimeItem(
        title = "Attack on Titan",
        genres = listOf("Drama", "Action", "Mystery"),
        description = "Демо-контент. Можно искать реальные тайтлы через Kodik API.",
        episodes = 94,
        sourceUrl = "https://kodikapi.com"
    )
)

@Composable
private fun ArnixAniApp() {
    var selectedScreen by remember { mutableStateOf(BottomScreen.Catalog) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                BottomScreen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen },
                        label = { Text(screen.title) },
                        icon = {}
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedScreen) {
            BottomScreen.Catalog -> CatalogScreen(paddingValues)
            BottomScreen.Favorites -> PlaceholderScreen(paddingValues, "Сохраняйте тайтлы в избранное")
            BottomScreen.Settings -> PlaceholderScreen(
                paddingValues,
                "Источник: Kodik API (https://kodik-api.com). Токен можно получить у Kodik."
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogScreen(paddingValues: PaddingValues) {
    var query by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var animeList by remember { mutableStateOf(demoAnime) }
    val scope = rememberCoroutineScope()

    fun fetchFromKodik() {
        if (query.isBlank()) {
            errorText = "Введите название для поиска"
            return
        }
        if (token.isBlank()) {
            errorText = "Введите токен Kodik API"
            return
        }

        scope.launch {
            loading = true
            errorText = null
            runCatching {
                KodikApi.search(title = query, token = token)
            }.onSuccess { result ->
                animeList = if (result.isEmpty()) {
                    errorText = "По вашему запросу ничего не найдено"
                    emptyList()
                } else {
                    result
                }
            }.onFailure { error ->
                errorText = "Ошибка запроса: ${error.message ?: "неизвестная"}"
            }
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = { fetchFromKodik() },
            active = false,
            onActiveChange = {},
            placeholder = { Text("Поиск в Kodik API") },
            modifier = Modifier.fillMaxWidth()
        ) {}

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Kodik API token") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))
        Button(onClick = { fetchFromKodik() }) {
            Text("Найти в Kodik")
        }

        if (loading) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator()
        }

        errorText?.let {
            Spacer(Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(animeList) { anime ->
                AnimeCard(anime)
            }
        }
    }
}

private object KodikApi {
    suspend fun search(title: String, token: String): List<AnimeItem> = withContext(Dispatchers.IO) {
        val encodedTitle = URLEncoder.encode(title, Charsets.UTF_8.name())
        val encodedToken = URLEncoder.encode(token, Charsets.UTF_8.name())
        val requestUrl = "https://kodik-api.com/search?token=$encodedToken&title=$encodedTitle&limit=15&with_material_data=true"
        val connection = URL(requestUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000

        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            throw IllegalStateException("HTTP $responseCode")
        }

        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(body)
        val results = json.optJSONArray("results") ?: return@withContext emptyList()

        buildList {
            for (i in 0 until results.length()) {
                val item = results.optJSONObject(i) ?: continue
                val materialData = item.optJSONObject("material_data")
                val genresArray = materialData?.optJSONArray("anime_genres")

                val genres = buildList {
                    if (genresArray != null) {
                        for (g in 0 until genresArray.length()) {
                            add(genresArray.optString(g))
                        }
                    }
                }

                val episodes = materialData?.optInt("episodes_total").takeIf { it != null && it > 0 }
                val rawLink = item.optString("link", "")
                val normalizedLink = when {
                    rawLink.startsWith("//") -> "https:$rawLink"
                    rawLink.startsWith("http") -> rawLink
                    else -> "https://kodik-api.com"
                }

                add(
                    AnimeItem(
                        title = item.optString("title", "Без названия"),
                        genres = genres,
                        description = materialData?.optString("description")
                            ?.takeIf { it.isNotBlank() }
                            ?: "Описание отсутствует",
                        episodes = episodes,
                        sourceUrl = normalizedLink
                    )
                )
            }
        }
    }
}

@Composable
private fun AnimeCard(anime: AnimeItem) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(anime.sourceUrl))
                context.startActivity(intent)
            }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = anime.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = anime.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (anime.genres.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    anime.genres.take(4).forEach { genre ->
                        AssistChip(onClick = {}, label = { Text(genre) })
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = anime.episodes?.let { "Эпизодов: $it" } ?: "Эпизоды: нет данных",
                style = MaterialTheme.typography.labelLarge
            )
            Text(text = "Нажмите, чтобы открыть источник", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun PlaceholderScreen(paddingValues: PaddingValues, text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}
