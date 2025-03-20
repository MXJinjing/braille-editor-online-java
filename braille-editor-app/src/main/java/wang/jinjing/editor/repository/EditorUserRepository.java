package wang.jinjing.editor.repository;

import wang.jinjing.common.repository.BaseRepository;
import wang.jinjing.editor.pojo.entity.EditorUser;

public interface EditorUserRepository extends BaseRepository<EditorUser> {

    EditorUser selectByUsername(String username);

    boolean existsByUsername(String username);
}
