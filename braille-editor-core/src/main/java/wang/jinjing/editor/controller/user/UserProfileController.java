package wang.jinjing.editor.controller.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import wang.jinjing.common.util.BeanConvertUtil;
import wang.jinjing.editor.pojo.DTO.EditorUserDTO;
import wang.jinjing.editor.pojo.VO.EditorUserSimpleVO;
import wang.jinjing.editor.pojo.VO.EditorUserVO;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.editor.service.user.UserProfileService;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @PutMapping("")
    public ResponseEntity<?> updateProfile(@RequestBody EditorUserDTO editorUserDTO ) {
        EditorUser editorUser = BeanConvertUtil.convertToEntity(EditorUser.class, editorUserDTO);
        int a = userProfileService.updateProfile(editorUser);
        return a > 0? ResponseEntity.ok().build() : ResponseEntity.noContent().build();
    }

    @GetMapping("")
    public ResponseEntity<?> getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal1 = authentication.getPrincipal();
        if (principal1 instanceof EditorUser editorUser) {
            return ResponseEntity.ok(BeanConvertUtil.convertToVo(EditorUserVO.class,editorUser));
        }else {
            return ResponseEntity.ok(principal1);
        }
    }

    @GetMapping("/simple")
    public ResponseEntity<?> getCurrentUserInfoSimple() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal1 = authentication.getPrincipal();
        if (principal1 instanceof EditorUser editorUser) {
            return ResponseEntity.ok(BeanConvertUtil.convertToVo(EditorUserSimpleVO.class,editorUser));
        }else {
            return ResponseEntity.ok(principal1);
        }
    }
}