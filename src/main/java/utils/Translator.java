package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Translator {

    private static final String CONFIG_FILE = "config.properties";
    private static final String ENDPOINT = "https://rapid-translate-multi-traduction.p.rapidapi.com/t";
    private static final String API_KEY;

    static {
        Properties prop = new Properties();
        try (InputStream input = Translator.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                prop.load(input);
                API_KEY = prop.getProperty("RAPID_API_KEY", "");
            } else {
                throw new RuntimeException("config.properties file not found in classpath");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load config.properties", ex);
        }
    }

    public static String translateToEnglish(String text) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(ENDPOINT).openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("x-rapidapi-host", "rapid-translate-multi-traduction.p.rapidapi.com");
        conn.setRequestProperty("x-rapidapi-key", API_KEY);

        String payload = String.format("{\"from\":\"es\",\"to\":\"en\",\"q\":\"%s\"}", text);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int status = conn.getResponseCode();
        InputStream responseStream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(responseStream));
        if (jsonElement.isJsonArray()) {
            JsonArray arr = jsonElement.getAsJsonArray();
            if (!arr.isEmpty()) {
                return arr.get(0).getAsString();
            }
        }
        throw new IOException("Unexpected translation API response: " + jsonElement);
    }
} 
