package utils;

import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BrowserStackCapabilities {

    private static final String CONFIG_FILE = "config.properties";
    private static final String BROWSERSTACK_USERNAME;
    private static final String BROWSERSTACK_ACCESS_KEY;
    private static final String BROWSERSTACK_URL;
    private static final boolean USE_BROWSERSTACK;

    static {
        Properties prop = new Properties();
        try (InputStream input = BrowserStackCapabilities.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                prop.load(input);
            }
        } catch (IOException e) {
            System.err.println("Failed to load config.properties");
        }

        BROWSERSTACK_USERNAME = prop.getProperty("BROWSERSTACK_USERNAME", "");
        BROWSERSTACK_ACCESS_KEY = prop.getProperty("BROWSERSTACK_ACCESS_KEY", "");
        USE_BROWSERSTACK = Boolean.parseBoolean(prop.getProperty("USE_BROWSERSTACK", "false"));
        BROWSERSTACK_URL = "https://" + BROWSERSTACK_USERNAME + ":" + BROWSERSTACK_ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";
    }

    public static boolean isUseBrowserStack() {
        return USE_BROWSERSTACK;
    }

    public static DesiredCapabilities getCapabilities(String browser, String os, String osVersion, String deviceName, String browserVersion, String sessionName) {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", browser);
        if (browserVersion != null && !browserVersion.isEmpty()) {
            caps.setCapability("browserVersion", browserVersion);
        }

        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("userName", BROWSERSTACK_USERNAME);
        bstackOptions.put("accessKey", BROWSERSTACK_ACCESS_KEY);
        bstackOptions.put("sessionName", sessionName);
        bstackOptions.put("buildName", "ElPais Build");
        bstackOptions.put("debug", true);
        bstackOptions.put("consoleLogs", "info");
        bstackOptions.put("networkLogs", true);
        bstackOptions.put("video", true);
        bstackOptions.put("idleTimeout", 300);

        if (deviceName != null && !deviceName.isEmpty()) {
            bstackOptions.put("deviceName", deviceName);
            bstackOptions.put("osVersion", osVersion);
        } else {
            bstackOptions.put("os", os);
            bstackOptions.put("osVersion", osVersion);
        }

        caps.setCapability("bstack:options", bstackOptions);
        return caps;
    }

    public static String getBrowserStackUrl() {
        return BROWSERSTACK_URL;
    }
} 
