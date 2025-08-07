package tests;

import model.Article;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.*;
import scraper.ElPaisScraper;
import utils.BrowserStackCapabilities;
import utils.Translator;
import utils.Utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElPaisTest {

    private WebDriver driver;

    @Parameters({"browser", "os", "osVersion", "browserVersion"})
    @BeforeMethod
    public void setUp(String browser, String os, String osVersion, String browserVersion) throws Exception {
        String sessionName = String.format("%s %s %s", os, browser, browserVersion);

        driver = BrowserStackCapabilities.isUseBrowserStack()
                ? new RemoteWebDriver(
                    new URL(BrowserStackCapabilities.getBrowserStackUrl()),
                    BrowserStackCapabilities.getCapabilities(
                        browser, os, osVersion, getDevice(browser, os), browserVersion, sessionName
                    )
                )
                : Utils.getLocalDriver(browser);
    }

    @Test
    public void testScrapeTranslateAnalyze() throws Exception {
        ElPaisScraper scraper = new ElPaisScraper(driver);
        List<Article> articles = scraper.scrapeFirstNOpinionArticles();

        List<String> translatedTitles = new ArrayList<>();

        for (Article article : articles) {
            System.out.println("ES Title: " + article.getTitle());
            System.out.println("ES Content:\n" + article.getContent());

            String translated = Translator.translateToEnglish(article.getTitle());
            translatedTitles.add(translated);

            System.out.println("EN Title: " + translated);
            System.out.println("--");
        }

        Map<String, Integer> repeated = Utils.repeatedWordAnalysis(translatedTitles);
        if (repeated.isEmpty()) {
            System.out.println("No words repeated more than twice.");
        } else {
            System.out.println("Repeated words in translated headers:");
            repeated.forEach((word, count) -> System.out.println(word + ": " + count));
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private String getDevice(String browser, String os) {
        if (os.equalsIgnoreCase("android")) return "Samsung Galaxy S22";
        if (os.equalsIgnoreCase("ios")) return "iPhone 14";
        return null;
    }
}
