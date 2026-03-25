import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class UrgentToggleTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String BASE_URL = "https://cerulean-praline-8e5aa6.netlify.app/";

    // ========== ЛОКАТОРЫ ==========
    private static final By AD_CARD = By.className("_card_15fhn_2");
    private static final By AD_PRIORITY = By.className("_card__priority_15fhn_172");

    // Правильный локатор для выпадающего списка "Срочность"
    private static final By URGENT_SELECT = By.xpath("//*[@id=\"root\"]/div/div[2]/aside/div[2]/div[3]/select");

    // Значения опций
    private static final String ALL_URGENCY = "";
    private static final String NORMAL_URGENCY = "normal";
    private static final String URGENT_ONLY = "urgent";

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
        wait.until(ExpectedConditions.presenceOfElementLocated(AD_CARD));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ========== ТЕСТ-КЕЙС 4: Проверка фильтра "Срочный" ==========

    @Test(priority = 1)
    public void testUrgentFilter() {
        System.out.println("=== Тест-кейс 4: Фильтр 'Срочный' ===");

        // Шаг 1: Находим выпадающий список срочности
        WebElement urgentSelect = wait.until(ExpectedConditions.presenceOfElementLocated(URGENT_SELECT));
        Select select = new Select(urgentSelect);

        // Шаг 2: Выбираем "Срочный"
        select.selectByValue(URGENT_ONLY);
        System.out.println("Выбран фильтр: Срочный");

        // Ждем обновления списка
        waitForUpdate();

        // Шаг 3: Проверяем, что все объявления имеют бейдж "Срочно"
        List<WebElement> cards = driver.findElements(AD_CARD);
        System.out.println("Найдено объявлений после фильтра 'Срочный': " + cards.size());

        boolean hasNonUrgent = false;
        int urgentCount = 0;

        for (WebElement card : cards) {
            List<WebElement> priorityBadges = card.findElements(AD_PRIORITY);
            if (priorityBadges.isEmpty()) {
                hasNonUrgent = true;
                System.out.println("❌ Найдено объявление без метки 'Срочно'");
            } else {
                urgentCount++;
            }
        }

        if (hasNonUrgent) {
            Assert.fail("❌ БАГ: при фильтре 'Срочный' отображаются несрочные объявления");
        } else {
            System.out.println("✅ Все отображаемые объявления имеют метку 'Срочно'. Найдено срочных: " + urgentCount);
        }

        // Шаг 4: Выбираем "Все" и проверяем, что появились и срочные, и несрочные
        select.selectByValue(ALL_URGENCY);
        System.out.println("Выбран фильтр: Все");
        waitForUpdate();

        cards = driver.findElements(AD_CARD);
        boolean hasUrgent = false;
        boolean hasNonUrgentAgain = false;
        int urgentAfter = 0, nonUrgentAfter = 0;

        for (WebElement card : cards) {
            List<WebElement> priorityBadges = card.findElements(AD_PRIORITY);
            if (!priorityBadges.isEmpty()) {
                hasUrgent = true;
                urgentAfter++;
            } else {
                hasNonUrgentAgain = true;
                nonUrgentAfter++;
            }
        }

        System.out.println("После выбора 'Все':");
        System.out.println("  Срочных объявлений: " + urgentAfter);
        System.out.println("  Несрочных объявлений: " + nonUrgentAfter);

        if (hasUrgent && hasNonUrgentAgain) {
            System.out.println("✅ После выбора 'Все' отображаются и срочные, и несрочные объявления");
        } else if (hasUrgent && !hasNonUrgentAgain) {
            System.out.println("⚠️ Отображаются только срочные объявления (возможно, на странице нет несрочных)");
        } else if (!hasUrgent && hasNonUrgentAgain) {
            System.out.println("⚠️ Отображаются только несрочные объявления (возможно, на странице нет срочных)");
        }

        System.out.println("✅ Тест-кейс 4 завершен");
    }

    // ========== ДОПОЛНИТЕЛЬНЫЙ ТЕСТ: Фильтр "Обычный" ==========

    @Test(priority = 2)
    public void testNormalFilter() {
        System.out.println("=== Дополнительный тест: Фильтр 'Обычный' ===");

        WebElement urgentSelect = wait.until(ExpectedConditions.presenceOfElementLocated(URGENT_SELECT));
        Select select = new Select(urgentSelect);

        // Выбираем "Обычный"
        select.selectByValue(NORMAL_URGENCY);
        System.out.println("Выбран фильтр: Обычный");
        waitForUpdate();

        List<WebElement> cards = driver.findElements(AD_CARD);
        System.out.println("Найдено объявлений после фильтра 'Обычный': " + cards.size());

        boolean hasUrgent = false;
        int normalCount = 0;

        for (WebElement card : cards) {
            List<WebElement> priorityBadges = card.findElements(AD_PRIORITY);
            if (!priorityBadges.isEmpty()) {
                hasUrgent = true;
                System.out.println("❌ Найдено объявление с меткой 'Срочно'");
            } else {
                normalCount++;
            }
        }

        if (hasUrgent) {
            Assert.fail("❌ БАГ: при фильтре 'Обычный' отображаются срочные объявления");
        } else {
            System.out.println("✅ Все отображаемые объявления не имеют метки 'Срочно'. Найдено обычных: " + normalCount);
        }

        // Возвращаем "Все"
        select.selectByValue(ALL_URGENCY);
        System.out.println("Фильтр сброшен на 'Все'");

        System.out.println("✅ Тест завершен");
    }

    // ========== ВСПОМОГАТЕЛЬНЫЙ МЕТОД ==========

    private void waitForUpdate() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        wait.until(ExpectedConditions.presenceOfElementLocated(AD_CARD));
    }
}