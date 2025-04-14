package wang.jinjing.editor.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@SpringBootTest
class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    private static String JWT_TOKEN = "eyJhbGciOiJIUzUxMiJ9." +
            "eyJzdWIiOiJ7fSIsImlhdCI6MTc0NDMwMjIwOCwiZXhwIjoxNzQ0Mzg4NjA4fQ." +
            "Oam731VF4g6s6rI7B0jIpq2AAO5fmsEztwbR3HvnrfS5MneXt8WHLdPZNs78OBtTZlWbt6mz891lXwFj-LraLQ";
    private static String USER_NAME  = "admin2";

    @Test
    void getUsernameFromJwtToken() {
        String username = jwtUtils.getUsernameFromJwtToken(JWT_TOKEN);
        log.info(username);
        assertEquals(USER_NAME, username);
    }

    @Test
    void getExpirationDateFromToken() {
        Date expirationDate = jwtUtils.getExpirationDateFromToken(JWT_TOKEN);
        log.info(expirationDate.toString());
    }

    @Test
    void validateJwtToken() {
        int result = jwtUtils.validateJwtToken(JWT_TOKEN);
        log.info("result: {}", result);
        assertEquals(0, result);
    }
}