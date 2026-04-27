const animeData = [
  {
    title: 'Атака титанов',
    description: 'Эпичная история о выживании человечества за стенами.',
    genres: ['Экшен', 'Драма'],
    poster: 'https://images.unsplash.com/photo-1580927752452-89d86da3fa0a?auto=format&fit=crop&w=700&q=80',
    link: 'https://myanimelist.net/anime/16498/Shingeki_no_Kyojin'
  },
  {
    title: 'Клинок, рассекающий демонов',
    description: 'Тандзиро отправляется в путь, чтобы спасти сестру.',
    genres: ['Экшен', 'Фэнтези'],
    poster: 'https://images.unsplash.com/photo-1613376023733-0a73315d9b06?auto=format&fit=crop&w=700&q=80',
    link: 'https://myanimelist.net/anime/38000/Kimetsu_no_Yaiba'
  },
  {
    title: 'Твоё имя',
    description: 'Романтическая история о загадочной связи двух подростков.',
    genres: ['Романтика', 'Драма'],
    poster: 'https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=700&q=80',
    link: 'https://myanimelist.net/anime/32281/Kimi_no_Na_wa'
  },
  {
    title: 'Ванпанчмен',
    description: 'Сайтама побеждает любого противника одним ударом.',
    genres: ['Комедия', 'Экшен'],
    poster: 'https://images.unsplash.com/photo-1541562232579-512a21360020?auto=format&fit=crop&w=700&q=80',
    link: 'https://myanimelist.net/anime/30276/One_Punch_Man'
  },
  {
    title: 'Тетрадь смерти',
    description: 'Интеллектуальное противостояние школьника и детектива.',
    genres: ['Триллер', 'Драма'],
    poster: 'https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?auto=format&fit=crop&w=700&q=80',
    link: 'https://myanimelist.net/anime/1535/Death_Note'
  }
];

const grid = document.getElementById('animeGrid');
const template = document.getElementById('animeCardTemplate');
const searchInput = document.getElementById('searchInput');
const filters = document.getElementById('genreFilters');

const genres = ['Все', ...new Set(animeData.flatMap((a) => a.genres))];
let currentGenre = 'Все';

function renderFilters() {
  filters.innerHTML = '';
  genres.forEach((genre) => {
    const btn = document.createElement('button');
    btn.textContent = genre;
    btn.className = `filter-btn ${genre === currentGenre ? 'active' : ''}`;
    btn.addEventListener('click', () => {
      currentGenre = genre;
      renderFilters();
      renderCards();
    });
    filters.appendChild(btn);
  });
}

function renderCards() {
  const query = searchInput.value.trim().toLowerCase();
  grid.innerHTML = '';

  const filtered = animeData.filter((anime) => {
    const byQuery = anime.title.toLowerCase().includes(query);
    const byGenre = currentGenre === 'Все' || anime.genres.includes(currentGenre);
    return byQuery && byGenre;
  });

  if (!filtered.length) {
    grid.innerHTML = '<p>Ничего не найдено. Попробуй другой запрос.</p>';
    return;
  }

  filtered.forEach((anime) => {
    const node = template.content.cloneNode(true);
    node.querySelector('.poster').src = anime.poster;
    node.querySelector('.title').textContent = anime.title;
    node.querySelector('.desc').textContent = anime.description;
    node.querySelector('.watch-btn').href = anime.link;

    const tags = node.querySelector('.tags');
    anime.genres.forEach((g) => {
      const tag = document.createElement('span');
      tag.className = 'tag';
      tag.textContent = g;
      tags.appendChild(tag);
    });

    grid.appendChild(node);
  });
}

searchInput.addEventListener('input', renderCards);
renderFilters();
renderCards();
