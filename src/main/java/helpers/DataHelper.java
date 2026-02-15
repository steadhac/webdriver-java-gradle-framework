package helpers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Random;

/**
 * Utility class for loading test data from JSON files and generating
 * random values for test isolation.
 *
 * Random data ensures each test run uses unique values, preventing
 * conflicts from duplicate usernames, emails, etc.
 *
 * Example usage:
 *
 *   JsonObject data = DataHelper.loadTestData("testdata/users.json");
 *   String email = DataHelper.randomEmail();  // e.g., "test_a7km2x@test.com"
 */
public class DataHelper {

    /** Gson instance for deserializing JSON files. */
    private static final Gson gson = new Gson();

    /** Random number generator for producing random strings and emails. */
    private static final Random rng = new Random();

    /**
     * Loads a JSON file from the classpath (src/test/resources) and parses it
     * into a JsonObject.
     *
     * The file must be on the classpath — typically placed in src/test/resources/.
     * Throws NullPointerException if the file is not found.
     *
     * Example:
     *   // Loads src/test/resources/testdata/users.json
     *   JsonObject data = DataHelper.loadTestData("testdata/users.json");
     *   String name = data.get("name").getAsString();
     *
     * @param file the classpath-relative path to the JSON file
     * @return the parsed JSON content as a JsonObject
     */
    public static JsonObject loadTestData(String file) {
        return gson.fromJson(new InputStreamReader(
            Objects.requireNonNull(DataHelper.class.getClassLoader().getResourceAsStream(file))
        ), JsonObject.class);
    }

    /**
     * Generates a random alphanumeric string of the specified length.
     * Uses lowercase letters (a-z) and digits (0-9).
     *
     * Example:
     *   randomString(8)  // returns something like "k3mf9x2a"
     *
     * @param len the desired string length
     * @return a random alphanumeric string
     */
    public static String randomString(int len) {
        String c = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(c.charAt(rng.nextInt(c.length())));
        return sb.toString();
    }

    /**
     * Generates a random email address in the format test_XXXXXX@test.com,
     * where XXXXXX is a 6-character random alphanumeric string.
     *
     * Useful for creating unique user accounts in tests without collisions.
     *
     * Example:
     *   randomEmail()  // returns something like "test_a7km2x@test.com"
     *
     * @return a unique random email address
     */
    public static String randomEmail() { return "test_" + randomString(6) + "@test.com"; }
}