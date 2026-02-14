package helpers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Random;

public class DataHelper {
    private static final Gson gson = new Gson();
    private static final Random rng = new Random();

    public static JsonObject loadTestData(String file) {
        return gson.fromJson(new InputStreamReader(
            Objects.requireNonNull(DataHelper.class.getClassLoader().getResourceAsStream(file))
        ), JsonObject.class);
    }

    public static String randomString(int len) {
        String c = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(c.charAt(rng.nextInt(c.length())));
        return sb.toString();
    }

    public static String randomEmail() { return "test_" + randomString(6) + "@test.com"; }
}
