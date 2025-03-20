package wang.jinjing.editor.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import wang.jinjing.editor.pojo.entity.EditorUser;

import java.util.List;

public interface UserBaseService{

    int addUser(EditorUser editorUser);

    EditorUser findById(Long id);

    EditorUser findByUsername(String username);

    int updateUser(EditorUser editorUser);

    int deleteUser(Long id);

    int addBatch(List<EditorUser> t);

    List<EditorUser> findByIds(Long id, Sort sort);

    Page<EditorUser> findByIdsPaged(int page, int size, Long id, Sort sort);

    int updateBatch(List<EditorUser> t);

    int deleteBatch(List<Long> ids);
}
