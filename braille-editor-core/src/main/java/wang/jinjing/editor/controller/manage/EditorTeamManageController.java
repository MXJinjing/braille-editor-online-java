package wang.jinjing.editor.controller.manage;


import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wang.jinjing.common.controller.RestfulAPIsController;
import wang.jinjing.editor.pojo.DTO.EditorTeamDTO;
import wang.jinjing.editor.pojo.VO.EditorTeamVO;
import wang.jinjing.editor.pojo.entity.EditorTeam;
import wang.jinjing.editor.pojo.enums.TeamRoleEnum;
import wang.jinjing.editor.repository.EditorTeamRepository;
import wang.jinjing.editor.service.manage.EditorTeamManageService;
import wang.jinjing.editor.service.impl.manage.EditorTeamManagerServiceImpl;

@RestController
@RequestMapping("/api/manage/team")
public class EditorTeamManageController extends RestfulAPIsController<
        EditorTeamDTO,EditorTeam, EditorTeamVO, EditorTeamRepository, EditorTeamManagerServiceImpl> {

    private final EditorTeamManageService editorTeamManagerService;

    @Autowired
    public EditorTeamManageController(EditorTeamManagerServiceImpl crudService) {
        super(crudService);
        this.editorTeamManagerService = crudService;
    }


    @PutMapping("/member/{teamId}/{userId}")
    public ResponseEntity<?> addUserToTeam(@PathVariable Long userId,
                             @PathVariable Long teamId,
                             @RequestParam(required = false) String teamRole) {
        TeamRoleEnum teamRoleEnum = TeamRoleEnum.valueOf(teamRole);
        if(StrUtil.isEmpty(teamRole)) {
            teamRoleEnum = TeamRoleEnum.MEMBER;
        }
        int i = editorTeamManagerService.addUserToTeam(userId, teamId, teamRoleEnum);
        if(i == 0){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok().body("id = "+i);
    }


    @PatchMapping("/member/{teamId}/{userId}/team_role/admin")
    public ResponseEntity<?> changeUserRoleToAdminInTeam(@PathVariable Long userId,
                                                  @PathVariable Long teamId) {
        int i = editorTeamManagerService.changeUserRoleInTeam(userId, teamId, TeamRoleEnum.ADMIN);
        if(i == 0){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/member/{teamId}/{userId}/team_role/member")
    public ResponseEntity<?> changeUserRoleToMemberInTeam(@PathVariable Long userId,
                                                         @PathVariable Long teamId) {
        int i = editorTeamManagerService.changeUserRoleInTeam(userId, teamId, TeamRoleEnum.MEMBER);
        if(i == 0){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/member/{teamId}/{userId}")
    public ResponseEntity<?> removeUserFromTeam(@PathVariable Long userId, @PathVariable Long teamId) {
        int i = editorTeamManagerService.removeUserFromTeam(userId, teamId);
        if(i == 0){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/transfer/{teamId}")
    public ResponseEntity<?> changeTeamOwner(@RequestParam("new_owner_id") Long ownerUserId,@ PathVariable Long teamId) {
        int i = editorTeamManagerService.changeTeamOwner(ownerUserId, teamId);
        if(i == 0){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok().build();
    }
}
