# Тест-план: Demo RTM Offers REST API

## 1. Объект и цель тестирования

Демонстрационный локальный REST API маркетинговых предложений. Проверить CRUD, валидацию данных,
фильтрацию выдачи, HTTP-коды, структуру JSON-ответов и сохранение изменений.
Это учебный сервис, а не реальный API МТС.

## 2. Объём

| ID | Действие | Проверка | Ожидаемый результат |
|---|---|---|---|
| TC-01 | POST /offers | Корректный JSON | 201; id, clientId, campaignId, channel, ACTIVE |
| TC-02 | POST /offers | Отсутствует clientId | 400 |
| TC-03 | POST /offers | Пустой clientId | 400 |
| TC-04 | POST /offers | Отсутствует campaignId | 400 |
| TC-05 | POST /offers | Недопустимый channel | 400 |
| TC-06 | POST /offers | Невалидный JSON | 400 |
| TC-07 | POST /offers | Неподдерживаемый Content-Type | 415 |
| TC-08 | GET /offers/{id} | Существующий id | 200; правильная запись |
| TC-09 | GET /offers/{id} | Несуществующий id | 404 |
| TC-10 | GET /offers?clientId=... | Фильтрация | 200; только записи клиента |
| TC-11 | GET /offers | Пустое хранилище | 200; пустой массив |
| TC-12 | PUT /offers/{id} | ACTIVE → PAUSED | 200; состояние сохранено |
| TC-13 | PUT /offers/{id} | Недопустимый статус | 400; статус не изменился |
| TC-14 | PUT /offers/{id} | Несуществующий id | 404 |
| TC-15 | DELETE /offers/{id} | Удаление существующего | 204; повторный GET → 404 |
| TC-16 | DELETE /offers/{id} | Повторное удаление | 404 |
| TC-17 | GET /offers/{id} | Нечисловой id | 400 |

## 3. Подход

- Чёрный ящик: HTTP и JSON, позитивные/негативные сценарии, граничные значения.
- Интеграционные API-тесты: реальный HTTP на localhost, без mock запросов со стороны теста.
- Изоляция: сервер и хранилище создаются перед каждым тестом; порт выделяется ОС.
- Allure: шаги тестов и вложения HTTP-запросов/ответов.
- CI: `mvn -B clean verify` при push/PR, результаты Allure сохраняются как артефакт.

## 4. Ограничения

- Нет реальных внешних микросервисов, Kafka, Redis, MongoDB или GraphQL.
- Нет нагрузочных/безопасностных проверок и contract testing со Swagger/OpenAPI.
- Это pet-project для демонстрации навыков Java API testing, не production RTM.
