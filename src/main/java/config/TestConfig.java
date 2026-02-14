package config;

public class TestConfig {
    public static final String BASE_URL = System.getProperty("baseUrl", "https://the-internet.herokuapp.com");
    public static final String API_BASE_URL = System.getProperty("apiBaseUrl", "https://jsonplaceholder.typicode.com");
    public static final int TIMEOUT = Integer.parseInt(System.getProperty("timeout", "10"));
    public static final String BROWSER = System.getProperty("browser", "chrome");
    public static final boolean HEADLESS = Boolean.parseBoolean(System.getProperty("headless", "true"));
}
