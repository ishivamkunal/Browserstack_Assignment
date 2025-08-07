// ✅ Simplified ElPaisScraper.java with fallback XPaths from config
package scraper;

import model.Article;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.Utils;

import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.stream.IntStream;

public class ElPaisScraper {
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties props = new Properties();

    private static final int MAX_RETRY = 3;
    private static final int RETRY_DELAY = 2000;

    static {
        try (InputStream input = ElPaisScraper.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) props.load(input);
        } catch (Exception ignored) {}
    }

    private static String get(String key, String def) {
        return props.getProperty(key, def);
    }

    private static int getInt(String key, int def) {
        return Integer.parseInt(props.getProperty(key, String.valueOf(def)));
    }

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    private final String BASE_URL = get("BASE_URL", "https://elpais.com/");
    private final String OPINION_XPATH = get("OPINION_LINK_XPATH", "//*[@id='csw']/div[1]/nav/div/a[2]");
    private final String MOBILE_OPINION_XPATH = get("MOBILE_OPINION_LINK_XPATH", "//*[@id='hamburger_container']/nav/div[1]/ul/li[2]/a");
    private final String ARTICLE_BLOCK_XPATH = get("ARTICLE_BLOCK_XPATH", "//main//article[.//h2/a[contains(@href, '/opinion/')]]");
    private final String ARTICLE_LINK_XPATH = get("ARTICLE_LINK_XPATH", "//h2/a");
    private final String TITLE_TAG = get("ARTICLE_TITLE_TAG", "h1");
    private final String PARAGRAPH_XPATH = get("ARTICLE_PARAGRAPH_XPATH", "//article//p");
    private final String IMAGE_XPATH = get("ARTICLE_IMAGE_XPATH", "//article//img");
    private final String IMAGE_FOLDER = get("IMAGE_FOLDER", "images/");
    private final int ARTICLE_COUNT = getInt("ARTICLE_COUNT", 5);
    private final int WAIT_TIMEOUT = getInt("WAIT_TIMEOUT", 10);
    private final int AFTER_CLICK_WAIT = getInt("DYNAMIC_WAIT_AFTER_CLICK", 3);
    private final List<String> FALLBACK_XPATHS = Arrays.asList((PARAGRAPH_XPATH + ";" +
            "//article//div[contains(@class,'article-body')]//p;" +
            "//div[@data-testid='article-content']//p;" +
            "//article//div[contains(@class,'content') or contains(@class,'text') or contains(@class,'body')]//p")
            .split("\\s*;\\s*"));

    public ElPaisScraper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT));
        this.js = (JavascriptExecutor) driver;
    }

    public List<String> scrapeArticleTitles() {
        List<Article> articles = scrapeFirstNOpinionArticles();
        List<String> titles = new ArrayList<>();
        for (Article a : articles) titles.add(a.getTitle());
        return titles;
    }

    public List<Article> scrapeFirstNOpinionArticles() {
        List<Article> articles = new ArrayList<>();
        driver.get(BASE_URL);
        try { driver.manage().window().maximize(); } catch (Exception ignored) {}

        handleCookie();
        navigateToOpinion();

        Set<String> links = fetchLinks();
        int index = 1;
        for (String url : links) {
            try {
                Article article = scrapeArticle(url, index);
                articles.add(article);
                Thread.sleep(AFTER_CLICK_WAIT * 1000L);
                index++;
            } catch (Exception ignored) {}
        }
        return articles;
    }

    private void handleCookie() {
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.id("didomi-notice-agree-button")));
            js.executeScript("arguments[0].click();", btn);
        } catch (Exception ignored) {}
    }

    private void navigateToOpinion() {
        String ua = js.executeScript("return navigator.userAgent").toString().toLowerCase();
        boolean isMobile = ua.contains("android") || ua.contains("iphone") || ua.contains("ipad");
        try {
            if (isMobile) {
                js.executeScript("arguments[0].click();",
                    wait.until(ExpectedConditions.elementToBeClickable(By.id("btn_open_hamburger"))));
                Thread.sleep(1000);
                js.executeScript("arguments[0].click();",
                    wait.until(ExpectedConditions.elementToBeClickable(By.xpath(MOBILE_OPINION_XPATH))));
            } else {
                js.executeScript("arguments[0].click();",
                    wait.until(ExpectedConditions.elementToBeClickable(By.xpath(OPINION_XPATH))));
            }
        } catch (Exception e) {
            throw new RuntimeException("Navigation to Opinion failed", e);
        }
        try { Thread.sleep(AFTER_CLICK_WAIT * 1000L); } catch (InterruptedException ignored) {}
    }

    private Set<String> fetchLinks() {
        Set<String> links = new LinkedHashSet<>();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(ARTICLE_BLOCK_XPATH)));
        IntStream.rangeClosed(1, ARTICLE_COUNT).forEach(i -> {
            String xpath = "(" + ARTICLE_BLOCK_XPATH + ")[" + i + "]" + ARTICLE_LINK_XPATH;
            try {
                WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
                String href = el.getAttribute("href");
                if (href != null && !href.isBlank()) links.add(href);
            } catch (Exception ignored) {}
        });
        return links;
    }

    private Article scrapeArticle(String url, int imageIndex) {
        int attempt = 0;
        while (attempt++ < MAX_RETRY) {
            try {
                driver.get(url);
                handleCookie();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName(TITLE_TAG)));

                String title = driver.findElement(By.tagName(TITLE_TAG)).getText();
                String content = extractContent();

                String imageUrl = null;
                try {
                    WebElement img = driver.findElement(By.xpath(IMAGE_XPATH));
                    imageUrl = img.getAttribute("src");
                    if (imageUrl != null && !imageUrl.isBlank()) {
                        String fileName = IMAGE_FOLDER + "image" + imageIndex + ".jpg";
                        Utils.downloadImage(imageUrl, fileName);
                    }
                } catch (NoSuchElementException ignored) {}

                return new Article(title, content, imageUrl);

            } catch (Exception e) {
                try { Thread.sleep(RETRY_DELAY); } catch (InterruptedException ignored) {}
            }
        }
        throw new RuntimeException("Failed to scrape article after retries: " + url);
    }

    private String extractContent() {
        for (String xpath : FALLBACK_XPATHS) {
            try {
                List<WebElement> paras = driver.findElements(By.xpath(xpath));
                StringBuilder content = new StringBuilder();
                for (WebElement p : paras) content.append(p.getText()).append("\n");
                if (content.length() > 100) return content.toString();
            } catch (Exception ignored) {}
        }
        try {
            WebElement article = driver.findElement(By.tagName("article"));
            String text = article.getText();
            if (text.length() > 100) return text;
        } catch (Exception ignored) {}
        return "[NO CONTENT FOUND]";
    }
}
