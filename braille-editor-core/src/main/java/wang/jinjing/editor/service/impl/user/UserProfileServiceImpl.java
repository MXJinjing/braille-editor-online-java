package wang.jinjing.editor.service.impl.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.jinjing.common.pojo.ErrorEnum;
import wang.jinjing.common.service.BasicCRUDService;
import wang.jinjing.editor.exception.UserServiceException;
import wang.jinjing.editor.pojo.VO.EditorUserVO;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.editor.service.user.UserProfileService;
import wang.jinjing.editor.util.SecurityUtils;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private BasicCRUDService<EditorUser, EditorUserVO> basicUserCRUDService;

    @Override
    public int updateProfile(EditorUser editorUser) {
        // 获取当前用户的的ID
        Long id = SecurityUtils.getCurrentUser().getId();
        if(!editorUser.getId().equals(id)) {
            // 如果传入的ID和当前用户的ID不一致，则抛出异常
            throw new UserServiceException(ErrorEnum.INVALID_USER_ID);
        }
        return basicUserCRUDService.updateOne(editorUser.getId(), editorUser);
    }
}
