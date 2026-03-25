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
import java.util.HashMap;
import java.util.Map;

public class ThemeSwitchTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String BASE_URL = "https://cerulean-praline-8e5aa6.netlify.app/";

    // ========== ЛОКАТОРЫ ==========
    // Кнопка переключения темы
    private static final By THEME_TOGGLE = By.className("_themeToggle_127us_1");

    // Иконка на кнопке
    private static final By THEME_ICON = By.className("_icon_127us_23");

    // Основной контейнер
    private static final By MAIN_CONTAINER = By.tagName("body");

    @BeforeClass
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();

        // Эмуляция мобильного устройства (iPhone 12)
        Map<String, Object> deviceMetrics = new HashMap<>();
        deviceMetrics.put("width", 390);
        deviceMetrics.put("height", 844);
        deviceMetrics.put("pixelRatio", 3.0);

        Map<String, Object> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceMetrics", deviceMetrics);
        mobileEmulation.put("userAgent", "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1");

        options.setExperimentalOption("mobileEmulation", mobileEmulation);
        options.addArguments("--disable-notifications");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(BASE_URL);

        // Ждем загрузки страницы и появления кнопки
        wait.until(ExpectedConditions.presenceOfElementLocated(THEME_TOGGLE));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ========== ТЕСТ-КЕЙС 6.1: Проверка переключения между тёмной и светлой темой ==========

    @Test(priority = 1)
    public void testThemeSwitching() {
        System.out.println("=== Тест-кейс 6.1: Проверка переключения между тёмной и светлой темой ===");

        WebElement themeToggle = driver.findElement(THEME_TOGGLE);

        // Запоминаем начальное состояние (иконка)
        String initialIcon = getThemeIcon();
        System.out.println("Начальная иконка: " + initialIcon);

        // Переключаем на тёмную тему
        themeToggle.click();
        waitForThemeChange();

        String darkIcon = getThemeIcon();
        System.out.println("После переключения на тёмную тему: иконка = " + darkIcon);

        // Проверяем, что иконка изменилась
        Assert.assertNotEquals(darkIcon, initialIcon,
                "❌ БАГ: Иконка не изменилась при переключении на тёмную тему");

        // Проверяем, что иконка соответствует тёмной теме (☀️ или 🌙 в зависимости от реализации)
        // В мобильной версии: светлая тема = 🌙, тёмная = ☀️
        Assert.assertTrue(darkIcon.contains("☀️"),
                "❌ БАГ: В тёмной теме должна быть иконка ☀️, найдено: " + darkIcon);

        // Переключаем обратно на светлую тему
        themeToggle.click();
        waitForThemeChange();

        String lightIcon = getThemeIcon();
        System.out.println("После возврата на светлую тему: иконка = " + lightIcon);

        // Проверяем, что вернулись к исходному состоянию
        Assert.assertEquals(lightIcon, initialIcon,
                "❌ БАГ: После возврата иконка не вернулась в исходное состояние");

        System.out.println("✅ Переключение между тёмной и светлой темой работает корректно");
    }

    // ========== ДОПОЛНИТЕЛЬНЫЙ ТЕСТ: Проверка сохранения темы после обновления страницы ==========

    @Test(priority = 2)
    public void testThemePersistence() {
        System.out.println("=== Дополнительный тест: Сохранение темы после обновления страницы ===");

        WebElement themeToggle = driver.findElement(THEME_TOGGLE);

        // Запоминаем начальную иконку
        String initialIcon = getThemeIcon();
        System.out.println("Начальная иконка: " + initialIcon);

        // Переключаем на тёмную тему
        themeToggle.click();
        waitForThemeChange();

        String darkIcon = getThemeIcon();
        System.out.println("Тема после переключения: иконка = " + darkIcon);

        // Обновляем страницу
        driver.navigate().refresh();

        // Ждем загрузки и находим кнопку заново
        wait.until(ExpectedConditions.presenceOfElementLocated(THEME_TOGGLE));

        String iconAfterRefresh = getThemeIcon();
        System.out.println("Иконка после обновления страницы: " + iconAfterRefresh);

        // Проверяем, что тема сохранилась
        Assert.assertEquals(iconAfterRefresh, darkIcon,
                "❌ БАГ: Тема не сохранилась после обновления страницы");

        System.out.println("✅ Тема сохраняется после обновления страницы");
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private String getThemeIcon() {
        try {
            WebElement icon = driver.findElement(THEME_ICON);
            return icon.getText();
        } catch (Exception e) {
            return "";
        }
    }

    private void waitForThemeChange() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}