package wang.jinjing.editor.util;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import wang.jinjing.common.exception.ServiceException;
import wang.jinjing.editor.pojo.entity.EditorUser;

import javax.security.sasl.AuthenticationException;

@Component
public class SecurityUtils {

    /**
     * 返回当前登录认证
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录用户
     */
    @NonNull
    public static EditorUser getCurrentUser() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw  new ServiceException("用户未登录或使用公共方法");
}
        Object principal = authentication.getPrincipal();
        if (principal instanceof EditorUser) {
            return (EditorUser) principal;
        }

        throw new IllegalStateException("未知的用户凭证类型");
    }
}