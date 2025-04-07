package wang.jinjing.editor.repository;

import wang.jinjing.common.repository.BaseRepository;
import wang.jinjing.editor.pojo.entity.EditorTeamMember;

public interface EditorTeamMemberRepository extends BaseRepository<EditorTeamMember> {

    int deleteByTeamId(Long id);

    long countByTeamId(Long id);

    EditorTeamMember selectByBoth(Long teamId, Long userId);
}
