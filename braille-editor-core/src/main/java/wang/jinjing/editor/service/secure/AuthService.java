package wang.jinjing.editor.service.secure;

import org.springframework.security.core.userdetails.UserDetails;
import wang.jinjing.editor.pojo.VO.EditorUserVO;
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


    /**
     * 修改密码
     */
    int changePassword(String username, String newPassword, String oldPassword);

    /**
     * 重置密码
     */
    int resetPassword(String username, String newPassword, String emailCode);

}
