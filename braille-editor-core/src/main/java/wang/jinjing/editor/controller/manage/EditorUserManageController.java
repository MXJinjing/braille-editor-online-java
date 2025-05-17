package wang.jinjing.editor.controller.manage;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wang.jinjing.common.controller.AbstractRestfulAPIsController;
import wang.jinjing.common.pojo.VO.OperationResultVO;
import wang.jinjing.common.service.BasicCRUDService;
import wang.jinjing.editor.pojo.DTO.EditorUserDTO;
import wang.jinjing.editor.pojo.VO.EditorUserVO;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.editor.repository.EditorUserRepository;
import wang.jinjing.editor.service.manage.EditorUserManageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/manage/user")
public class EditorUserManageController extends AbstractRestfulAPIsController<
        EditorUserDTO,EditorUser, EditorUserVO, EditorUserRepository, BasicCRUDService<EditorUser,EditorUserVO>> {

    @Autowired
    private EditorUserManageService editorUserManageService;

    public EditorUserManageController(BasicCRUDService<EditorUser,EditorUserVO> crudService) {
        super(crudService);
        setClasses(EditorUser.class, EditorUserVO.class);
    }

    @PutMapping("/{id}/setStatus")
    ResponseEntity<?> setAccountSecureStatus(@PathVariable Long id,
                                             @RequestParam(required = false) Boolean accountNonExpired,
                                             @RequestParam(required = false) Boolean accountNonLocked,
                                             @RequestParam(required = false) Boolean credentialsNonExpired,
                                             @RequestParam(required = false) Boolean enabled) {
        if(Objects.isNull(accountNonExpired)
                && Objects.isNull(accountNonLocked)
                && Objects.isNull(credentialsNonExpired)
                && Objects.isNull(enabled)) {
            return ResponseEntity.badRequest().body("Invalid request");
        } else {
            List<OperationResultVO> result = new ArrayList<>();
            if(!Objects.isNull(accountNonExpired)) {
                int i = editorUserManageService.setAccountNonExpired(id, accountNonExpired);
                if(i>0) result.add(OperationResultVO.success(1));
            }
            if(!Objects.isNull(accountNonLocked)) {
                int i = editorUserManageService.setAccountNonLocked(id, accountNonLocked);
                if(i>0) result.add(OperationResultVO.success(2));
            }
            if(!Objects.isNull(credentialsNonExpired)) {
                int i = editorUserManageService.setCredentialsNonExpired(id, credentialsNonExpired);
                if(i>0) result.add(OperationResultVO.success(3));
            }
            if(!Objects.isNull(enabled)) {
                int i = editorUserManageService.setEnabled(id, enabled);
                if(i>0) result.add(OperationResultVO.success(4));
            }
            return ResponseEntity.ok().body(result);
        }
    }

    @PatchMapping("/changeStorageQuota")
    ResponseEntity<?> changeStorageQuota(@RequestParam Long id, @RequestParam Long storageQuota) {
        int i = editorUserManageService.changeStorageQuota(id, storageQuota);
        return (i > 0)? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("Unknown error");
    }

    @PatchMapping("/changePassword")
    ResponseEntity<?> changePasswordNotCheck(@RequestParam Long id, @RequestParam String password) {
        int i = editorUserManageService.changePassword(id, password,false);
        return (i > 0)? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("Unknown error");
    }

    @PatchMapping("/initBucket")
    ResponseEntity<?> initBucket(@RequestParam Long id) {
       editorUserManageService.initBucket(id);
        return ResponseEntity.ok().build();
    }
}
