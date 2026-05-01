# ArnixAni Web

Простой статический сайт для просмотра каталога аниме.

## Запуск локально

Открой `index.html` в браузере или запусти локальный сервер:

```bash
python3 -m http.server 8080 --directory web
```

После этого открой `http://localhost:8080`.

## Публикация на GitHub Pages

В репозиторий добавлен workflow `.github/workflows/deploy-pages.yml`, который публикует папку `web/` в GitHub Pages после пуша.

1. Открой **Settings → Pages**.
2. В разделе **Build and deployment** выбери **Source: GitHub Actions**.
3. Запушь изменения в одну из веток, указанных в workflow (`main`, `master` или `work`).
4. После успешного workflow сайт появится по адресу:
   `https://<username>.github.io/<repository>/`.
