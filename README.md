# RTM Offers API — Java Automation Tests

**Учебный проект QA Automation на Java:** тестирование REST API сервиса маркетинговых предложений с **JUnit 5**, **REST Assured**, **Allure Report**, **Maven** и **GitHub Actions**.

> **Важно:** API в репозитории — специально созданный *демонстрационный локальный сервис*. Это **не** внутренний сервис МТС и не корпоративный проект. Тематика маркетинговых предложений выбрана как пример предметной области RTM (Real Time Marketing).

## Что умеет проект

- **17 автоматизированных проверок**: CRUD, позитивные/негативные сценарии, валидация входных данных, HTTP-коды, JSON-ответы, фильтрация и сохранение изменений.
- **Test design:** классы эквивалентности, негативные сценарии, параметризация JUnit 5.
- **REST Assured:** HTTP `GET`, `POST`, `PUT`, `DELETE`, проверка тела ответа и статус-кодов.
- **Allure:** названия сценариев и HTTP-вложения (через `AllureRestAssured`).
- **Независимость:** перед **каждым тестом** запускается новый локальный HTTP-сервер со свежим хранилищем на случайном свободном порту.
- **CI:** GitHub Actions запускает тесты при push и pull request, сохраняет Allure results.

## Стек

`Java 17` · `JUnit Jupiter 5` · `REST Assured` · `Allure` · `Maven` · `Jackson` · `GitHub Actions`

## Быстрый запуск

Нужны **JDK 17+** и **Apache Maven 3.9+** (или совместимая версия).

```bash
java -version
mvn -version
mvn clean test
```

Прогон одного класса:

```bash
mvn -Dtest=CreateOfferTests test
```

Смотреть **Allure Report** локально:

```bash
mvn allure:serve
```

Команда `allure:serve` создаёт отчёт из `target/allure-results`, но при первом запуске ей может потребоваться скачать Allure CLI. Для `mvn clean test` при первом запуске также нужен интернет, чтобы Maven скачал зависимости.

Сами тесты запускают HTTP-сервис **на localhost** и **не требуют ни токенов, ни доступа к внешним API**.

### Если Maven не установлен (Ubuntu / WSL)

```bash
sudo apt update
sudo apt install -y maven openjdk-17-jdk
mvn -version
mvn clean test
```

Если у вас уже установлен JDK 21, он тоже подходит: код компилируется с `--release 17`.

## Проверяемое API

| Метод | Endpoint | Описание | Успешный код |
|---|---|---|---|
| POST | `/api/v1/offers` | Создать предложение | 201 |
| GET | `/api/v1/offers/{id}` | Получить предложение | 200 |
| GET | `/api/v1/offers?clientId=...` | Список с фильтрацией | 200 |
| PUT | `/api/v1/offers/{id}` | Обновить статус | 200 |
| DELETE | `/api/v1/offers/{id}` | Удалить предложение | 204 |

Пример `POST /api/v1/offers`:

```json
{
  "clientId": "client-101",
  "campaignId": "autumn-campaign",
  "channel": "SMS"
}
```

Пример успешного ответа `201`:

```json
{
  "id": 1,
  "clientId": "client-101",
  "campaignId": "autumn-campaign",
  "channel": "SMS",
  "status": "ACTIVE"
}
```

Валидация: все поля обязательны, каналы `SMS / PUSH / EMAIL`, статусы `ACTIVE / PAUSED`. Ошибки: `400`, `404`, `405`, `415`.

## Структура проекта

```text
rtm-offers-java-api-autotests/
├── .github/workflows/tests.yml         # CI: Java + Maven + артефакт Allure
├── docs/TEST_PLAN.md                   # тест-план, тест-кейсы, ограничения
├── src/
│   ├── main/java/dev/portfolio/rtm/
│   │   ├── Offer.java                  # модель предложения
│   │   ├── OfferStore.java             # изолированное in-memory хранилище
│   │   └── OfferApiServer.java         # тестовый HTTP API
│   └── test/
│       ├── java/dev/portfolio/rtm/
│       │   ├── BaseApiTest.java        # lifecycle / RequestSpecification / Allure
│       │   ├── CreateOfferTests.java   # POST и негативные сценарии
│       │   ├── GetOfferTests.java      # GET, 404 и фильтрация
│       │   ├── UpdateOfferTests.java   # PUT, сохранение изменений
│       │   └── DeleteOfferTests.java   # DELETE, 204 / 404
│       └── resources/allure.properties
├── pom.xml
└── README.md
```

## Документация

В [тест-плане](docs/TEST_PLAN.md) перечислены сценарии **TC-01 — TC-17** и ограничения проекта.

## Как добавить на GitHub

1. Создайте **пустой публичный** репозиторий с названием `rtm-offers-java-api-autotests` (без начальных README и `.gitignore`).
2. Распакуйте ZIP и откройте терминал **внутри папки, где находится `pom.xml`**.
3. Выполните команды (SSH-способ, если ключ уже привязан к GitHub):

```bash
git init
git branch -M main
git add .
git commit -m "Add Java API automation tests with Allure"
git remote add origin git@github.com:angelina2006mmmm-lab/rtm-offers-java-api-autotests.git
git push -u origin main
```

Если SSH не настроен, можно загрузить файлы через веб-интерфейс GitHub или настроить авторизацию GitHub. После push откройте вкладку **Actions** и убедитесь, что workflow завершился успешно.

## Как указать в резюме

**RTM Offers API Autotests — Java, JUnit 5, REST Assured, Allure, Maven, GitHub Actions.** Учебный проект: разработка 17 автоматизированных проверок REST API (CRUD, негативные сценарии, HTTP/JSON, параметризация), тест-план и CI-запуск. Репозиторий GitHub — добавить ссылку после публикации.

**Что ещё можно развивать:** contract testing с OpenAPI, Docker, Kafka, SQL/NoSQL, тестовые данные и CI quality gates. Эти инструменты **не реализованы в текущем проекте**.
