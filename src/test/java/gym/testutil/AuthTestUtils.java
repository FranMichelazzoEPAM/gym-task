package gym.testutil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class AuthTestUtils {

    private AuthTestUtils() {
    }

    public static String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;

        return "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}