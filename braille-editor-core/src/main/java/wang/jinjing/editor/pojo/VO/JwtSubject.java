package wang.jinjing.editor.pojo.VO;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import wang.jinjing.common.pojo.VO.BaseVO;

import java.util.Collection;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtSubject extends BaseVO {

    private String username;

    private String nickname;

    private List<String> authorities;

    public JwtSubject(String username, String nickname, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.nickname = nickname;
        this.authorities = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
}