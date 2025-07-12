package utils;

import java.io.*;
import java.net.*;
import java.util.Properties;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class Translator {
    private static final String ENDPOINT = "https://rapid-translate-multi-traduction.p.rapidapi.com/t";
    private static String API_KEY = null;

    static {
        try (InputStream input = Translator.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                API_KEY = prop.getProperty("RAPID_API_KEY");
            } else {
                System.err.println("config.properties file not found in classpath");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String translateToEnglish(String text) throws IOException {
        URL url = new URL(ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("x-rapidapi-host", "rapid-translate-multi-traduction.p.rapidapi.com");
        conn.setRequestProperty("x-rapidapi-key", API_KEY);

        String payload = "{\"from\":\"es\",\"to\":\"en\",\"q\":\"" + text + "\"}";
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int status = conn.getResponseCode();
        InputStream responseStream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(responseStream));
        if (jsonElement.isJsonArray()) {
            JsonArray arr = jsonElement.getAsJsonArray();
            if (arr.size() > 0) {
                return arr.get(0).getAsString();
            }
        }
        throw new IOException("Unexpected translation API response: " + jsonElement);
    }
} 