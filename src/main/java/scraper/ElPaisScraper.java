package scraper;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import model.Article;
import utils.Utils;

public class ElPaisScraper {
    private static final String CONFIG_FILE = "config.properties";
    private static final String BASE_URL;
    private static final String OPINION_LINK_XPATH;
    private static final String MOBILE_OPINION_LINK_XPATH;
    private static final String ARTICLE_BLOCK_XPATH;
    private static final String ARTICLE_LINK_XPATH;
    private static final String ARTICLE_TITLE_TAG;
    private static final String ARTICLE_PARAGRAPH_XPATH;
    private static final String ARTICLE_IMAGE_XPATH;
    private static final String IMAGE_FOLDER;
    private static final int ARTICLE_COUNT;
    private static final int WAIT_TIMEOUT;
    private static final int DYNAMIC_WAIT_AFTER_CLICK;

    static {
        Properties prop = new Properties();
        try (InputStream input = ElPaisScraper.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                prop.load(input);
            }
        } catch (IOException e) {
            System.err.println("Could not load config.properties, using defaults.");
        }
        BASE_URL = prop.getProperty("BASE_URL", "https://elpais.com/");
        OPINION_LINK_XPATH = prop.getProperty("OPINION_LINK_XPATH", "//div[@class='sm _df']//a[normalize-space()='Opinión']");
        MOBILE_OPINION_LINK_XPATH = prop.getProperty("MOBILE_OPINION_LINK_XPATH", "//*[@id=\"hamburger_container\"]/nav/div[1]/ul/li[2]/a");
        ARTICLE_BLOCK_XPATH = prop.getProperty("ARTICLE_BLOCK_XPATH", "//main//article[.//a[contains(@href, '/opinion/') and not(contains(@href, '/editoriales')) and not(contains(@href, '/tribuna'))]]");
        ARTICLE_LINK_XPATH = prop.getProperty("ARTICLE_LINK_XPATH", ".//a[contains(@href, '/opinion/') and not(contains(@href, '/editoriales')) and not(contains(@href, '/tribuna'))]");
        ARTICLE_TITLE_TAG = prop.getProperty("ARTICLE_TITLE_TAG", "h1");
        ARTICLE_PARAGRAPH_XPATH = prop.getProperty("ARTICLE_PARAGRAPH_XPATH", "//article//p");
        ARTICLE_IMAGE_XPATH = prop.getProperty("ARTICLE_IMAGE_XPATH", "//article//img");
        IMAGE_FOLDER = prop.getProperty("IMAGE_FOLDER", "images/");
        ARTICLE_COUNT = Integer.parseInt(prop.getProperty("ARTICLE_COUNT", "5"));
        WAIT_TIMEOUT = Integer.parseInt(prop.getProperty("WAIT_TIMEOUT", "10"));
        DYNAMIC_WAIT_AFTER_CLICK = Integer.parseInt(prop.getProperty("DYNAMIC_WAIT_AFTER_CLICK", "3"));
    }

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    public ElPaisScraper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT));
        this.js = (JavascriptExecutor) driver;
    }

    private void handleCookiePopup() {
        try {
            WebElement cookieAcceptBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id='didomi-notice-agree-button']")));
            
            js.executeScript("arguments[0].click();", cookieAcceptBtn);
            
            // Wait for popup to disappear
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//*[@id='didomi-notice-agree-button']")));
            
            System.out.println("[INFO] Cookie popup handled successfully");
            
        } catch (TimeoutException e) {
            System.out.println("[INFO] Cookie popup not found or already handled");
        } catch (Exception e) {
            System.err.println("[WARNING] Error handling cookie popup: " + e.getMessage());
        }
    }

    public List<Article> scrapeFirstNOpinionArticles() {
        List<Article> articles = new ArrayList<>();
        try {
            driver.get(BASE_URL);
            
            try {
                driver.manage().window().maximize();
                System.out.println("[INFO] Window maximized for desktop platform");
            } catch (Exception e) {
                System.out.println("[INFO] Window maximize not supported on mobile platform");
            }
            
            handleCookiePopup();
            
            String userAgent = ((JavascriptExecutor) driver).executeScript("return navigator.userAgent;").toString().toLowerCase();
            boolean isMobile = userAgent.contains("android") || userAgent.contains("iphone") || userAgent.contains("ipad");
            
            if (isMobile) {
                // Mobile navigation: Click hamburger menu first, then opinion link
                System.out.println("[INFO] Detected mobile platform, using hamburger menu navigation");
                try {
                    WebElement hamburgerBtn = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[@id='btn_open_hamburger']")));
                    js.executeScript("arguments[0].click();", hamburgerBtn);
                    System.out.println("[INFO] Hamburger menu clicked");
                    
                    // Wait a bit for menu to open
                    try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    
                    WebElement opinionLink = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath(MOBILE_OPINION_LINK_XPATH)));
                    js.executeScript("arguments[0].click();", opinionLink);
                    System.out.println("[INFO] Opinion link clicked on mobile using mobile-specific XPath");
                } catch (Exception e) {
                    System.err.println("[ERROR] Mobile navigation failed: " + e.getMessage());
                    throw e;
                }
            } else {
            
                System.out.println("[INFO] Detected desktop platform, using direct navigation");
                WebElement opinionLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath(OPINION_LINK_XPATH)));
                js.executeScript("arguments[0].click();", opinionLink);
            }
            
            System.out.println("[INFO] Waiting " + DYNAMIC_WAIT_AFTER_CLICK + " seconds after clicking Opinion link...");
            try { Thread.sleep(DYNAMIC_WAIT_AFTER_CLICK * 1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            
            Set<String> articleLinks = fetchOpinionArticleLinks();
            int imageIndex = 1;
            
            for (String articleUrl : articleLinks) {
                try {
                    Article article = scrapeArticle(articleUrl, imageIndex);
                    articles.add(article);
                    imageIndex++;
                    
                    System.out.println("[INFO] Waiting " + DYNAMIC_WAIT_AFTER_CLICK + " seconds after scraping article...");
                    try { Thread.sleep(DYNAMIC_WAIT_AFTER_CLICK * 1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    
                } catch (StaleElementReferenceException se) {
                    System.err.println("[ERROR] Stale element for article: " + articleUrl + " - " + se.getMessage());
                } catch (Exception e) {
                    System.err.println("[ERROR] Failed to scrape article: " + articleUrl + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to scrape opinion articles: " + e.getMessage());
        }
        return articles;
    }

    private Set<String> fetchOpinionArticleLinks() {
        Set<String> uniqueLinks = new LinkedHashSet<>();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(ARTICLE_BLOCK_XPATH)));
        
        for (int i = 1; i <= ARTICLE_COUNT; i++) {
            String indexedBlockXpath = "(" + ARTICLE_BLOCK_XPATH + ")[" + i + "]" + ARTICLE_LINK_XPATH;
            try {
                WebElement articleLinkElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath(indexedBlockXpath)));
                String href = articleLinkElement.getAttribute("href");
                if (href != null && !href.trim().isEmpty()) {
                    uniqueLinks.add(href);
                    System.out.println("[INFO] Found article link " + i + ": " + href);
                }
            } catch (TimeoutException e) {
                System.err.println("[WARNING] Article link " + i + " not found within timeout");
            } catch (Exception e) {
                System.err.println("[ERROR] Error fetching article link " + i + ": " + e.getMessage());
            }
        }
        return uniqueLinks;
    }

    private Article scrapeArticle(String url, int imageIndex) {
        int maxRetries = 3;
        int attempt = 0;
        
        while (attempt < maxRetries) {
            try {
                driver.get(url);
                
                handleCookiePopup();
                
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName(ARTICLE_TITLE_TAG)));
                
                String title = driver.findElement(By.tagName(ARTICLE_TITLE_TAG)).getText();
                String content = extractArticleContent();
                
                String imageUrl = null;
                try {
                    WebElement imageElement = driver.findElement(By.xpath(ARTICLE_IMAGE_XPATH));
                    imageUrl = imageElement.getAttribute("src");
                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        String fileName = IMAGE_FOLDER + "image" + imageIndex + ".jpg";
                        Utils.downloadImage(imageUrl, fileName);
                        System.out.println("[INFO] Downloaded image for article " + imageIndex);
                    }
                } catch (NoSuchElementException e) {
                    System.out.println("[INFO] No image found for article " + imageIndex);
                }
                
                System.out.println("[INFO] Successfully scraped article: " + title);
                System.out.println("[DEBUG] Content length: " + content.length() + " characters");
                return new Article(title, content, imageUrl);
                
            } catch (StaleElementReferenceException se) {
                attempt++;
                if (attempt >= maxRetries) {
                    System.err.println("[ERROR] Stale element for article after " + maxRetries + " attempts: " + url + " - " + se.getMessage());
                    throw se;
                }
                System.err.println("[WARNING] Stale element, retrying (" + attempt + "/" + maxRetries + "): " + url);
                try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxRetries) {
                    System.err.println("[ERROR] Failed to scrape article after " + maxRetries + " attempts: " + url + " - " + e.getMessage());
                    throw e;
                }
                System.err.println("[WARNING] Error scraping article, retrying (" + attempt + "/" + maxRetries + "): " + url + " - " + e.getMessage());
                try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); } 
            }
        }
        
        throw new RuntimeException("Failed to scrape article after " + maxRetries + " attempts: " + url);
    }

    private String extractArticleContent() {
        StringBuilder content = new StringBuilder();
        
        try {
            List<WebElement> paragraphElements = driver.findElements(By.xpath(ARTICLE_PARAGRAPH_XPATH));
            for (WebElement paragraphElement : paragraphElements) {
                String paragraphText = paragraphElement.getText();
                if (paragraphText != null && !paragraphText.trim().isEmpty()) {
                    content.append(paragraphText).append("\n");
                }
            }
            
            if (content.length() > 100) {
                System.out.println("[DEBUG] Content extracted using default XPath: " + content.length() + " characters");
                return content.toString();
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] Default XPath failed: " + e.getMessage());
        }
        
        try {
            List<WebElement> bodyElements = driver.findElements(By.xpath("//article//div[contains(@class,'article-body')]//p"));
            if (bodyElements.isEmpty()) {
                bodyElements = driver.findElements(By.xpath("//article//div[contains(@class,'article-body')]"));
            }
            
            for (WebElement element : bodyElements) {
                String text = element.getText();
                if (text != null && !text.trim().isEmpty()) {
                    content.append(text).append("\n");
                }
            }
            
            if (content.length() > 100) {
                System.out.println("[DEBUG] Content extracted using article-body: " + content.length() + " characters");
                return content.toString();
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] Article-body XPath failed: " + e.getMessage());
        }
        
        try {
            List<WebElement> testElements = driver.findElements(By.xpath("//div[@data-testid='article-content']//p"));
            if (testElements.isEmpty()) {
                testElements = driver.findElements(By.xpath("//div[@data-testid='article-content']"));
            }
            
            for (WebElement element : testElements) {
                String text = element.getText();
                if (text != null && !text.trim().isEmpty()) {
                    content.append(text).append("\n");
                }
            }
            
            if (content.length() > 100) {
                System.out.println("[DEBUG] Content extracted using data-testid: " + content.length() + " characters");
                return content.toString();
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] Data-testid XPath failed: " + e.getMessage());
        }
        
        try {
            List<WebElement> divElements = driver.findElements(By.xpath("//article//div[contains(@class,'content') or contains(@class,'text') or contains(@class,'body')]//p"));
            if (divElements.isEmpty()) {
                divElements = driver.findElements(By.xpath("//article//div[contains(@class,'content') or contains(@class,'text') or contains(@class,'body')]"));
            }
            
            for (WebElement element : divElements) {
                String text = element.getText();
                if (text != null && !text.trim().isEmpty()) {
                    content.append(text).append("\n");
                }
            }
            
            if (content.length() > 100) {
                System.out.println("[DEBUG] Content extracted using generic div: " + content.length() + " characters");
                return content.toString();
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] Generic div XPath failed: " + e.getMessage());
        }
        
        try {
            WebElement articleElement = driver.findElement(By.tagName("article"));
            if (articleElement != null) {
                String allText = articleElement.getText();
                if (allText != null && allText.length() > 100) {
                    System.out.println("[DEBUG] Content extracted using all article text: " + allText.length() + " characters");
                    return allText;
                }
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] All article text failed: " + e.getMessage());
        }
        System.err.println("[WARNING] No content found for article: " + driver.getCurrentUrl());
        return "[NO CONTENT FOUND]";
    }
} 