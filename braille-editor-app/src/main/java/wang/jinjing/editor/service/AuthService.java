package wang.jinjing.editor.service;

import org.springframework.security.core.userdetails.UserDetails;
import wang.jinjing.editor.pojo.VO.LoginResponseVO;

public interface AuthService {

    /**
     * 登录
     */
    public LoginResponseVO authenticateLogin(String username, String password);

    /**
     * 刷新token
     */
    public LoginResponseVO refreshToken(String token);

    /**
     * 验证token
     */
    public boolean verifyToken(String token);

    /**
     * 获取用户信息
     */
    public UserDetails getUserInfo(String token);


}
