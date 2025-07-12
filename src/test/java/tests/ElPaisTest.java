package tests;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import model.Article;
import scraper.ElPaisScraper;
import utils.BrowserStackCapabilities;
import utils.Translator;
import utils.Utils;

public class ElPaisTest {
    
    private WebDriver driver;
    private ElPaisScraper scraper;
    
    @Parameters({"browser", "os", "osVersion", "browserVersion"})
    @BeforeMethod
    public void setUp(String browser, String os, String osVersion, String browserVersion) throws Exception {
        DesiredCapabilities caps;
        
        // Set capabilities based on browser type
        switch (browser.toLowerCase()) {
            case "chrome":
                if (os.equalsIgnoreCase("android")) {
                    caps = BrowserStackCapabilities.getAndroidChromeCapabilities();
                } else {
                    caps = BrowserStackCapabilities.getCapabilities(browser, os, osVersion, browserVersion);
                }
                break;
            case "firefox":
                caps = BrowserStackCapabilities.getWindowsFirefoxCapabilities();
                break;
            case "safari":
                if (os.equalsIgnoreCase("ios")) {
                    caps = BrowserStackCapabilities.getIOSSafariCapabilities();
                } else {
                    caps = BrowserStackCapabilities.getMacSafariCapabilities();
                }
                break;
            default:
                caps = BrowserStackCapabilities.getWindowsChromeCapabilities();
        }
        
        // Create RemoteWebDriver for BrowserStack
        driver = new RemoteWebDriver(new URL(BrowserStackCapabilities.getBrowserStackUrl()), caps);
        scraper = new ElPaisScraper(driver);
        
        System.out.println("[INFO] Starting test on BrowserStack - " + os + " " + osVersion + " " + browser + " " + browserVersion);
    }
    
    @Test
    public void testScrapeAndTranslate() {
        try {
            // Scrape articles
            List<Article> articles = scraper.scrapeFirstNOpinionArticles();
            
            for (int i = 0; i < articles.size(); i++) {
                Article article = articles.get(i);
                
                Assert.assertNotNull(article.getTitle(), "Article title should not be null");
                Assert.assertFalse(article.getTitle().trim().isEmpty(), "Article title should not be empty");
                System.out.println("[ASSERT] Article " + (i + 1) + " title: " + article.getTitle());
                
                Assert.assertNotNull(article.getContent(), "Article content should not be null");
                Assert.assertFalse(article.getContent().trim().isEmpty(), "Article content should not be empty");
                Assert.assertFalse(article.getContent().equals("[NO CONTENT FOUND]"), "Article content should not be fallback message");
                System.out.println("[ASSERT] Article " + (i + 1) + " content length: " + article.getContent().length() + " characters");
                
                if (article.getImageUrl() != null && !article.getImageUrl().trim().isEmpty()) {
                    String imageFileName = "images/image" + (i + 1) + ".jpg";
                    File imageFile = new File(imageFileName);
                    Assert.assertTrue(imageFile.exists(), "Image file should exist: " + imageFileName);
                    Assert.assertTrue(imageFile.length() > 0, "Image file should not be empty: " + imageFileName);
                    System.out.println("[ASSERT] Article " + (i + 1) + " image: " + imageFileName + " (" + imageFile.length() + " bytes)");
                } else {
                    System.out.println("[INFO] Article " + (i + 1) + " has no image");
                }
                
                // Translate title
                try {
                    String translatedTitle = Translator.translateToEnglish(article.getTitle());
                    Assert.assertNotNull(translatedTitle, "Translated title should not be null");
                    Assert.assertFalse(translatedTitle.trim().isEmpty(), "Translated title should not be empty");
                    System.out.println("Original: " + article.getTitle());
                    System.out.println("Translated: " + translatedTitle);
                } catch (Exception e) {
                    System.err.println("[WARNING] Translation failed for title: " + article.getTitle() + " - " + e.getMessage());
                }
            }
           
            String allContent = articles.stream()
                .map(Article::getContent)
                .collect(Collectors.joining(" "));
            
            Map<String, Integer> wordCount = Utils.analyzeRepeatedWords(allContent);
            System.out.println("Repeated words (more than twice):");
            wordCount.entrySet().stream()
                .filter(entry -> entry.getValue() > 2)
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue() + " times"));
            
            // Final assertion - should have scraped at least 4 articles successfully
            Assert.assertTrue(articles.size() >= 4, "Should have scraped at least 4 articles, got: " + articles.size());
            System.out.println("[SUCCESS] Successfully scraped " + articles.size() + " articles");
            
        } catch (Exception e) {
            System.err.println("[ERROR] Test failed: " + e.getMessage());
            throw e;
        }
    }
    
    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
                System.out.println("[INFO] Browser session closed");
            } catch (Exception e) {
                System.err.println("[WARNING] Error closing browser: " + e.getMessage());
            }
        }
    }
} 