package wang.jinjing.editor.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import wang.jinjing.common.pojo.entity.BaseEntity;
import wang.jinjing.editor.pojo.enums.SysRoleEnum;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;


@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Builder
@TableName("editor_user")
public class EditorUser extends BaseEntity implements UserDetails {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("nickname")
    private String nickname;

    @TableField("sys_role")
    private SysRoleEnum sysRole;

    @TableField("password")
    private String password;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("register_at")
    private Date registerAt;

    @TableField("last_login_at")
    private Date lastLoginAt;

    @TableField("storage_quota")
    private Long storageQuota;

    @TableField(value = "account_non_expired", fill= FieldFill.INSERT)
    private Boolean accountNonExpired = true;

    @TableField(value = "account_non_locked",fill = FieldFill.INSERT)
    private Boolean accountNonLocked = true;

    @TableField(value = "credentials_non_expired",fill = FieldFill.INSERT)
    private Boolean credentialsNonExpired = true;

    @TableField(value = "enabled", fill = FieldFill.INSERT)
    private Boolean enabled = true;

    public EditorUser() {

    }

    // 实现 UserDetails 接口方法
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(this.sysRole.getValue()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public SysRoleEnum getSysRole() {
        return sysRole;
    }

    public void setSysRole(SysRoleEnum sysRole) {
        this.sysRole = sysRole;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getRegisterAt() {
        return registerAt;
    }

    public void setRegisterAt(Date registerAt) {
        this.registerAt = registerAt;
    }

    public Date getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Date lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Long getStorageQuota() {
        return storageQuota;
    }

    public void setStorageQuota(Long storageQuota) {
        this.storageQuota = storageQuota;
    }
}