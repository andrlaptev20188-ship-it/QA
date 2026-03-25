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
import java.util.ArrayList;
import java.util.List;

public class SortingTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String BASE_URL = "https://cerulean-praline-8e5aa6.netlify.app/";

    // ========== ЛОКАТОРЫ ==========
    // Карточки объявлений
    private static final By AD_CARD = By.className("_card_15fhn_2");
    private static final By AD_PRICE = By.className("_card__price_15fhn_241");

    // Блок пагинации для ожидания загрузки
    private static final By PAGINATION_INFO = By.className("_pagination__info_a1c3m_93");

    // ========== ЛОКАТОРЫ ДЛЯ СОРТИРОВКИ ==========
    // Выпадающий список "Сортировать по" (первый)
    private static final By SORT_SELECT = By.cssSelector("._filters__select_1iunh_21");

    // Выпадающий список "Порядок" (второй)
    private static final By ORDER_SELECT = By.xpath("(//select[@class='_filters__select_1iunh_21'])[2]");

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

        // Ждем загрузки объявлений
        wait.until(ExpectedConditions.presenceOfElementLocated(AD_CARD));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ========== ТЕСТ-КЕЙС 2.1: Сортировка по цене (по возрастанию) ==========

    @Test(priority = 1)
    public void testSortByPriceAscending() {
        System.out.println("=== Тест: Сортировка по цене (по возрастанию) ===");

        // Шаг 1: Выбираем сортировку по цене (первый select)
        WebElement sortSelect = driver.findElement(SORT_SELECT);
        Select sortDropdown = new Select(sortSelect);
        sortDropdown.selectByValue("price");

        // Шаг 2: Выбираем порядок "По возрастанию" (второй select)
        WebElement orderSelect = driver.findElement(ORDER_SELECT);
        Select orderDropdown = new Select(orderSelect);
        orderDropdown.selectByValue("asc");

        // Ждем обновления списка
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Шаг 3: Получаем цены и проверяем порядок
        List<WebElement> cards = driver.findElements(AD_CARD);
        List<Integer> prices = new ArrayList<>();

        for (WebElement card : cards) {
            prices.add(extractPrice(card));
        }

        System.out.println("Получено объявлений: " + prices.size());
        System.out.println("Цены: " + prices);

        // Проверяем, что цены отсортированы по возрастанию
        for (int i = 0; i < prices.size() - 1; i++) {
            Assert.assertTrue(prices.get(i) <= prices.get(i + 1),
                    String.format("❌ Цены не отсортированы по возрастанию: %d > %d",
                            prices.get(i), prices.get(i + 1)));
        }

        System.out.println("✅ Сортировка по цене (возрастание) работает корректно");
    }

    // ========== ТЕСТ-КЕЙС 2.2: Сортировка по цене (по убыванию) ==========

    @Test(priority = 2)
    public void testSortByPriceDescending() {
        System.out.println("=== Тест: Сортировка по цене (по убыванию) ===");

        // Шаг 1: Выбираем сортировку по цене (первый select)
        WebElement sortSelect = driver.findElement(SORT_SELECT);
        Select sortDropdown = new Select(sortSelect);
        sortDropdown.selectByValue("price");

        // Шаг 2: Выбираем порядок "По убыванию" (второй select)
        WebElement orderSelect = driver.findElement(ORDER_SELECT);
        Select orderDropdown = new Select(orderSelect);
        orderDropdown.selectByValue("desc");

        // Ждем обновления списка
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Шаг 3: Получаем цены и проверяем порядок
        List<WebElement> cards = driver.findElements(AD_CARD);
        List<Integer> prices = new ArrayList<>();

        for (WebElement card : cards) {
            prices.add(extractPrice(card));
        }

        System.out.println("Получено объявлений: " + prices.size());
        System.out.println("Цены: " + prices);

        // Проверяем, что цены отсортированы по убыванию
        for (int i = 0; i < prices.size() - 1; i++) {
            Assert.assertTrue(prices.get(i) >= prices.get(i + 1),
                    String.format("❌ Цены не отсортированы по убыванию: %d < %d",
                            prices.get(i), prices.get(i + 1)));
        }

        System.out.println("✅ Сортировка по цене (убывание) работает корректно");
    }

    // ========== ТЕСТ-КЕЙС 2.3: Сортировка по цене после применения фильтра ==========

    @Test(priority = 3)
    public void testSortByPriceAfterFilter() {
        System.out.println("=== Тест: Сортировка по цене после применения фильтра ===");

        // Шаг 1: Применяем фильтр по цене
        WebElement fromInput = driver.findElement(By.cssSelector("input._filters__input_1iunh_20[placeholder='От']"));
        WebElement toInput = driver.findElement(By.cssSelector("input._filters__input_1iunh_20[placeholder='До']"));

        fromInput.clear();
        fromInput.sendKeys("5000");
        toInput.clear();
        toInput.sendKeys("50000");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Шаг 2: Выбираем сортировку по цене (первый select)
        WebElement sortSelect = driver.findElement(SORT_SELECT);
        Select sortDropdown = new Select(sortSelect);
        sortDropdown.selectByValue("price");

        // Шаг 3: Выбираем порядок "По возрастанию" (второй select)
        WebElement orderSelect = driver.findElement(ORDER_SELECT);
        Select orderDropdown = new Select(orderSelect);
        orderDropdown.selectByValue("asc");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Шаг 4: Проверяем порядок цен
        List<WebElement> cards = driver.findElements(AD_CARD);
        List<Integer> prices = new ArrayList<>();

        for (WebElement card : cards) {
            prices.add(extractPrice(card));
        }

        System.out.println("После фильтрации и сортировки получено объявлений: " + prices.size());
        System.out.println("Цены: " + prices);

        // Проверяем, что цены отсортированы по возрастанию
        for (int i = 0; i < prices.size() - 1; i++) {
            Assert.assertTrue(prices.get(i) <= prices.get(i + 1),
                    String.format("❌ Цены не отсортированы по возрастанию: %d > %d",
                            prices.get(i), prices.get(i + 1)));
        }

        // Проверяем, что цены в диапазоне фильтра
        for (int price : prices) {
            Assert.assertTrue(price >= 5000 && price <= 50000,
                    String.format("❌ Цена %d выходит за пределы фильтра (5000-50000)", price));
        }

        System.out.println("✅ Сортировка по цене после фильтрации работает корректно");
    }

    // ========== ВСПОМОГАТЕЛЬНЫЙ МЕТОД ==========

    private int extractPrice(WebElement card) {
        try {
            WebElement priceElement = card.findElement(AD_PRICE);
            String priceText = priceElement.getText()
                    .replaceAll("[^0-9]", "")
                    .trim();
            return priceText.isEmpty() ? 0 : Integer.parseInt(priceText);
        } catch (Exception e) {
            return 0;
        }
    }
}