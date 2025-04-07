package wang.jinjing.editor.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import wang.jinjing.common.repository.AbstractBaseRepositoryImpl;
import wang.jinjing.editor.mapper.EditorTeamMemberMapper;
import wang.jinjing.editor.pojo.entity.EditorTeamMember;
import wang.jinjing.editor.repository.EditorTeamMemberRepository;

@Repository
public class EditorTeamMemberRepositoryImpl extends
        AbstractBaseRepositoryImpl<EditorTeamMember, EditorTeamMemberMapper>
        implements EditorTeamMemberRepository {

    @Autowired
    public EditorTeamMemberRepositoryImpl(EditorTeamMemberMapper mapper) {
        super(mapper);
    }

    /**
     * @param id
     */
    @Override
    public int deleteByTeamId(Long id) {
        QueryWrapper<EditorTeamMember> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(EditorTeamMember::getTeamId, id);
        return mapper.delete(wrapper);
    }

    /**
     * @param id
     * @return
     */
    @Override
    public long countByTeamId(Long id) {
        QueryWrapper<EditorTeamMember> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(EditorTeamMember::getTeamId, id);
        return mapper.selectCount(wrapper);
    }

    @Override
    public EditorTeamMember selectByBoth(Long teamId, Long userId) {
        QueryWrapper<EditorTeamMember> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(EditorTeamMember::getTeamId, teamId);
        wrapper.lambda().eq(EditorTeamMember::getUserId, userId);
        return mapper.selectOne(wrapper);
    }
}
