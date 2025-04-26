package wang.jinjing.editor.service;

import org.springframework.lang.NonNull;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.editor.util.SecurityUtils;

public abstract class AbstractUserService {

    @NonNull
    protected EditorUser getCurrentUser(){
        return SecurityUtils.getCurrentUser();
    }


}
