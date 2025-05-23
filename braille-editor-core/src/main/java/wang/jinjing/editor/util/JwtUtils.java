package wang.jinjing.editor.util;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.core.util.Json;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import wang.jinjing.editor.pojo.VO.JwtSubject;
import wang.jinjing.editor.pojo.entity.EditorUser;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration.ms}")
    private int expirationMs = 300;

    private  SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成JWT token
     * @param authentication 认证信息
     * @return JWT token
     */
    public  String generateJwtToken(Authentication authentication) {
        EditorUser userPrincipal = (EditorUser) authentication.getPrincipal();
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();
        JwtSubject jwtSubject = new JwtSubject(userPrincipal.getUsername(), userPrincipal.getAuthorities());
        String jsonStr = JSONUtil.toJsonStr(jwtSubject);

        return Jwts.builder()
                .subject(jsonStr)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(SignatureAlgorithm.HS512,getSecretKey())
                .compact();
    }


    /**
     * 从JWT token中获取用户名
     * @param token JWT token
     * @return 用户名
     */
    public  String getUsernameFromJwtToken(String token) {
        String subject = getClaimFromToken(token, Claims::getSubject);
        JwtSubject jwtSubject = JSONUtil.toBean(subject, JwtSubject.class);
        return jwtSubject.getUsername();
    }

    /**
     * 从JWT token中获取过期时间
     * @param token JWT token
     * @return 过期时间
     */
    public  Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private   <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private  Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())  // 使用 verifyWith 替代旧版 setSigningKey
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    /**
     * 验证JWT token 是否有效
     * @param authToken JWT token
     * @return 错误原因(1: token无效 2: token过期 0: token有效)
     */
    public  int validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(getSecretKey()).build().parseClaimsJws(authToken);
            return 0;
        } catch (SignatureException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
            return 1;
        } catch (ExpiredJwtException e) {
            return 2;
        }
    }
}