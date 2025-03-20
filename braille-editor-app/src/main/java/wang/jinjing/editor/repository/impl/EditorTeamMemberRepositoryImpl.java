package wang.jinjing.editor.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import wang.jinjing.common.repository.AbstractBaseRepositoryImpl;
import wang.jinjing.editor.mapper.EditorTeamMapper;
import wang.jinjing.editor.pojo.entity.EditorTeam;
import wang.jinjing.editor.repository.EditorTeamRepository;

@Repository
public class EditorTeamMemberRepositoryImpl extends AbstractBaseRepositoryImpl<EditorTeam, EditorTeamMapper> implements EditorTeamRepository {

    @Autowired
    public EditorTeamMemberRepositoryImpl(EditorTeamMapper mapper) {
        super(mapper);
    }
}
