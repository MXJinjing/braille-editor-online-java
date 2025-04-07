package wang.jinjing.editor.service.impl.secure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.editor.repository.EditorUserRepository;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private EditorUserRepository editorUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EditorUser editorUser = editorUserRepository.selectByUsername(username);
        if (editorUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return editorUser;
    }
}
