package utils;

import org.openqa.selenium.remote.DesiredCapabilities;
import java.util.HashMap;
import java.util.Map;

public class BrowserStackCapabilities {

    private static final String BROWSERSTACK_USERNAME = "shivamkunal_jfTmJw";
    private static final String BROWSERSTACK_ACCESS_KEY = "RS2WkGMMDvjdsbxfECp8";
    private static final String BROWSERSTACK_URL = "https://" + BROWSERSTACK_USERNAME + ":" + BROWSERSTACK_ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";

    public static DesiredCapabilities getWindowsChromeCapabilities() {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", "chrome");
        caps.setCapability("browserVersion", "latest");
        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("os", "Windows");
        bstackOptions.put("osVersion", "10");
        bstackOptions.put("userName", BROWSERSTACK_USERNAME);
        bstackOptions.put("accessKey", BROWSERSTACK_ACCESS_KEY);
        bstackOptions.put("sessionName", "ElPais Windows Chrome");
        bstackOptions.put("buildName", "ElPais Build");
        bstackOptions.put("debug", true);
        bstackOptions.put("consoleLogs", "info");
        bstackOptions.put("networkLogs", true);
        bstackOptions.put("video", true);
        bstackOptions.put("idleTimeout", 300);
        caps.setCapability("bstack:options", bstackOptions);
        return caps;
    }

    public static DesiredCapabilities getWindowsFirefoxCapabilities() {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", "firefox");
        caps.setCapability("browserVersion", "latest");
        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("os", "Windows");
        bstackOptions.put("osVersion", "10");
        bstackOptions.put("userName", BROWSERSTACK_USERNAME);
        bstackOptions.put("accessKey", BROWSERSTACK_ACCESS_KEY);
        bstackOptions.put("sessionName", "ElPais Windows Firefox");
        bstackOptions.put("buildName", "ElPais Build");
        bstackOptions.put("debug", true);
        bstackOptions.put("consoleLogs", "info");
        bstackOptions.put("networkLogs", true);
        bstackOptions.put("video", true);
        bstackOptions.put("idleTimeout", 300);
        caps.setCapability("bstack:options", bstackOptions);
        return caps;
    }

    public static DesiredCapabilities getMacSafariCapabilities() {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", "safari");
        caps.setCapability("browserVersion", "latest");
        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("os", "OS X");
        bstackOptions.put("osVersion", "Big Sur");
        bstackOptions.put("userName", BROWSERSTACK_USERNAME);
        bstackOptions.put("accessKey", BROWSERSTACK_ACCESS_KEY);
        bstackOptions.put("sessionName", "ElPais macOS Safari");
        bstackOptions.put("buildName", "ElPais Build");
        bstackOptions.put("debug", true);
        bstackOptions.put("consoleLogs", "info");
        bstackOptions.put("networkLogs", true);
        bstackOptions.put("video", true);
        bstackOptions.put("idleTimeout", 300);
        caps.setCapability("bstack:options", bstackOptions);
        return caps;
    }

    public static DesiredCapabilities getAndroidChromeCapabilities() {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", "chrome");
        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("deviceName", "Samsung Galaxy S22");
        bstackOptions.put("osVersion", "12.0");
        bstackOptions.put("userName", BROWSERSTACK_USERNAME);
        bstackOptions.put("accessKey", BROWSERSTACK_ACCESS_KEY);
        bstackOptions.put("sessionName", "ElPais Android Chrome");
        bstackOptions.put("buildName", "ElPais Build");
        bstackOptions.put("debug", true);
        bstackOptions.put("consoleLogs", "info");
        bstackOptions.put("networkLogs", true);
        bstackOptions.put("video", true);
        bstackOptions.put("idleTimeout", 300);
        caps.setCapability("bstack:options", bstackOptions);
        return caps;
    }

    public static DesiredCapabilities getIOSSafariCapabilities() {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", "safari");
        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("deviceName", "iPhone 14");
        bstackOptions.put("osVersion", "16");
        bstackOptions.put("userName", BROWSERSTACK_USERNAME);
        bstackOptions.put("accessKey", BROWSERSTACK_ACCESS_KEY);
        bstackOptions.put("sessionName", "ElPais iOS Safari");
        bstackOptions.put("buildName", "ElPais Build");
        bstackOptions.put("debug", true);
        bstackOptions.put("consoleLogs", "info");
        bstackOptions.put("networkLogs", true);
        bstackOptions.put("video", true);
        bstackOptions.put("idleTimeout", 300);
        caps.setCapability("bstack:options", bstackOptions);
        return caps;
    }

    public static DesiredCapabilities getCapabilities(String browser, String os, String osVersion, String browserVersion) {
        // Fallback for custom config, not used in testng.xml
        if (browser.equalsIgnoreCase("chrome") && os.equalsIgnoreCase("windows")) {
            return getWindowsChromeCapabilities();
        } else if (browser.equalsIgnoreCase("firefox") && os.equalsIgnoreCase("windows")) {
            return getWindowsFirefoxCapabilities();
        } else if (browser.equalsIgnoreCase("safari") && os.equalsIgnoreCase("os x")) {
            return getMacSafariCapabilities();
        } else if (browser.equalsIgnoreCase("chrome") && os.equalsIgnoreCase("android")) {
            return getAndroidChromeCapabilities();
        } else if (browser.equalsIgnoreCase("safari") && os.equalsIgnoreCase("ios")) {
            return getIOSSafariCapabilities();
        } else {
            return getWindowsChromeCapabilities();
        }
    }

    public static String getBrowserStackUrl() {
        return BROWSERSTACK_URL;
    }
} 