const tokenInput = document.getElementById("token");
const queryInput = document.getElementById("query");
const searchBtn = document.getElementById("searchBtn");
const results = document.getElementById("results");
const statusEl = document.getElementById("status");
const cardTemplate = document.getElementById("animeCardTemplate");

const API_URL = "https://kodik-api.com/search";

function normalizeLink(link) {
  if (!link) return "https://kodikapi.com";
  if (link.startsWith("//")) return `https:${link}`;
  return link;
}

function setStatus(message, isError = false) {
  statusEl.textContent = message;
  statusEl.style.color = isError ? "#ff9ea8" : "#aeb7dd";
}

function renderResults(items) {
  results.innerHTML = "";

  if (!items.length) {
    setStatus("Ничего не найдено.");
    return;
  }

  items.forEach((item) => {
    const node = cardTemplate.content.cloneNode(true);
    node.querySelector(".title").textContent = item.title || "Без названия";

    const description = item.material_data?.description || "Описание отсутствует";
    node.querySelector(".description").textContent = description;

    const episodes = item.material_data?.episodes_total || "нет данных";
    node.querySelector(".meta").textContent = `Эпизодов: ${episodes}`;

    const genres = item.material_data?.anime_genres?.join(", ") || "Жанры не указаны";
    node.querySelector(".genres").textContent = genres;

    const link = normalizeLink(item.link);
    const watchLink = node.querySelector(".watch-link");
    watchLink.href = link;

    results.appendChild(node);
  });

  setStatus(`Найдено: ${items.length}`);
}

async function searchAnime() {
  const token = tokenInput.value.trim();
  const query = queryInput.value.trim();

  if (!token) {
    setStatus("Введите Kodik token.", true);
    return;
  }

  if (!query) {
    setStatus("Введите название аниме.", true);
    return;
  }

  setStatus("Загрузка...");
  searchBtn.disabled = true;

  try {
    const params = new URLSearchParams({
      token,
      title: query,
      with_material_data: "true",
      limit: "20",
    });

    const response = await fetch(`${API_URL}?${params.toString()}`, {
      method: "POST",
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    const data = await response.json();
    renderResults(data.results || []);
  } catch (error) {
    setStatus(`Ошибка запроса: ${error.message}`, true);
  } finally {
    searchBtn.disabled = false;
  }
}

searchBtn.addEventListener("click", searchAnime);
queryInput.addEventListener("keydown", (event) => {
  if (event.key === "Enter") searchAnime();
});
