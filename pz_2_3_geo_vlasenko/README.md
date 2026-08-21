# geo-places-service — ПЗ-3. Гео-сервіс «Точки на карті»

Бекенд для застосунку «Мої місця»: користувач зберігає локації за адресою,
сервіс сам знаходить координати (Nominatim/OpenStreetMap), зберігає їх у БД
і вміє шукати місця в радіусі від точки (формула Haversine).

## Запуск

### Dev-режим (H2 in-memory, за замовчуванням)

```bash
./mvnw spring-boot:run
```

Застосунок підніметься на `http://localhost:8080`, H2-консоль — `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:geoplaces`).

### Prod-режим (PostgreSQL через docker-compose)

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Змінні середовища (з дефолтами): `DB_HOST=localhost`, `DB_PORT=55432`, `DB_NAME=geoplaces`,
`DB_USER=geo`, `DB_PASSWORD=geo`. Хостовий порт навмисно **55432**, а не стандартний 5432 —
щоб не конфліктувати з локально встановленим PostgreSQL-сервісом, якщо він вже є на машині
(контейнер усередині все одно слухає стандартний 5432). Перевірено наживо: CRUD-дані
дійсно пишуться в PostgreSQL (не в H2) і переживають перезапуск застосунку.

### Тести

```bash
./mvnw test
```

## Ендпоінти

| Метод | Шлях | Опис |
|---|---|---|
| POST | `/api/places` | Створити місце. Якщо `latitude`/`longitude` не передані — геокодується за `address` через Nominatim |
| GET | `/api/places?category=&name=&page=&size=` | Список місць з фільтрами й пагінацією |
| GET | `/api/places/nearby?lat=&lon=&radiusKm=&category=` | Місця в радіусі, відсортовані за відстанню (Haversine) |
| GET | `/api/places/{id}` | Отримати місце |
| PUT | `/api/places/{id}` | Оновити місце |
| DELETE | `/api/places/{id}` | Видалити місце |
| POST | `/api/places/{id}/regeocode` | Повторна спроба геокодування (якщо адресу раніше не знайдено / API було недоступне) |
| GET | `/api/geocode/reverse?lat=&lon=` | Зворотне геокодування: координати → адреса |

## Архітектура

```
controller/  → REST-шар (валідація вхідних параметрів, HTTP-статуси)
service/     → бізнес-логіка: PlaceService, GeocodingService, DistanceCalculator (Haversine)
client/      → NominatimClient (RestClient + rate-limit 1 req/sec + власний User-Agent)
repository/  → Spring Data JPA (bounding-box запит для /nearby, фільтри, пагінація)
entity/      → Place (JPA-сутність)
dto/         → запити/відповіді API
exception/   → доменні винятки + GlobalExceptionHandler (жодного "голого" 500)
config/      → RestClientConfig (User-Agent/таймаути), CacheConfig (@EnableCaching + кеш геокодування)
```

Схема БД керується Flyway-міграціями (`src/main/resources/db/migration`), не `ddl-auto`.

## Стійкість до збоїв Nominatim

- Власний `User-Agent` (`nominatim.user-agent` в `application.yml`).
- Throttling: не частіше 1 запиту/сек (`NominatimClient`).
- Адресу не знайдено / API недоступний → місце все одно зберігається без координат
  (`geocodingWarning` у відповіді), а не 500. Догеокодувати можна через `POST /{id}/regeocode`.
- Результати геокодування кешуються (`@Cacheable`) — та сама адреса не йде в API двічі.
