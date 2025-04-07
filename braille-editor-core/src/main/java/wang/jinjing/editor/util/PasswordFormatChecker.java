package wang.jinjing.editor.util;

import org.springframework.beans.factory.annotation.Value;
import wang.jinjing.editor.pojo.enums.PasswordFormatEnum;

import java.util.HashSet;
import java.util.Set;

public class PasswordFormatChecker {

    @Value("${password.format.checker.enabled}")
    private static boolean enabled = true;

    private static final Set<Character> supportedChars;

    private static final Set<String> weakPasswords;

    static {
        supportedChars = new HashSet<>();
        for(char c = 'a'; c <= 'z'; c++) {
            supportedChars.add(c);
        }
        for(char c = 'A'; c <= 'Z'; c++) {
            supportedChars.add(c);
        }
        for(char c = '0'; c <= '9'; c++) {
            supportedChars.add(c);
        }
        String specialChars = "!@#$%^&*()_+";
        for (char c : specialChars.toCharArray()) {
            supportedChars.add(c);
        }

        weakPasswords = new HashSet<>();
        weakPasswords.add("password");
        weakPasswords.add("123456");
        weakPasswords.add("12345678");
        weakPasswords.add("111111");
    }

    public static PasswordFormatEnum checkPasswordFormat(String password) {
        if(!enabled) {
            return PasswordFormatEnum.PERMITTED;
        } else if(password.length() < 8) {
            return PasswordFormatEnum.TOO_SHORT;
        } else if(password.length() > 20) {
            return PasswordFormatEnum.TOO_LONG;
        } else if(weakPasswords.contains(password)) {
            return PasswordFormatEnum.TOO_WEAL;
        } else {
            for (char c : password.toCharArray()) {
                if (!supportedChars.contains(c)) {
                    return PasswordFormatEnum.NOT_SUPPORTED;
                }
            }
            return PasswordFormatEnum.PERMITTED;
        }
    }
}
