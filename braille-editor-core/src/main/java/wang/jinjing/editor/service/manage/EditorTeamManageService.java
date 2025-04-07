package wang.jinjing.editor.service.manage;

import wang.jinjing.editor.pojo.enums.TeamRoleEnum;

public interface EditorTeamManageService {

    int addUserToTeam(Long userId, Long teamId, TeamRoleEnum teamRoleEnum);

    int changeUserRoleInTeam(Long userId, Long teamId, TeamRoleEnum teamRoleEnum);

    int removeUserFromTeam(Long userId, Long teamId);

    int changeTeamOwner(Long ownerUserId, Long teamId);

}
