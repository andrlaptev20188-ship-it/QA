import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
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

public class CategoryFilterTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String BASE_URL = "https://cerulean-praline-8e5aa6.netlify.app/";

    // ========== ЛОКАТОРЫ ==========
    private static final By AD_CARD = By.className("_card_15fhn_2");
    private static final By AD_CATEGORY = By.className("_card__category_15fhn_259");
    private static final By PAGINATION_INFO = By.className("_pagination__info_a1c3m_93");

    // Точный XPath для select категории
    private static final By CATEGORY_SELECT = By.xpath("//*[@id=\"root\"]/div/div[2]/aside/div[2]/div[2]/select");

    // Значения категорий
    private static final String ALL_CATEGORIES = "";
    private static final String CATEGORY_ELECTRONICS = "0";
    private static final String CATEGORY_REAL_ESTATE = "1";
    private static final String CATEGORY_TRANSPORT = "2";
    private static final String CATEGORY_JOBS = "3";
    private static final String CATEGORY_SERVICES = "4";
    private static final String CATEGORY_PETS = "5";
    private static final String CATEGORY_FASHION = "6";
    private static final String CATEGORY_KIDS = "7";

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

    // ========== ТЕСТ-КЕЙС 3.1: Фильтр по всем категориям ==========

    @Test(priority = 1)
    public void testFilterAllCategories() {
        System.out.println("=== Тест-кейс 3.1: Фильтр по всем категориям ===");

        List<WebElement> cards = driver.findElements(AD_CARD);
        int totalCount = cards.size();

        System.out.println("Отображается объявлений: " + totalCount);
        Assert.assertTrue(totalCount > 0, "❌ Не найдено ни одного объявления");

        System.out.println("✅ Фильтр 'Все категории' работает корректно");
    }

    // ========== ТЕСТ-КЕЙС 3.2: Фильтр по конкретной категории ==========

    @Test(priority = 2)
    public void testFilterBySpecificCategory() {
        System.out.println("=== Тест-кейс 3.2: Фильтр по конкретной категории ===");

        testSingleCategoryFilter(CATEGORY_FASHION, "Мода");
        testSingleCategoryFilter(CATEGORY_KIDS, "Детское");
        testSingleCategoryFilter(CATEGORY_ELECTRONICS, "Электроника");
    }

    // ========== ТЕСТ-КЕЙС 3.3: Вернуть фильтр в исходное положение ==========

    @Test(priority = 3)
    public void testResetCategoryFilter() {
        System.out.println("=== Тест-кейс 3.3: Вернуть фильтр в исходное положение ===");

        // Шаг 1: Выбираем категорию "Детское"
        selectCategory(CATEGORY_KIDS);
        waitForUpdate();

        List<WebElement> cardsKids = driver.findElements(AD_CARD);
        System.out.println("После выбора категории 'Детское' найдено объявлений: " + cardsKids.size());

        // Проверяем, что все отображаемые объявления — категории "Детское"
        for (WebElement card : cardsKids) {
            String actualCategory = card.findElement(AD_CATEGORY).getText();
            Assert.assertEquals(actualCategory, "Детское",
                    "❌ Найдено объявление категории '" + actualCategory + "' при фильтре по 'Детское'");
        }

        // Шаг 2: Возвращаем "Все категории"
        selectCategory(ALL_CATEGORIES);
        waitForUpdate();

        List<WebElement> cardsAll = driver.findElements(AD_CARD);
        System.out.println("После выбора 'Все категории' найдено объявлений: " + cardsAll.size());

        // Проверяем, что теперь есть объявления разных категорий
        boolean hasDifferentCategories = false;
        String firstCategory = null;
        for (WebElement card : cardsAll) {
            String actualCategory = card.findElement(AD_CATEGORY).getText();
            if (firstCategory == null) {
                firstCategory = actualCategory;
            } else if (!actualCategory.equals(firstCategory)) {
                hasDifferentCategories = true;
                break;
            }
        }

        Assert.assertTrue(hasDifferentCategories,
                "❌ После выбора 'Все категории' отображаются объявления только одной категории (" + firstCategory + ")");

        // Также проверяем, что количество объявлений не меньше, чем было при фильтре
        Assert.assertTrue(cardsAll.size() >= cardsKids.size(),
                String.format("❌ После сброса фильтра количество объявлений (%d) меньше, чем было (%d)",
                        cardsAll.size(), cardsKids.size()));

        System.out.println("✅ Фильтр успешно вернулся в исходное положение: отображаются разные категории");
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private void selectCategory(String categoryValue) {
        // Ждем, что select категории доступен
        WebElement categorySelect = wait.until(ExpectedConditions.presenceOfElementLocated(CATEGORY_SELECT));
        Select select = new Select(categorySelect);
        select.selectByValue(categoryValue);
    }

    private void testSingleCategoryFilter(String categoryValue, String categoryName) {
        System.out.println("\n--- Проверка категории: " + categoryName + " ---");

        selectCategory(categoryValue);
        waitForUpdate();

        List<WebElement> cards = driver.findElements(AD_CARD);
        System.out.println("Найдено объявлений: " + cards.size());

        if (cards.isEmpty()) {
            System.out.println("⚠️ В категории '" + categoryName + "' нет объявлений");
            return;
        }

        for (WebElement card : cards) {
            String actualCategory = card.findElement(AD_CATEGORY).getText();
            Assert.assertEquals(actualCategory, categoryName,
                    String.format("❌ Найдено объявление категории '%s' при фильтре по '%s'",
                            actualCategory, categoryName));
        }

        System.out.println("✅ Фильтр по категории '" + categoryName + "' работает корректно");
    }

    private void waitForUpdate() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        wait.until(ExpectedConditions.presenceOfElementLocated(AD_CARD));
    }
}