import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

public class StatisticsTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String BASE_URL = "https://cerulean-praline-8e5aa6.netlify.app/stats";

    // ========== ЛОКАТОРЫ ==========
    // Кнопка "Обновить"
    private static final By REFRESH_BUTTON = By.className("_refreshButton_ir5wu_16");

    // Кнопка паузы/запуска
    private static final By TOGGLE_BUTTON = By.className("_toggleButton_ir5wu_69");

    // Отображение таймера
    private static final By TIMER_VALUE = By.className("_timeValue_ir5wu_112");

    // Прогресс-бар
    private static final By PROGRESS_BAR_FILL = By.className("_progressFill_ir5wu_129");

    @BeforeClass
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(BASE_URL);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ========== ТЕСТ-КЕЙС 5.0: Проверка доступности страницы статистики ==========

    @Test(priority = 1)
    public void testStatisticsPageAccessible() {
        System.out.println("=== Тест-кейс 5.0: Проверка доступности страницы статистики ===");

        // Проверяем текущий URL
        String currentUrl = driver.getCurrentUrl();
        System.out.println("Текущий URL: " + currentUrl);

        // Проверяем, что URL соответствует ожидаемому
        Assert.assertEquals(currentUrl, BASE_URL,
                "❌ БАГ: Страница статистики недоступна. Ожидался URL: " + BASE_URL +
                        ", получен: " + currentUrl);

        // Проверяем, что страница не содержит ошибку 404
        String pageSource = driver.getPageSource();
        boolean is404 = pageSource.contains("404") ||
                pageSource.contains("Not Found") ||
                pageSource.contains("Страница не найдена");

        if (is404) {
            System.out.println("❌ БАГ: Страница статистики возвращает 404 Not Found");
            Assert.fail("❌ БАГ: Страница статистики недоступна (404). " +
                    "Ссылка ведёт на несуществующую страницу.");
        }

        // Проверяем, что есть кнопка "Обновить" (признак того, что страница загружена корректно)
        try {
            WebElement refreshButton = wait.until(ExpectedConditions.presenceOfElementLocated(REFRESH_BUTTON));
            System.out.println("✅ Кнопка 'Обновить' найдена на странице");
        } catch (Exception e) {
            System.out.println("❌ БАГ: На странице статистики не найдена кнопка 'Обновить'");
            Assert.fail("❌ БАГ: Страница статистики не содержит ожидаемых элементов управления");
        }

        System.out.println("✅ Страница статистики доступна и содержит корректный контент");
    }

    // ========== ТЕСТ-КЕЙС 5.1: Проверка кнопки "Обновить" ==========

    @Test(priority = 2, dependsOnMethods = "testStatisticsPageAccessible")
    public void testRefreshButton() {
        System.out.println("=== Тест-кейс 5.1: Проверка кнопки 'Обновить' ===");

        // Запоминаем текущее значение таймера
        WebElement timer = driver.findElement(TIMER_VALUE);
        String timerBefore = timer.getText();
        System.out.println("Таймер до обновления: " + timerBefore);

        // Нажимаем кнопку "Обновить"
        WebElement refreshButton = driver.findElement(REFRESH_BUTTON);
        refreshButton.click();

        // Ждем обновления (таймер должен сброситься)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Проверяем, что таймер изменился (сбросился)
        String timerAfter = timer.getText();
        System.out.println("Таймер после обновления: " + timerAfter);

        Assert.assertNotEquals(timerAfter, timerBefore,
                "❌ Таймер не изменился после нажатия кнопки 'Обновить'");

        // Проверяем, что таймер сбросился на максимальное значение (обычно 5:00)
        Assert.assertTrue(timerAfter.equals("5:00") || timerAfter.contains("5:00"),
                "❌ Таймер не сбросился на 5:00, текущее значение: " + timerAfter);

        System.out.println("✅ Кнопка 'Обновить' работает корректно");
    }

    // ========== ТЕСТ-КЕЙС 5.2: Проверка работы таймера (автообновление) ==========

    @Test(priority = 3, dependsOnMethods = "testStatisticsPageAccessible")
    public void testTimerAutoRefresh() {
        System.out.println("=== Тест-кейс 5.2: Проверка работы таймера ===");

        // Запоминаем начальное значение таймера
        WebElement timer = driver.findElement(TIMER_VALUE);
        String initialTimer = timer.getText();
        System.out.println("Начальное значение таймера: " + initialTimer);

        // Ждем 2 секунды и проверяем, что таймер уменьшился
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String timerAfter2Sec = timer.getText();
        System.out.println("Таймер через 2 секунды: " + timerAfter2Sec);

        Assert.assertNotEquals(timerAfter2Sec, initialTimer,
                "❌ Таймер не изменился через 2 секунды");

        // Проверяем, что таймер действительно уменьшился
        // Парсим время (формат "минуты:секунды")
        int initialSeconds = parseTimeToSeconds(initialTimer);
        int afterSeconds = parseTimeToSeconds(timerAfter2Sec);

        Assert.assertTrue(afterSeconds < initialSeconds,
                String.format("❌ Таймер не уменьшился: было %d сек, стало %d сек",
                        initialSeconds, afterSeconds));

        System.out.println("✅ Таймер работает корректно");
    }

    // ========== ТЕСТ-КЕЙС 5.3: Проверка остановки таймера ==========

    @Test(priority = 4, dependsOnMethods = "testStatisticsPageAccessible")
    public void testPauseTimer() {
        System.out.println("=== Тест-кейс 5.3: Проверка остановки таймера ===");

        WebElement toggleButton = driver.findElement(TOGGLE_BUTTON);
        WebElement timer = driver.findElement(TIMER_VALUE);

        // Запоминаем текущее значение таймера
        String timerBefore = timer.getText();
        System.out.println("Таймер до паузы: " + timerBefore);

        // Нажимаем кнопку паузы (останавливаем автообновление)
        toggleButton.click();

        // Проверяем, что кнопка изменила состояние (активна/неактивна)
        String buttonClass = toggleButton.getAttribute("class");
        boolean isPaused = buttonClass.contains("_toggleButton_active_ir5wu_89");
        System.out.println("Состояние кнопки после паузы: " + (isPaused ? "активна" : "неактивна"));

        // Ждем 3 секунды и проверяем, что таймер не изменился
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String timerAfter = timer.getText();
        System.out.println("Таймер через 3 секунды после паузы: " + timerAfter);

        // Таймер не должен измениться (или измениться незначительно из-за округления)
        Assert.assertEquals(timerAfter, timerBefore,
                String.format("❌ Таймер изменился после паузы: было %s, стало %s",
                        timerBefore, timerAfter));

        System.out.println("✅ Кнопка паузы работает корректно: таймер остановлен");
    }

    // ========== ТЕСТ-КЕЙС 5.4: Проверка восстановления таймера ==========

    @Test(priority = 5, dependsOnMethods = "testStatisticsPageAccessible")
    public void testResumeTimer() {
        System.out.println("=== Тест-кейс 5.4: Проверка восстановления таймера ===");

        WebElement toggleButton = driver.findElement(TOGGLE_BUTTON);
        WebElement timer = driver.findElement(TIMER_VALUE);

        // Сначала останавливаем таймер
        String buttonClass = toggleButton.getAttribute("class");
        boolean isRunning = buttonClass.contains("_toggleButton_active_ir5wu_89");

        if (isRunning) {
            // Если таймер активен, останавливаем
            toggleButton.click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Запоминаем значение на паузе
        String timerPaused = timer.getText();
        System.out.println("Таймер на паузе: " + timerPaused);

        // Нажимаем кнопку "Запуск" (снова кликаем по той же кнопке)
        toggleButton.click();

        // Проверяем, что кнопка изменила состояние
        buttonClass = toggleButton.getAttribute("class");
        boolean isRunningAfter = buttonClass.contains("_toggleButton_active_ir5wu_89");
        System.out.println("Состояние кнопки после запуска: " + (isRunningAfter ? "активна" : "неактивна"));

        // Ждем 2 секунды и проверяем, что таймер уменьшился
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String timerAfter = timer.getText();
        System.out.println("Таймер через 2 секунды после запуска: " + timerAfter);

        // Таймер должен измениться (уменьшиться)
        int pausedSeconds = parseTimeToSeconds(timerPaused);
        int afterSeconds = parseTimeToSeconds(timerAfter);

        if (afterSeconds >= pausedSeconds) {
            System.out.println("⚠️ По багу: таймер не запустился после нажатия");
            Assert.fail("❌ БАГ: таймер не запускается после нажатия кнопки запуска. " +
                    "Было: " + timerPaused + ", стало: " + timerAfter);
        } else {
            System.out.println("✅ Таймер успешно запустился");
        }

        System.out.println("✅ Тест-кейс 5.4 завершен");
    }

    // ========== ВСПОМОГАТЕЛЬНЫЙ МЕТОД ==========

    private int parseTimeToSeconds(String time) {
        try {
            String[] parts = time.split(":");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            return minutes * 60 + seconds;
        } catch (Exception e) {
            return 0;
        }
    }
}