# sales-report-service

Spring Boot застосунок для обліку продажів та автоматичної розсилки звітів
(PDF + Excel) на email. ПЗ 2.2.

## Вимоги

- Java 26
- Maven не потрібен окремо — в проєкті є `mvnw` / `mvnw.cmd` (Maven Wrapper)

## Налаштування пошти (обов'язково перед першим запуском)

Розсилка йде через Gmail SMTP. Дані беруться зі змінних `MAIL_USERNAME` /
`MAIL_PASSWORD`, які підтягуються з файлу `.env` в корені модуля
(`pz_2_2_vlasenko/.env`, у git не потрапляє — див. `.gitignore`).

Створи файл `.env` поруч із `pom.xml`:

```properties
MAIL_USERNAME=твоя_пошта@gmail.com
MAIL_PASSWORD=пароль_застосунку_gmail
```

> Потрібен саме **App Password** Gmail (звичайний пароль від акаунта не
> підійде, якщо ввімкнена двофакторка) — Google Account → Security →
> 2-Step Verification → App passwords.

Кому надсилати звіти — керується окремо, у `application.properties`:

```properties
report.recipients=пошта1@example.com,пошта2@example.com
```

## Запуск

```bash
./mvnw spring-boot:run
```

(Windows: `mvnw.cmd spring-boot:run`)

Застосунок піднімається на `http://localhost:8080`.

При кожному старті відбувається (у такому порядку):

1. **`DataSeeder`** — наповнює in-memory сховище тестовими продажами за
   поточний, минулий і позаминулий місяць (дані пропадають при перезапуску).
2. **`StartupReportMailer`** — формує PDF+Excel звіт і одразу шле листом на
   `report.recipients` (можна вимкнути / налаштувати період — див. нижче).

## Як задати період звіту, що шлеться при старті

Файл: `src/main/resources/application.properties`, ключі `app.startup-mail.*`.

Обери **один** з двох варіантів (другий лиши порожнім):

```properties
# Варіант 1 — цілий календарний місяць
app.startup-mail.month=2026-05
app.startup-mail.from=
app.startup-mail.to=

# Варіант 2 — довільний період
app.startup-mail.month=
app.startup-mail.from=2026-06-01
app.startup-mail.to=2026-08-31
```

- Формат `month` — `YYYY-MM`; формат `from`/`to` — `YYYY-MM-DD`.
- Якщо всі три поля порожні — береться поточний місяць.
- Заповнювати `month` разом із `from`/`to` не можна — розсилка впаде з
  помилкою в лог (сам застосунок не падає), лист просто не піде.
- Щоб взагалі вимкнути лист при старті: `app.startup-mail.enabled=false`.
- Зміни в цьому файлі діють лише після перезапуску застосунку.

Ті самі ключі можна замість цього класти в `.env` — він теж підхоплюється
(`spring.config.import=optional:file:.env[.properties]`) і не в git, якщо
не хочеш тримати робочий період у `application.properties`.

## API

### Продажі

| Метод | Шлях     | Опис                                                  |
|-------|----------|--------------------------------------------------------|
| POST  | `/sales` | Додати продаж (тіло — `SaleRequestDto`)                |
| GET   | `/sales` | Список продажів, фільтри: `region`, `from`, `to`        |

### Звіти

Період для звітів задається так само, як для стартового листа: **або**
`month=YYYY-MM`, **або** пара `from=YYYY-MM-DD&to=YYYY-MM-DD`. Некоректна
комбінація → `400 Bad Request`.

| Метод | Шлях             | Опис                                    |
|-------|------------------|------------------------------------------|
| GET   | `/reports/sales.pdf`  | PDF-звіт за період (inline)         |
| GET   | `/reports/sales.xlsx` | Excel-звіт за період (attachment)   |
| POST  | `/reports/send`       | Сформувати PDF і надіслати на `report.recipients` |

Приклади:

```
GET /reports/sales.pdf?month=2026-08
GET /reports/sales.xlsx?from=2026-06-01&to=2026-08-31
POST /reports/send?month=2026-08
```
```
- GET /reports/sales.pdf?month=2026-08 — PDF за місяць.
- GET /reports/sales.xlsx?from=2026-01-01&to=2026-08-31 — Excel за діапазон.
- POST /reports/send?month=2026-08 — надіслати звіт на email (з report.recipients).
```
```
{
  "manager": "Іван Петренко",
  "product": "Ноутбук",
  "amount": 15999.99,
  "region": "KYIV",
  "date": "2026-08-17"
}
```
## Автоматична щомісячна розсилка

`ReportScheduler` шле звіт за **попередній** місяць 1-го числа о 03:00
(Europe/Kyiv), незалежно від налаштувань `app.startup-mail.*`. Вимикається
тільки видаленням/коментуванням `@Scheduled`-компонента.

## Тести

```bash
./mvnw test
```

Розсилка листів у тестах вимкнена окремо (`@SpringBootTest(properties = ...)`
у `SalesReportServiceApplicationTests`), тож `mvn test` нікуди нічого не шле.
