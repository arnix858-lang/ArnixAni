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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
    val episodes: Int,
    val sourceUrl: String
)

private val demoAnime = listOf(
    AnimeItem(
        title = "Fullmetal Alchemist: Brotherhood",
        genres = listOf("Adventure", "Fantasy", "Action"),
        description = "Два брата-алхимика ищут способ вернуть утраченные тела и раскрывают заговор в армии.",
        episodes = 64,
        sourceUrl = "https://www.crunchyroll.com/"
    ),
    AnimeItem(
        title = "Attack on Titan",
        genres = listOf("Drama", "Action", "Mystery"),
        description = "Оставшееся человечество сражается с титанами за пределами стен.",
        episodes = 94,
        sourceUrl = "https://www.crunchyroll.com/"
    ),
    AnimeItem(
        title = "Steins;Gate",
        genres = listOf("Sci-Fi", "Thriller"),
        description = "Группа друзей случайно открывает способ отправлять сообщения в прошлое.",
        episodes = 24,
        sourceUrl = "https://www.crunchyroll.com/"
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
            BottomScreen.Settings -> PlaceholderScreen(paddingValues, "Добавьте авторизацию и настройки качества")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogScreen(paddingValues: PaddingValues) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        demoAnime.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.genres.any { genre -> genre.contains(query, ignoreCase = true) }
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
            onSearch = {},
            active = false,
            onActiveChange = {},
            placeholder = { Text("Поиск аниме по названию или жанру") },
            modifier = Modifier.fillMaxWidth()
        ) {}

        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { anime ->
                AnimeCard(anime)
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

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                anime.genres.forEach { genre ->
                    AssistChip(onClick = {}, label = { Text(genre) })
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Эпизодов: ${anime.episodes}", style = MaterialTheme.typography.labelLarge)
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
