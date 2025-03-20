package wang.jinjing.editor.service.impl.base;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import wang.jinjing.common.exception.UserServiceException;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.editor.pojo.enums.ErrorEnum;
import wang.jinjing.editor.pojo.enums.SysRoleEnum;
import wang.jinjing.editor.repository.EditorUserRepository;
import wang.jinjing.editor.service.EditorUserManageService;
import wang.jinjing.editor.service.impl.manage.AbstractCRUDService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class EditorUserBaseServiceImpl extends AbstractCRUDService<EditorUser, EditorUserRepository>
        implements EditorUserManageService {

    @Autowired
    protected EditorUserBaseServiceImpl(EditorUserRepository repository) {
        super(repository);
    }

    @Override
    public int addOne(EditorUser editorUser) {
        checkUserNotExist(editorUser);
        //检测用户是否为超级管理员
        if (editorUser.getSysRole().equals(SysRoleEnum.SUPER_ADMIN)) {
            throw new UserServiceException(ErrorEnum.CHANGE_SUPER_ADMIN);
        }

        // 保存用户至数据库
        int i = super.addOne(editorUser);
        log.info("add user: {}", editorUser);
        return i;
    }

    @Override
    public EditorUser findById(Long id) {
        EditorUser editorUser = super.findById(id);
        if (Objects.isNull(editorUser)) {
            throw new UserServiceException(ErrorEnum.USER_NOT_FOUND);
        }
        return editorUser;
    }

    @Override
    public int updateOne(Long id, EditorUser editorUser) {
        // 检测ID和editorUser的ID是否一致
        if (!Objects.equals(id, editorUser.getId())) {
            throw new UserServiceException(ErrorEnum.USER_ID_NOT_MATCH);
        }
        checkUserExist(id);

        // 检测用户是否修改为超级管理员
        if (SysRoleEnum.SUPER_ADMIN.equals(editorUser.getSysRole())) {
            throw new UserServiceException(ErrorEnum.CHANGE_SUPER_ADMIN);
        }

        return super.updateOne(id, editorUser);
    }

    @Override
    public int deleteOne(Long id) {
        checkUserExist(id);
        return super.deleteOne(id);
    }

    @Override
    public List<EditorUser> searchList(EditorUser editorUser, Map<String,Object> args, Sort sort) {
        return super.searchList(editorUser, args, sort);
    }

    @Override
    public Page<EditorUser> searchPage(EditorUser editorUser, Map<String,Object> args,  int page, int size, Sort sort) {
        return super.searchPage(editorUser, args, page, size, sort);
    }

    @Override
    public int addBatch(List<EditorUser> t) {
        // 检测用户每个用户是否存在
        for (EditorUser editorUser : t) {
            checkUserNotExist(editorUser);
        }
        repository.insertBatch(t);
        return 0;
    }

    @Override
    public int updateBatch(List<EditorUser> t) {
        return 0;
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        return 0;
    }

    // ===== 私有方法 =====

    private void checkUserNotExist(EditorUser editorUser) {
        // 检测用户的ID是否已经存在
        if (repository.existsById(editorUser.getId())) {
            throw new UserServiceException(ErrorEnum.USER_ID_ALREADY_EXIST);
        }
        // 检测用户的用户名是否已经存在
        else if (repository.existsByUsername(editorUser.getUsername())){
            throw new UserServiceException(ErrorEnum.USER_NAME_ALREADY_EXIST);
        }
    }


    private void checkUserExist(Long id) {
        // 检测用户的ID是否已经存在
        if (!repository.existsById(id)) {
            throw new UserServiceException(ErrorEnum.USER_NOT_FOUND);
        } // 检测用户是否为超级管理员
        else if (repository.selectById(id).getSysRole().equals(SysRoleEnum.SUPER_ADMIN)) {
            throw new UserServiceException(ErrorEnum.CHANGE_SUPER_ADMIN);
        }
    }
}
