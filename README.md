# QA
# Автотесты для тестирования сайта объявлений

## 📋 Описание

Набор автоматизированных тестов для проверки функциональности сайта объявлений. Тесты написаны на Java с использованием Selenium WebDriver и TestNG.

## 📁 Структура проекта
src/test/java/
├── CategoryFilterTest.java # Тесты фильтра по категории
├── PriceFilterTest.java # Тесты фильтра по цене
├── SortingTest.java # Тесты сортировки по цене
├── StatisticsTest.java # Тесты страницы статистики
├── ThemeSwitchTest.java # Тесты переключения темы (мобильная версия)
└── UrgentToggleTest.java # Тесты фильтра "Срочность"

text

## ⚙️ Требования

- Java 11 или выше
- Chrome браузер (последняя версия)
- Gradle (или использовать встроенный в IDEA)

## 🚀 Запуск тестов

### Способ 1: Запуск всех тестов

```bash
./gradlew test
**Способ 2: Запуск конкретного класса тестов**
bash
# Запустить тесты фильтра по цене
./gradlew test --tests PriceFilterTest

# Запустить тесты сортировки
./gradlew test --tests SortingTest

# Запустить тесты фильтра по категории
./gradlew test --tests CategoryFilterTest

# Запустить тесты фильтра "Срочность"
./gradlew test --tests UrgentToggleTest

# Запустить тесты страницы статистики
./gradlew test --tests StatisticsTest

# Запустить тесты переключения темы
./gradlew test --tests ThemeSwitchTest
Способ 3: Запуск конкретного теста
bash
# Пример: запустить только тест фильтра от 1014
./gradlew test --tests PriceFilterTest.testPriceFromBoundaryValue_1014

# Пример: запустить только тест сортировки по возрастанию
./gradlew test --tests SortingTest.testSortByPriceAscending
Способ 4: Запуск через IntelliJ IDEA
Откройте проект в IntelliJ IDEA

Нажмите правой кнопкой мыши на папку src/test/java

Выберите Run 'All Tests'

Или запустите конкретный класс:

Откройте нужный тестовый класс

Нажмите зеленую стрелку ▶ рядом с названием класса

📊 Отчет о тестировании
После выполнения тестов отчет формируется в папке:

text
build/reports/tests/test/index.html
Откройте файл в браузере для просмотра деталей.

🔧 Настройка перед запуском
Установка ChromeDriver
Тесты используют WebDriverManager, который автоматически загружает подходящую версию ChromeDriver. Никаких дополнительных действий не требуется.

Зависимости (build.gradle)
gradle
dependencies {
    testImplementation 'org.seleniumhq.selenium:selenium-java:4.15.0'
    testImplementation 'org.testng:testng:7.8.0'
    testImplementation 'io.github.bonigarcia:webdrivermanager:5.6.2'
}
📝 Список тестов
Класс	Тесты	Описание
PriceFilterTest	31	Фильтрация по диапазону цен
SortingTest	3	Сортировка по цене
CategoryFilterTest	3	Фильтрация по категории
UrgentToggleTest	2	Фильтр "Только срочные"
StatisticsTest	5	Страница статистики
ThemeSwitchTest	2	Переключение темы (мобильная версия)
⚠️ Известные баги
При запуске тестов некоторые тесты могут падать. Это ожидаемое поведение — тесты выявляют реальные баги в приложении:

Фильтр по цене — не отображаются объявления с граничными значениями

Фильтр "Срочность" — не фильтрует объявления

Страница статистики — возвращает 404

Кнопка "Сбросить" — не работает

Очистите кэш Gradle:

bash
./gradlew clean
Перезапустите тесты

Если не работает WebDriverManager
Добавьте в build.gradle:

gradle
testImplementation 'io.github.bonigarcia:webdrivermanager:5.6.2'


