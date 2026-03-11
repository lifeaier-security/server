package web.server.api.utility;

import java.security.SecureRandom;
import java.util.Base64;

public class MailVerificationUtility {

    public static String generateToken() {

        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[32]; // 256-bit token
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
