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

    private static String JWT_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ7XCJ1c2VybmFtZVwiOlwiYWRtaW4xXCIsXCJuaWNrbmFtZVwiOlwi57O757uf566h55CG5ZGYMVwiLFwiYXV0aG9yaXRpZXNcIjpbXCJzdXBlcl9hZG1pblwiXX0iLCJpYXQiOjE3NDQ4MDkzNTgsImV4cCI6MTc0NDg5NTc1OH0.0C1y36TnZRMO5o2MbBBEuDnTkXaviix6FZ3QmwrWzxnHh2uU9Sv2VjxwrMeGtWD3tOR8mMVAlsFtm7amFybTTQ";
    private static String USER_NAME  = "admin1";

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