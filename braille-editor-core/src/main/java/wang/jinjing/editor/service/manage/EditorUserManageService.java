package wang.jinjing.editor.service.manage;

import jakarta.validation.Valid;

public interface EditorUserManageService{

    int setAccountNonExpired(Long userId, boolean accountNonExpired);

    int setAccountNonLocked(Long userId, boolean accountNonLocked);

    int setCredentialsNonExpired(Long userId, boolean credentialsNonExpired);

    int setEnabled(Long userId, boolean enabled);

    int changeStorageQuota(Long userId, Long storageQuota);

    int changePassword(Long userId, String newPassword, boolean check);

    void initBucket(@Valid Long id);
}
