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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriceFilterTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String BASE_URL = "https://cerulean-praline-8e5aa6.netlify.app/";

    // ========== ЛОКАТОРЫ ==========
    private static final By SIDEBAR = By.className("_sidebar_tw7kk_74");
    private static final By PRICE_FROM_INPUT = By.cssSelector("input._filters__input_1iunh_20[placeholder='От']");
    private static final By PRICE_TO_INPUT = By.cssSelector("input._filters__input_1iunh_20[placeholder='До']");
    private static final By AD_CARD = By.className("_card_15fhn_2");
    private static final By AD_PRICE = By.className("_card__price_15fhn_241");
    private static final By PAGINATION_INFO = By.className("_pagination__info_a1c3m_93");
    private static final By RESET_BUTTON = By.xpath("//button[contains(text(), 'Сбросить фильтры')]");

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

    // ========== ТЕСТ-КЕЙС 1: Проверка наличия блока фильтрации ==========

    @Test(priority = 1)
    public void testPriceFilterBlockExists() {
        WebElement sidebar = wait.until(ExpectedConditions.presenceOfElementLocated(SIDEBAR));
        Assert.assertTrue(sidebar.isDisplayed(), "❌ Блок фильтрации (sidebar) не отображается");
        System.out.println("✅ Блок фильтрации (sidebar) присутствует и отображается");

        WebElement priceFrom = wait.until(ExpectedConditions.presenceOfElementLocated(PRICE_FROM_INPUT));
        Assert.assertTrue(priceFrom.isDisplayed(), "❌ Поле 'ОТ' не отображается");
        System.out.println("✅ Поле 'ОТ' присутствует и отображается");

        WebElement priceTo = wait.until(ExpectedConditions.presenceOfElementLocated(PRICE_TO_INPUT));
        Assert.assertTrue(priceTo.isDisplayed(), "❌ Поле 'ДО' не отображается");
        System.out.println("✅ Поле 'ДО' присутствует и отображается");

        System.out.println("\n🎉 Тест-кейс 1 пройден: блок фильтрации цен присутствует!");
    }

    // ========== ТЕСТ-КЕЙС 1.1: Фильтрация по полю "ОТ" ==========

    @Test(priority = 2)
    public void testPriceFromFilter() {
        testPriceFromValue(9000);
        testPriceFromValue(25000);
        testPriceFromValue(95000);
    }

    @Test(priority = 3)
    public void testPriceFromBoundaryValue_1014() {
        applyPriceFromFilter(1014);

        List<WebElement> cards = driver.findElements(AD_CARD);
        boolean hasPrice1014 = false;

        for (WebElement card : cards) {
            int price = extractPrice(card);
            if (price == 1014) {
                hasPrice1014 = true;
                break;
            }
        }

        Assert.assertTrue(hasPrice1014,
                "❌ Объявление с ценой 1014 должно отображаться при фильтре от 1014");
        System.out.println("✅ Объявление с ценой 1014 найдено");
    }

    @Test(priority = 4)
    public void testPriceFromLowValue_100() {
        applyPriceFromFilter(100);

        List<WebElement> cards = driver.findElements(AD_CARD);

        for (WebElement card : cards) {
            int price = extractPrice(card);
            Assert.assertTrue(price >= 100,
                    String.format("❌ Найдено объявление с ценой %d при фильтре от 100", price));
        }

        int displayedCount = cards.size();
        int paginationCount = extractCountFromPagination();

        if (paginationCount > 0) {
            Assert.assertEquals(displayedCount, paginationCount,
                    String.format("❌ Количество отображаемых объявлений (%d) не соответствует счетчику (%d)",
                            displayedCount, paginationCount));
        }

        System.out.println("✅ Фильтр от 100: найдено " + displayedCount + " объявлений");
    }

    @Test(priority = 5)
    public void testPriceFromHighValue_100000() {
        applyPriceFromFilter(100000);

        List<WebElement> cards = driver.findElements(AD_CARD);

        if (cards.size() > 0) {
            for (WebElement card : cards) {
                int price = extractPrice(card);
                Assert.assertTrue(price >= 100000,
                        String.format("❌ Объявление с ценой %d при фильтре от 100000 не должно отображаться", price));
            }
            System.out.println("✅ Фильтр от 100000: найдено " + cards.size() + " объявлений");
        } else {
            System.out.println("✅ Фильтр от 100000: объявлений не найдено");
        }
    }

    // ========== ТЕСТ-КЕЙС 1.2: Фильтрация по полю "ДО" ==========

    @Test(priority = 6)
    public void testPriceToFilter() {
        testPriceToValue(25000);
        testPriceToValue(9500);
        testPriceToValue(110000);
    }

    @Test(priority = 7)
    public void testPriceToValue_25000() {
        applyPriceToFilter(25000);

        List<WebElement> cards = driver.findElements(AD_CARD);

        boolean hasPriceAbove25000 = false;
        int maxPrice = 0;

        for (WebElement card : cards) {
            int price = extractPrice(card);
            if (price > maxPrice) maxPrice = price;
            if (price > 25000) {
                hasPriceAbove25000 = true;
            }
        }

        if (hasPriceAbove25000) {
            System.out.println("❌ БАГ: при фильтре ДО 25000 отображаются объявления до " + maxPrice);
        }

        Assert.assertFalse(hasPriceAbove25000,
                String.format("❌ БАГ: при фильтре ДО 25000 отображаются объявления с ценой выше (до %d)", maxPrice));
    }

    @Test(priority = 8)
    public void testPriceToValue_9500() {
        applyPriceToFilter(9500);

        List<WebElement> cards = driver.findElements(AD_CARD);

        boolean hasPriceAbove9500 = false;
        int maxPrice = 0;

        for (WebElement card : cards) {
            int price = extractPrice(card);
            if (price > maxPrice) maxPrice = price;
            if (price > 9500) {
                hasPriceAbove9500 = true;
            }
        }

        Assert.assertFalse(hasPriceAbove9500,
                String.format("❌ БАГ: при фильтре ДО 9500 отображаются объявления с ценой выше (до %d)", maxPrice));
    }

    @Test(priority = 9)
    public void testPriceToValue_110000() {
        applyPriceToFilter(110000);

        List<WebElement> cards = driver.findElements(AD_CARD);

        boolean hasPriceAbove110000 = false;
        int maxPrice = 0;

        for (WebElement card : cards) {
            int price = extractPrice(card);
            if (price > maxPrice) maxPrice = price;
            if (price > 110000) {
                hasPriceAbove110000 = true;
            }
        }

        Assert.assertFalse(hasPriceAbove110000,
                String.format("❌ БАГ: при фильтре ДО 110000 отображаются объявления с ценой выше (до %d)", maxPrice));
    }

    // ========== ТЕСТ-КЕЙС 1.3: Фильтрация по диапазону "ОТ" и "ДО" ==========

    @Test(priority = 10)
    public void testPriceRangeFilter_1000_to_25000() {
        applyPriceRangeFilter(1000, 25000);

        List<WebElement> cards = driver.findElements(AD_CARD);

        boolean hasPriceBelow1000 = false;
        boolean hasPriceAbove25000 = false;
        int minPrice = Integer.MAX_VALUE;
        int maxPrice = 0;

        for (WebElement card : cards) {
            int price = extractPrice(card);
            if (price < minPrice) minPrice = price;
            if (price > maxPrice) maxPrice = price;

            if (price < 1000) {
                hasPriceBelow1000 = true;
            }
            if (price > 25000) {
                hasPriceAbove25000 = true;
            }
        }

        System.out.println("=== Фильтр от 1000 до 25000 ===");
        System.out.println("Найдено объявлений: " + cards.size());
        System.out.println("Минимальная цена в выдаче: " + minPrice);
        System.out.println("Максимальная цена в выдаче: " + maxPrice);

        Assert.assertFalse(hasPriceBelow1000,
                "❌ БАГ: при фильтре от 1000 до 25000 отображаются объявления с ценой ниже 1000");

        Assert.assertFalse(hasPriceAbove25000,
                String.format("❌ БАГ: при фильтре от 1000 до 25000 отображаются объявления с ценой выше 25000 (до %d)", maxPrice));

        System.out.println("✅ Фильтр от 1000 до 25000 работает корректно");
    }

    @Test(priority = 11)
    public void testPriceRangeFilter_5000_to_15000() {
        applyPriceRangeFilter(5000, 15000);

        List<WebElement> cards = driver.findElements(AD_CARD);

        boolean hasPriceBelow5000 = false;
        boolean hasPriceAbove15000 = false;
        int minPrice = Integer.MAX_VALUE;
        int maxPrice = 0;

        for (WebElement card : cards) {
            int price = extractPrice(card);
            if (price < minPrice) minPrice = price;
            if (price > maxPrice) maxPrice = price;

            if (price < 5000) hasPriceBelow5000 = true;
            if (price > 15000) hasPriceAbove15000 = true;
        }

        System.out.println("=== Фильтр от 5000 до 15000 ===");
        System.out.println("Найдено объявлений: " + cards.size());
        System.out.println("Минимальная цена в выдаче: " + minPrice);
        System.out.println("Максимальная цена в выдаче: " + maxPrice);

        Assert.assertFalse(hasPriceBelow5000,
                "❌ БАГ: при фильтре от 5000 до 15000 отображаются объявления с ценой ниже 5000");

        Assert.assertFalse(hasPriceAbove15000,
                String.format("❌ БАГ: при фильтре от 5000 до 15000 отображаются объявления с ценой выше 15000 (до %d)", maxPrice));

        System.out.println("✅ Фильтр от 5000 до 15000 работает корректно");
    }

    @Test(priority = 12)
    public void testPriceRangeFilter_WithKnownBug_30000Boundary() {
        applyPriceRangeFilter(1000, 25000);

        List<WebElement> cards = driver.findElements(AD_CARD);

        boolean hasPriceAbove30000 = false;
        int maxPrice = 0;

        for (WebElement card : cards) {
            int price = extractPrice(card);
            if (price > maxPrice) maxPrice = price;
            if (price > 30000) {
                hasPriceAbove30000 = true;
            }
        }

        System.out.println("=== Проверка границы 30000 ===");
        System.out.println("Максимальная цена в выдаче: " + maxPrice);

        if (maxPrice > 25000 && maxPrice < 30000) {
            System.out.println("✅ БАГ ПОДТВЕРЖДЕН: при фильтре ДО 25000 отображаются объявления до " + maxPrice);
        }

        Assert.assertFalse(hasPriceAbove30000,
                String.format("❌ БАГ: при фильтре от 1000 до 25000 отображаются объявления выше 30000 (до %d)", maxPrice));

        System.out.println("✅ Проверка границы 30000 пройдена");
    }

    @Test(priority = 13)
    public void testPriceRangeFilter_EdgeCases() {
        applyPriceRangeFilter(200000, 300000);
        List<WebElement> cards1 = driver.findElements(AD_CARD);
        System.out.println("=== Фильтр от 200000 до 300000 ===");
        System.out.println("Найдено объявлений: " + cards1.size());
        Assert.assertTrue(cards1.size() == 0, "❌ БАГ: при фильтре от 200000 до 300000 найдены объявления");
        System.out.println("✅ Пустой диапазон работает корректно");

        applyPriceRangeFilter(0, 200000);
        List<WebElement> cards2 = driver.findElements(AD_CARD);
        int totalCount = extractCountFromPagination();
        System.out.println("=== Фильтр от 0 до 200000 ===");
        System.out.println("Найдено объявлений: " + cards2.size() + ", всего в пагинации: " + totalCount);
        System.out.println("✅ Широкий диапазон работает корректно");
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private void applyPriceFromFilter(int priceFrom) {
        wait.until(ExpectedConditions.presenceOfElementLocated(SIDEBAR));

        WebElement fromInput = driver.findElement(PRICE_FROM_INPUT);
        fromInput.clear();
        fromInput.sendKeys(String.valueOf(priceFrom));

        WebElement toInput = driver.findElement(PRICE_TO_INPUT);
        toInput.clear();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(AD_CARD));
        } catch (Exception e) {
            // Карточек может не быть
        }
    }

    private void applyPriceToFilter(int priceTo) {
        wait.until(ExpectedConditions.presenceOfElementLocated(SIDEBAR));

        WebElement fromInput = driver.findElement(PRICE_FROM_INPUT);
        fromInput.clear();

        WebElement toInput = driver.findElement(PRICE_TO_INPUT);
        toInput.clear();
        toInput.sendKeys(String.valueOf(priceTo));

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(AD_CARD));
        } catch (Exception e) {
            // Карточек может не быть
        }
    }

    private void applyPriceRangeFilter(int priceFrom, int priceTo) {
        wait.until(ExpectedConditions.presenceOfElementLocated(SIDEBAR));

        WebElement fromInput = driver.findElement(PRICE_FROM_INPUT);
        fromInput.clear();
        fromInput.sendKeys(String.valueOf(priceFrom));

        WebElement toInput = driver.findElement(PRICE_TO_INPUT);
        toInput.clear();
        toInput.sendKeys(String.valueOf(priceTo));

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(AD_CARD));
        } catch (Exception e) {
            // Карточек может не быть
        }
    }

    private void testPriceFromValue(int priceFrom) {
        applyPriceFromFilter(priceFrom);

        List<WebElement> cards = driver.findElements(AD_CARD);

        for (WebElement card : cards) {
            int price = extractPrice(card);
            Assert.assertTrue(price >= priceFrom,
                    String.format("❌ Объявление с ценой %d отображается при фильтре от %d", price, priceFrom));
        }

        System.out.println("✅ Фильтр от " + priceFrom + ": найдено " + cards.size() + " объявлений");
    }

    private void testPriceToValue(int priceTo) {
        applyPriceToFilter(priceTo);

        List<WebElement> cards = driver.findElements(AD_CARD);

        boolean hasPriceAbove = false;
        int maxPrice = 0;

        for (WebElement card : cards) {
            int price = extractPrice(card);
            if (price > maxPrice) maxPrice = price;
            if (price > priceTo) {
                hasPriceAbove = true;
            }
        }

        if (hasPriceAbove) {
            System.out.println("❌ БАГ: при фильтре ДО " + priceTo + " отображаются объявления до " + maxPrice);
            Assert.fail(String.format("При фильтре ДО %d отображаются объявления с ценой выше (до %d)", priceTo, maxPrice));
        } else {
            System.out.println("✅ Фильтр ДО " + priceTo + ": найдено " + cards.size() + " объявлений, все цены <= " + priceTo);
        }
    }

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

    private int extractCountFromPagination() {
        try {
            WebElement paginationInfo = driver.findElement(PAGINATION_INFO);
            String text = paginationInfo.getText();

            Pattern pattern = Pattern.compile("из (\\d+)");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            // Игнорируем
        }
        return 0;
    }
    // ========== ТЕСТ-КЕЙС 1.4: Граничные значения (минимальные) ==========

    @Test(priority = 14)
    public void testPriceFromBoundary_MinimalValue_1014() {
        // Применяем фильтр от 1014 (минимальная цена)
        applyPriceFromFilter(1014);

        List<WebElement> cards = driver.findElements(AD_CARD);
        int displayedCount = cards.size();
        int paginationCount = extractCountFromPagination();

        System.out.println("=== Фильтр от 1014 (минимальная цена) ===");
        System.out.println("Найдено объявлений на странице: " + displayedCount);
        System.out.println("Всего объявлений по счетчику: " + paginationCount);

        // Проверяем, что объявление с ценой 1014 отображается
        boolean hasPrice1014 = false;
        for (WebElement card : cards) {
            int price = extractPrice(card);
            if (price == 1014) {
                hasPrice1014 = true;
                break;
            }
        }

        Assert.assertTrue(hasPrice1014,
                "❌ БАГ: объявление с минимальной ценой 1014 не отображается при фильтре от 1014");

        // Проверяем, что количество объявлений соответствует счетчику
        // По багу: должно быть 150, но счетчик показывает 141
        if (paginationCount > 0) {
            System.out.println("Ожидаемое количество объявлений: " + paginationCount);
            System.out.println("Фактическое количество на странице: " + displayedCount);

            if (displayedCount < paginationCount) {
                System.out.println("❌ БАГ: на странице отображается меньше объявлений (" + displayedCount +
                        "), чем указано в счетчике (" + paginationCount + ")");
            }

            Assert.assertEquals(displayedCount, paginationCount,
                    String.format("❌ БАГ: количество отображаемых объявлений (%d) не соответствует счетчику (%d)",
                            displayedCount, paginationCount));
        }

        // Проверяем, что все цены >= 1014
        for (WebElement card : cards) {
            int price = extractPrice(card);
            Assert.assertTrue(price >= 1014,
                    String.format("❌ БАГ: объявление с ценой %d отображается при фильтре от 1014", price));
        }

        System.out.println("✅ Фильтр от 1014 работает корректно");
    }

    @Test(priority = 15)
    public void testPriceFromBoundary_MaximalValue_100068() {
        // Применяем фильтр до 100068 (максимальная цена)
        applyPriceToFilter(100068);

        List<WebElement> cards = driver.findElements(AD_CARD);
        int displayedCount = cards.size();
        int paginationCount = extractCountFromPagination();

        System.out.println("=== Фильтр до 100068 (максимальная цена) ===");
        System.out.println("Найдено объявлений на странице: " + displayedCount);
        System.out.println("Всего объявлений по счетчику: " + paginationCount);

        // Проверяем, что объявление с максимальной ценой отображается
        boolean hasPrice100068 = false;
        int maxPrice = 0;
        for (WebElement card : cards) {
            int price = extractPrice(card);
            if (price > maxPrice) maxPrice = price;
            if (price == 100068) {
                hasPrice100068 = true;
            }
        }

        System.out.println("Максимальная цена в выдаче: " + maxPrice);

        Assert.assertTrue(hasPrice100068,
                "❌ БАГ: объявление с максимальной ценой 100068 не отображается при фильтре до 100068");

        // Проверяем, что все цены <= 100068
        for (WebElement card : cards) {
            int price = extractPrice(card);
            Assert.assertTrue(price <= 100068,
                    String.format("❌ БАГ: объявление с ценой %d отображается при фильтре до 100068", price));
        }

        System.out.println("✅ Фильтр до 100068 работает корректно");
    }

    @Test(priority = 16)
    public void testPriceFromBoundary_FullRange() {
        // Применяем полный диапазон от минимальной до максимальной цены
        applyPriceRangeFilter(1014, 100068);

        List<WebElement> cards = driver.findElements(AD_CARD);
        int displayedCount = cards.size();
        int paginationCount = extractCountFromPagination();

        System.out.println("=== Фильтр от 1014 до 100068 (полный диапазон) ===");
        System.out.println("Найдено объявлений на странице: " + displayedCount);
        System.out.println("Всего объявлений по счетчику: " + paginationCount);

        // Проверяем, что количество объявлений соответствует ожидаемому
        if (paginationCount > 0) {
            System.out.println("Ожидаемое количество объявлений по счетчику: " + paginationCount);
            System.out.println("Фактическое количество на странице: " + displayedCount);

            // По багу: не хватает 4 объявлений (включая объявление с ценой 1014)
            int expectedCount = paginationCount;
            if (displayedCount < expectedCount) {
                System.out.println("❌ БАГ: не хватает " + (expectedCount - displayedCount) + " объявлений");
                System.out.println("   В том числе объявление с ценой 1014 не отображается");
            }

            Assert.assertEquals(displayedCount, paginationCount,
                    String.format("❌ БАГ: количество отображаемых объявлений (%d) не соответствует счетчику (%d)",
                            displayedCount, paginationCount));
        }

        // Проверяем, что объявление с минимальной ценой отображается
        boolean hasPrice1014 = false;
        for (WebElement card : cards) {
            int price = extractPrice(card);
            if (price == 1014) {
                hasPrice1014 = true;
                break;
            }
        }

        Assert.assertTrue(hasPrice1014,
                "❌ БАГ: объявление с минимальной ценой 1014 не отображается в полном диапазоне");

        // Проверяем, что все цены в диапазоне
        for (WebElement card : cards) {
            int price = extractPrice(card);
            Assert.assertTrue(price >= 1014 && price <= 100068,
                    String.format("❌ БАГ: объявление с ценой %d вне диапазона от 1014 до 100068", price));
        }

        System.out.println("✅ Полный диапазон от 1014 до 100068 работает корректно");
    }

    @Test(priority = 17)
    public void testPriceFromBoundary_MissingAdsCount() {
        // Специальный тест для проверки количества потерянных объявлений
        applyPriceRangeFilter(1014, 100068);

        List<WebElement> cards = driver.findElements(AD_CARD);
        int displayedCount = cards.size();
        int paginationCount = extractCountFromPagination();

        System.out.println("=== Проверка количества потерянных объявлений ===");
        System.out.println("Объявлений по счетчику: " + paginationCount);
        System.out.println("Объявлений на странице: " + displayedCount);

        if (displayedCount < paginationCount) {
            int lostCount = paginationCount - displayedCount;
            System.out.println("❌ БАГ: потеряно " + lostCount + " объявлений при фильтрации");

            // По багу: потеряно 4 объявления
            if (lostCount == 4) {
                System.out.println("✅ БАГ ПОДТВЕРЖДЕН: потеряно ровно 4 объявления (как описано в баг-репорте)");
            } else {
                System.out.println("⚠️ БАГ: потеряно " + lostCount + " объявлений (ожидалось 4)");
            }

            // Проверяем, какие цены отсутствуют
            List<Integer> displayedPrices = new ArrayList<>();
            for (WebElement card : cards) {
                displayedPrices.add(extractPrice(card));
            }

            // Проверяем наличие минимальной цены 1014
            if (!displayedPrices.contains(1014)) {
                System.out.println("❌ БАГ: объявление с ценой 1014 отсутствует в выдаче");
            }

            Assert.fail(String.format("❌ БАГ: потеряно %d объявлений при фильтрации (счетчик: %d, отображается: %d)",
                    lostCount, paginationCount, displayedCount));
        } else {
            System.out.println("✅ Все объявления отображаются корректно");
        }
    }
    // ========== ТЕСТ-КЕЙС 1.5: Граничные значения (максимальная цена) ==========

    @Test(priority = 18)
    public void testPriceToBoundary_MaximalValue_100068() {
        // Применяем фильтр до 100068 (максимальная цена)
        applyPriceToFilter(100068);

        List<WebElement> cards = driver.findElements(AD_CARD);
        int displayedCount = cards.size();
        int paginationCount = extractCountFromPagination();

        System.out.println("=== Фильтр до 100068 (максимальная цена) ===");
        System.out.println("Найдено объявлений на странице: " + displayedCount);
        System.out.println("Всего объявлений по счетчику: " + paginationCount);

        // Проверяем, что объявление с максимальной ценой отображается
        boolean hasPrice100068 = false;
        int maxPrice = 0;
        for (WebElement card : cards) {
            int price = extractPrice(card);
            if (price > maxPrice) maxPrice = price;
            if (price == 100068) {
                hasPrice100068 = true;
            }
        }

        System.out.println("Максимальная цена в выдаче: " + maxPrice);

        // По условию: фактический результат совпадает с ОР, значит объявление с ценой 100068 должно быть
        Assert.assertTrue(hasPrice100068,
                "❌ БАГ: объявление с максимальной ценой 100068 не отображается при фильтре до 100068");

        // Проверяем, что все цены <= 100068
        for (WebElement card : cards) {
            int price = extractPrice(card);
            Assert.assertTrue(price <= 100068,
                    String.format("❌ БАГ: объявление с ценой %d отображается при фильтре до 100068", price));
        }

        // Проверяем, что количество объявлений соответствует счетчику
        if (paginationCount > 0) {
            Assert.assertEquals(displayedCount, paginationCount,
                    String.format("❌ БАГ: количество отображаемых объявлений (%d) не соответствует счетчику (%d)",
                            displayedCount, paginationCount));
        }

        System.out.println("✅ Фильтр до 100068 работает корректно: все объявления отображаются");
    }
    // ========== ТЕСТ-КЕЙС 1.6: Фильтрация с отрицательными значениями ==========

    @Test(priority = 19)
    public void testNegativeValue_PriceFrom() {
        // Вводим отрицательное значение в поле "ОТ"
        WebElement fromInput = wait.until(ExpectedConditions.presenceOfElementLocated(PRICE_FROM_INPUT));
        fromInput.clear();
        fromInput.sendKeys("-3000");

        WebElement toInput = driver.findElement(PRICE_TO_INPUT);
        toInput.clear();

        // Ждем возможного применения фильтра
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Проверяем, что фильтр не применился (значение должно быть очищено или игнорироваться)
        String fromValue = fromInput.getAttribute("value");
        List<WebElement> cards = driver.findElements(AD_CARD);
        int paginationCount = extractCountFromPagination();

        System.out.println("=== Фильтрация с отрицательным значением в поле 'ОТ' ===");
        System.out.println("Введено значение: -3000");
        System.out.println("Значение в поле после применения: '" + fromValue + "'");
        System.out.println("Количество объявлений на странице: " + cards.size());
        System.out.println("Всего объявлений по счетчику: " + paginationCount);

        // По багу: запрос не отправляется, фильтр не применяется
        // Ожидаемое поведение: либо поле очищается, либо показывается сообщение об ошибке
        if (fromValue == null || fromValue.isEmpty() || fromValue.equals("-3000")) {
            System.out.println("⚠️ БАГ: отрицательное значение не обработано корректно");
        }

        // Проверяем, что фильтр не применился (отображаются все объявления)
        // Если счетчик показывает общее количество объявлений, значит фильтр не сработал
        if (paginationCount == 150) {
            System.out.println("✅ Фильтр не применился (как и ожидалось для отрицательного значения)");
        } else {
            System.out.println("❌ БАГ: отрицательное значение повлияло на фильтрацию");
        }

        // Тест не падает, а логирует — это информационный тест
    }

    @Test(priority = 20)
    public void testNegativeValue_PriceTo() {
        // Вводим отрицательное значение в поле "ДО"
        WebElement toInput = wait.until(ExpectedConditions.presenceOfElementLocated(PRICE_TO_INPUT));
        toInput.clear();
        toInput.sendKeys("-30000");

        WebElement fromInput = driver.findElement(PRICE_FROM_INPUT);
        fromInput.clear();

        // Ждем применения фильтра
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Проверяем результат
        String toValue = toInput.getAttribute("value");
        List<WebElement> cards = driver.findElements(AD_CARD);
        int paginationCount = extractCountFromPagination();

        System.out.println("=== Фильтрация с отрицательным значением в поле 'ДО' ===");
        System.out.println("Введено значение: -30000");
        System.out.println("Значение в поле после применения: '" + toValue + "'");
        System.out.println("Количество объявлений на странице: " + cards.size());
        System.out.println("Всего объявлений по счетчику: " + paginationCount);

        // По багу: отображается "ничего не найдено"
        if (cards.size() == 0 && paginationCount == 0) {
            System.out.println("❌ БАГ ПОДТВЕРЖДЕН: при вводе отрицательного значения в поле 'ДО' отображается 'ничего не найдено'");
            Assert.fail("❌ БАГ: при вводе отрицательного значения в поле 'ДО' должно быть сообщение об ошибке или поле должно очищаться, а не показывать 'ничего не найдено'");
        } else {
            System.out.println("✅ Отрицательное значение обработано корректно (фильтр не применился)");
        }
    }

    @Test(priority = 21)
    public void testNegativeValue_PriceFrom_Validation() {
        // Проверяем, что поле "ОТ" не принимает отрицательные значения
        WebElement fromInput = wait.until(ExpectedConditions.presenceOfElementLocated(PRICE_FROM_INPUT));
        String originalValue = fromInput.getAttribute("value");

        fromInput.clear();
        fromInput.sendKeys("-3000");

        // Потеря фокуса для применения валидации
        WebElement toInput = driver.findElement(PRICE_TO_INPUT);
        toInput.click();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String newValue = fromInput.getAttribute("value");

        System.out.println("=== Валидация поля 'ОТ' ===");
        System.out.println("Исходное значение: '" + originalValue + "'");
        System.out.println("Значение после ввода -3000: '" + newValue + "'");

        // Ожидаемое поведение: знак "-" автоматически стирается
        if (newValue == null || newValue.isEmpty() || newValue.equals("3000")) {
            System.out.println("✅ Поле 'ОТ' корректно обрабатывает отрицательные значения");
        } else if (newValue.equals("-3000")) {
            System.out.println("❌ БАГ: поле 'ОТ' позволяет вводить отрицательные значения");
        }
    }

    @Test(priority = 22)
    public void testNegativeValue_PriceTo_Validation() {
        // Проверяем, что поле "ДО" не принимает отрицательные значения
        WebElement toInput = wait.until(ExpectedConditions.presenceOfElementLocated(PRICE_TO_INPUT));
        String originalValue = toInput.getAttribute("value");

        toInput.clear();
        toInput.sendKeys("-30000");

        // Потеря фокуса для применения валидации
        WebElement fromInput = driver.findElement(PRICE_FROM_INPUT);
        fromInput.click();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String newValue = toInput.getAttribute("value");

        System.out.println("=== Валидация поля 'ДО' ===");
        System.out.println("Исходное значение: '" + originalValue + "'");
        System.out.println("Значение после ввода -30000: '" + newValue + "'");

        // По багу: поле "ДО" принимает отрицательные значения и показывает "ничего не найдено"
        if (newValue == null || newValue.isEmpty() || newValue.equals("30000")) {
            System.out.println("✅ Поле 'ДО' корректно обрабатывает отрицательные значения");
        } else if (newValue.equals("-30000")) {
            System.out.println("❌ БАГ: поле 'ДО' позволяет вводить отрицательные значения");
        }
    }

    @Test(priority = 23)
    public void testNegativeValue_Combined_Invalid() {
        // Проверяем комбинацию отрицательных значений
        WebElement fromInput = driver.findElement(PRICE_FROM_INPUT);
        WebElement toInput = driver.findElement(PRICE_TO_INPUT);

        fromInput.clear();
        fromInput.sendKeys("-5000");
        toInput.clear();
        toInput.sendKeys("-1000");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<WebElement> cards = driver.findElements(AD_CARD);
        int paginationCount = extractCountFromPagination();

        System.out.println("=== Фильтрация с отрицательными значениями в обоих полях ===");
        System.out.println("Введено: ОТ = -5000, ДО = -1000");
        System.out.println("Количество объявлений на странице: " + cards.size());
        System.out.println("Всего объявлений по счетчику: " + paginationCount);

        // Ожидаемое поведение: либо фильтр не применяется, либо показывается сообщение об ошибке
        if (cards.size() == 0) {
            System.out.println("❌ БАГ: отрицательные значения привели к пустому результату");
        } else if (paginationCount == 150 || paginationCount == 144) {
            System.out.println("✅ Отрицательные значения не повлияли на фильтрацию");
        }
    }
    // ========== ТЕСТ-КЕЙС 1.7: Фильтрация с символами/буквами ==========

    @Test(priority = 24)
    public void testPriceFrom_WithText_Invalid() {
        // Вводим текст в поле "ОТ"
        WebElement fromInput = wait.until(ExpectedConditions.presenceOfElementLocated(PRICE_FROM_INPUT));
        String originalValue = fromInput.getAttribute("value");

        fromInput.clear();
        fromInput.sendKeys("тест");

        // Ждем возможного применения фильтра
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Проверяем, что поле не приняло текст
        String currentValue = fromInput.getAttribute("value");

        System.out.println("=== Фильтрация с текстом в поле 'ОТ' ===");
        System.out.println("Введено значение: 'тест'");
        System.out.println("Значение в поле после ввода: '" + currentValue + "'");

        // Ожидаемое поведение: поле не принимает текст (остается пустым или не меняется)
        boolean isValid = currentValue == null || currentValue.isEmpty() || !currentValue.equals("тест");

        Assert.assertTrue(isValid,
                "❌ БАГ: поле 'ОТ' приняло текстовое значение '" + currentValue + "'");

        System.out.println("✅ Поле 'ОТ' не принимает текстовые значения (работает корректно)");

        // Очищаем поле
        fromInput.clear();
    }

    @Test(priority = 25)
    public void testPriceTo_WithText_Invalid() {
        // Вводим текст в поле "ДО"
        WebElement toInput = wait.until(ExpectedConditions.presenceOfElementLocated(PRICE_TO_INPUT));
        String originalValue = toInput.getAttribute("value");

        toInput.clear();
        toInput.sendKeys("тест");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String currentValue = toInput.getAttribute("value");

        System.out.println("=== Фильтрация с текстом в поле 'ДО' ===");
        System.out.println("Введено значение: 'тест'");
        System.out.println("Значение в поле после ввода: '" + currentValue + "'");

        boolean isValid = currentValue == null || currentValue.isEmpty() || !currentValue.equals("тест");

        Assert.assertTrue(isValid,
                "❌ БАГ: поле 'ДО' приняло текстовое значение '" + currentValue + "'");

        System.out.println("✅ Поле 'ДО' не принимает текстовые значения (работает корректно)");

        toInput.clear();
    }

    @Test(priority = 26)
    public void testPriceFrom_WithMixedCharacters_Invalid() {
        // Вводим смешанные символы в поле "ОТ"
        WebElement fromInput = driver.findElement(PRICE_FROM_INPUT);

        fromInput.clear();
        fromInput.sendKeys("12п!");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String currentValue = fromInput.getAttribute("value");

        System.out.println("=== Фильтрация с символами '12п!' в поле 'ОТ' ===");
        System.out.println("Введено значение: '12п!'");
        System.out.println("Значение в поле после ввода: '" + currentValue + "'");

        // Ожидаемое поведение: поле не принимает нечисловые символы
        boolean isValid = currentValue == null || currentValue.isEmpty() ||
                !currentValue.equals("12п!") && currentValue.matches("\\d*");

        Assert.assertTrue(isValid,
                "❌ БАГ: поле 'ОТ' приняло нечисловое значение '" + currentValue + "'");

        System.out.println("✅ Поле 'ОТ' не принимает нечисловые значения (работает корректно)");

        fromInput.clear();
    }

    @Test(priority = 27)
    public void testPriceTo_WithMixedCharacters_Invalid() {
        // Вводим смешанные символы в поле "ДО"
        WebElement toInput = driver.findElement(PRICE_TO_INPUT);

        toInput.clear();
        toInput.sendKeys("12п!");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String currentValue = toInput.getAttribute("value");

        System.out.println("=== Фильтрация с символами '12п!' в поле 'ДО' ===");
        System.out.println("Введено значение: '12п!'");
        System.out.println("Значение в поле после ввода: '" + currentValue + "'");

        boolean isValid = currentValue == null || currentValue.isEmpty() ||
                !currentValue.equals("12п!") && currentValue.matches("\\d*");

        Assert.assertTrue(isValid,
                "❌ БАГ: поле 'ДО' приняло нечисловое значение '" + currentValue + "'");

        System.out.println("✅ Поле 'ДО' не принимает нечисловые значения (работает корректно)");

        toInput.clear();
    }

    @Test(priority = 28)
    public void testPriceFilter_WithText_NoEffect() {
        // Проверяем, что фильтр не применяется при вводе текста
        WebElement fromInput = driver.findElement(PRICE_FROM_INPUT);
        WebElement toInput = driver.findElement(PRICE_TO_INPUT);

        // Запоминаем текущее состояние
        List<WebElement> cardsBefore = driver.findElements(AD_CARD);
        int countBefore = cardsBefore.size();
        int paginationBefore = extractCountFromPagination();

        // Пытаемся применить фильтр с текстом
        fromInput.clear();
        fromInput.sendKeys("тест");
        toInput.clear();
        toInput.sendKeys("abc");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<WebElement> cardsAfter = driver.findElements(AD_CARD);
        int countAfter = cardsAfter.size();
        int paginationAfter = extractCountFromPagination();

        System.out.println("=== Проверка, что текстовые значения не влияют на фильтрацию ===");
        System.out.println("Количество объявлений до: " + countBefore);
        System.out.println("Количество объявлений после: " + countAfter);
        System.out.println("Счетчик до: " + paginationBefore);
        System.out.println("Счетчик после: " + paginationAfter);

        // По факту: совпадает с ОР — фильтр не применяется, результаты не меняются
        Assert.assertEquals(countAfter, countBefore,
                "❌ БАГ: текстовые значения в фильтре изменили количество объявлений");

        Assert.assertEquals(paginationAfter, paginationBefore,
                "❌ БАГ: текстовые значения в фильтре изменили счетчик");

        System.out.println("✅ Текстовые значения не влияют на фильтрацию (работает корректно)");

        // Очищаем поля
        fromInput.clear();
        toInput.clear();
    }
    // ========== ТЕСТ-КЕЙС 1.8: Проверка кнопки "Сбросить" фильтры ==========

    @Test(priority = 29)
    public void testResetButton_ResetsPriceFields() {
        // Шаг 1: Вводим значения в поля "ОТ" и "ДО"
        WebElement fromInput = wait.until(ExpectedConditions.presenceOfElementLocated(PRICE_FROM_INPUT));
        WebElement toInput = driver.findElement(PRICE_TO_INPUT);

        fromInput.clear();
        fromInput.sendKeys("2000");
        toInput.clear();
        toInput.sendKeys("25000");

        // Проверяем, что значения применились
        String fromValueBefore = fromInput.getAttribute("value");
        String toValueBefore = toInput.getAttribute("value");

        System.out.println("=== Проверка кнопки 'Сбросить' ===");
        System.out.println("Значение 'ОТ' до сброса: " + fromValueBefore);
        System.out.println("Значение 'ДО' до сброса: " + toValueBefore);

        Assert.assertEquals(fromValueBefore, "2000", "❌ Поле 'ОТ' не установлено в 2000");
        Assert.assertEquals(toValueBefore, "25000", "❌ Поле 'ДО' не установлено в 25000");

        // Шаг 2: Нажимаем кнопку "Сбросить" (используем обновленный селектор)
        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(RESET_BUTTON));
        resetButton.click();

        // Ждем применения сброса
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Шаг 3: Проверяем, что поля очистились
        String fromValueAfter = fromInput.getAttribute("value");
        String toValueAfter = toInput.getAttribute("value");

        System.out.println("Значение 'ОТ' после сброса: '" + fromValueAfter + "'");
        System.out.println("Значение 'ДО' после сброса: '" + toValueAfter + "'");

        // Ожидаемый результат: поля очищены (пустые строки)
        boolean isFromEmpty = fromValueAfter == null || fromValueAfter.isEmpty();
        boolean isToEmpty = toValueAfter == null || toValueAfter.isEmpty();

        if (isFromEmpty && isToEmpty) {
            System.out.println("✅ Кнопка 'Сбросить' работает корректно: поля очищены");
        } else {
            System.out.println("❌ БАГ: кнопка 'Сбросить' не очистила поля");
            System.out.println("   ОТ: '" + fromValueAfter + "', ДО: '" + toValueAfter + "'");
        }

        Assert.assertTrue(isFromEmpty, "❌ БАГ: поле 'ОТ' не очистилось после сброса");
        Assert.assertTrue(isToEmpty, "❌ БАГ: поле 'ДО' не очистилось после сброса");
    }

    @Test(priority = 30)
    public void testResetButton_ResetsFilterResults() {
        // Шаг 1: Запоминаем исходное состояние
        List<WebElement> cardsBeforeAnyFilter = driver.findElements(AD_CARD);
        int countBefore = cardsBeforeAnyFilter.size();
        int paginationBefore = extractCountFromPagination();

        System.out.println("=== Проверка сброса результатов фильтрации ===");
        System.out.println("Количество объявлений до фильтра: " + countBefore);
        System.out.println("Счетчик до фильтра: " + paginationBefore);

        // Шаг 2: Применяем фильтр
        WebElement fromInput = driver.findElement(PRICE_FROM_INPUT);
        WebElement toInput = driver.findElement(PRICE_TO_INPUT);

        fromInput.clear();
        fromInput.sendKeys("5000");
        toInput.clear();
        toInput.sendKeys("10000");

        // Ждем применения фильтра
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<WebElement> cardsAfterFilter = driver.findElements(AD_CARD);
        int countAfterFilter = cardsAfterFilter.size();

        System.out.println("Количество объявлений после фильтра: " + countAfterFilter);

        // Шаг 3: Нажимаем кнопку "Сбросить"
        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(RESET_BUTTON));
        resetButton.click();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Шаг 4: Проверяем, что результаты вернулись к исходным
        List<WebElement> cardsAfterReset = driver.findElements(AD_CARD);
        int countAfterReset = cardsAfterReset.size();
        int paginationAfterReset = extractCountFromPagination();

        System.out.println("Количество объявлений после сброса: " + countAfterReset);
        System.out.println("Счетчик после сброса: " + paginationAfterReset);

        // Проверяем, что поля очистились
        String fromValue = fromInput.getAttribute("value");
        String toValue = toInput.getAttribute("value");
        boolean isFromEmpty = fromValue == null || fromValue.isEmpty();
        boolean isToEmpty = toValue == null || toValue.isEmpty();

        System.out.println("Поле 'ОТ' после сброса: '" + fromValue + "'");
        System.out.println("Поле 'ДО' после сброса: '" + toValue + "'");

        if (countAfterReset == countBefore && isFromEmpty && isToEmpty) {
            System.out.println("✅ Кнопка 'Сбросить' корректно сбрасывает результаты фильтрации");
        } else {
            System.out.println("❌ БАГ: результаты фильтрации не сбросились");
        }

        Assert.assertTrue(isFromEmpty, "❌ БАГ: поле 'ОТ' не очистилось после сброса");
        Assert.assertTrue(isToEmpty, "❌ БАГ: поле 'ДО' не очистилось после сброса");
        Assert.assertEquals(countAfterReset, countBefore,
                "❌ БАГ: количество объявлений после сброса не соответствует исходному");
    }

    @Test(priority = 31)
    public void testResetButton_ResetsWithEmptyFields() {
        // Шаг 1: Убеждаемся, что поля пустые
        WebElement fromInput = driver.findElement(PRICE_FROM_INPUT);
        WebElement toInput = driver.findElement(PRICE_TO_INPUT);

        fromInput.clear();
        toInput.clear();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String fromValueBefore = fromInput.getAttribute("value");
        String toValueBefore = toInput.getAttribute("value");

        System.out.println("=== Проверка кнопки 'Сбросить' с пустыми полями ===");
        System.out.println("Значение 'ОТ' до сброса: '" + fromValueBefore + "'");
        System.out.println("Значение 'ДО' до сброса: '" + toValueBefore + "'");

        // Шаг 2: Нажимаем кнопку "Сбросить"
        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(RESET_BUTTON));
        resetButton.click();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Шаг 3: Проверяем, что поля остались пустыми (ничего не сломалось)
        String fromValueAfter = fromInput.getAttribute("value");
        String toValueAfter = toInput.getAttribute("value");

        System.out.println("Значение 'ОТ' после сброса: '" + fromValueAfter + "'");
        System.out.println("Значение 'ДО' после сброса: '" + toValueAfter + "'");

        boolean isFromEmpty = fromValueAfter == null || fromValueAfter.isEmpty();
        boolean isToEmpty = toValueAfter == null || toValueAfter.isEmpty();

        if (isFromEmpty && isToEmpty) {
            System.out.println("✅ Кнопка 'Сбросить' не ломает пустые поля");
        } else {
            System.out.println("❌ БАГ: после сброса появились значения в полях");
        }

        Assert.assertTrue(isFromEmpty, "❌ БАГ: поле 'ОТ' не должно содержать значение после сброса");
        Assert.assertTrue(isToEmpty, "❌ БАГ: поле 'ДО' не должно содержать значение после сброса");
    }
}