package wang.jinjing.editor.service.impl.manage;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wang.jinjing.editor.exception.AuthServiceException;
import wang.jinjing.editor.exception.ObjectStorageException;
import wang.jinjing.editor.exception.UserServiceException;
import wang.jinjing.common.util.BeanConvertUtil;
import wang.jinjing.editor.pojo.VO.EditorUserVO;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.common.pojo.ErrorEnum;
import wang.jinjing.editor.pojo.enums.SysRoleEnum;
import wang.jinjing.editor.repository.EditorUserRepository;
import wang.jinjing.common.service.AbstractCRUDService;
import wang.jinjing.editor.service.file.BaseFileService;
import wang.jinjing.editor.service.manage.EditorUserManageService;
import wang.jinjing.editor.service.oss.S3BucketService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EditorUserManageServiceImpl
        extends AbstractCRUDService<EditorUser, EditorUserVO, EditorUserRepository>
        implements EditorUserManageService {

    @Autowired
    private S3BucketService s3BucketService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BaseFileService baseFileService;

    @Autowired
    protected EditorUserManageServiceImpl(EditorUserRepository repository) {
        super(repository,EditorUserVO.class);
    }

    /**
     * 添加一个用户
     * @param editorUser 用户信息
     * @return 返回添加的用户的ID
     */
    @Override
    @Transactional
    public long addOne(EditorUser editorUser) {
        // 为用户分配UUID
        editorUser.setUuid(UUID.randomUUID().toString());

        checkUserNotExist(editorUser);

        //检测用户是否为超级管理员
        if (editorUser.getSysRole().equals(SysRoleEnum.SUPER_ADMIN)) {
            throw new UserServiceException(ErrorEnum.CHANGE_SUPER_ADMIN);
        }

        // 加密密码数据
        String encode = passwordEncoder.encode(editorUser.getPassword());
        editorUser.setPassword(encode);

        // 保存用户至数据库
        long id = super.addOne(editorUser);

        if(id <= 0){
            throw new UserServiceException(ErrorEnum.CREATE_FAIL);
        }

        return id;
    }

    private void cleanupBucket(String bucketName) {
        try {
            if (bucketName != null && s3BucketService.bucketExists(bucketName)) {
                s3BucketService.deleteBucket(bucketName);
            }
        } catch (Exception e) {
            log.error("Bucket cleanup failed: {}", bucketName, e);
        }
    }


    /**
     * 通过ID寻找用户
     *
     * @param id 用户ID
     * @return 返回用户信息
     */
    @Override
    public EditorUserVO findById(Long id) {
        EditorUser editorUser = repository.selectById(id);
        if (Objects.isNull(editorUser)) {
            throw new UserServiceException(ErrorEnum.USER_NOT_FOUND);
        }
        return BeanConvertUtil.convertToVo(voClass,editorUser);
    }

    /**
     * 更新一个用户信息
     * @param id 用户ID
     * @param editorUser 用户信息
     * @return 返回更新的用户数量(0或1)
     */
    @Override
    @Transactional
    public int updateOne(Long id, EditorUser editorUser) {
        // 检测ID和editorUser的ID是否一致
        if (editorUser.getId() != null && !Objects.equals(id, editorUser.getId())) {
            throw new UserServiceException(ErrorEnum.UPDATE_ID_NOT_MATCH);
        }
        checkUserExist(id);
        // 检测用户是否修改为超级管理员
        if (SysRoleEnum.SUPER_ADMIN.equals(editorUser.getSysRole())) {
            throw new UserServiceException(ErrorEnum.CHANGE_SUPER_ADMIN);
        }

        if(editorUser.getPassword() == null){
            // 如果密码没有被修改，则不需要加密
            editorUser.setPassword(null);
        } else {
            // 如果密码被修改，则需要加密
            String encode = passwordEncoder.encode(editorUser.getPassword());
            editorUser.setPassword(encode);
        }

        return super.updateOne(id, editorUser);
    }

    /**
     * 删除一个用户
     * @param id 用户ID
     * @return 返回删除的用户数量(0或1)
     */
    @Override
    @Transactional
    public int deleteOne(Long id) {
        EditorUser user = checkUserExist(id);
        String bucketName = "user-" + user.getUuid();

        try {
            // 强制删除存储桶（忽略不存在情况）
            deleteBucketWithRetry(bucketName, 3);

            // 删除数据库记录
            int deletedCount = super.deleteOne(id);
            if (deletedCount <= 0) {
                throw new UserServiceException(ErrorEnum.DELETE_FAIL);
            }

            return deletedCount;
        } catch (UserServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new UserServiceException(ErrorEnum.DELETE_FAIL, e);
        }
    }


    private void deleteBucketWithRetry(String bucketName, int maxRetries) {
        int attempt = 0;
        boolean b = s3BucketService.bucketExists(bucketName);
        if(!b) {
            log.warn("Bucket {} does not exist, skipping deletion.", bucketName);
            return;
        }
        while (attempt < maxRetries) {
            try {
                s3BucketService.deleteBucket(bucketName);
                return;
            } catch (ObjectStorageException e) {

                if (++attempt >= maxRetries) {
                    throw new UserServiceException(ErrorEnum.BUCKET_DELETE_FAIL, e);
                }

                log.warn("Bucket deletion failed, retrying... (attempt {}/{})", attempt, maxRetries);
                sleep(1000L * attempt);
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }


    /**
     * 搜索用户列表
     *
     * @param editorUser 用户信息
     * @param args       参数
     * @param sort       排序
     * @return 返回用户列表
     */
    @Override
    public List<EditorUserVO> searchList(EditorUser editorUser, Map<String,Object> args, Sort sort) {
        return super.searchList(editorUser, args, sort);
    }

    /**
     * 搜索用户分页
     *
     * @param editorUser 用户信息
     * @param args       参数
     * @param page       分页页码
     * @param size       分页大小
     * @param sort       排序
     * @return 返回用户分页
     */
    @Override
    public Page<EditorUserVO> searchPage(EditorUser editorUser, Map<String,Object> args, int page, int size, Sort sort) {
        return super.searchPage(editorUser, args, page, size, sort);
    }

    /**
     * 批量添加用户
     *
     * @param users 用户列表
     * @return 返回添加的用户数量
     */
    @Override
    @Transactional
    public Map<Integer, ErrorEnum> addBatch(List<EditorUser> users) {
        Map<Integer, ErrorEnum> errorMap = new HashMap<>();
        List<EditorUser> validUsers = new ArrayList<>();
        List<Integer> originalIndices = new ArrayList<>();

        // 预处理 - 生成UUID并过滤非法用户
        for (int i = 0; i < users.size(); i++) {
            EditorUser user = users.get(i);
            if (SysRoleEnum.SUPER_ADMIN.equals(user.getSysRole())) {
                errorMap.put(i, ErrorEnum.CHANGE_SUPER_ADMIN);
                continue;
            }

            // 生成唯一UUID
            user.setUuid(UUID.randomUUID().toString());
            validUsers.add(user);
            originalIndices.add(i);
        }

        // 批量插入数据库
        Map<Integer, Boolean> insertResults = repository.insertBatch(validUsers);
        Map<Integer, EditorUser> successUsers = new HashMap<>();

        insertResults.forEach((validIndex, success) -> {
            int originalIndex = originalIndices.get(validIndex);
            if (success) {
                successUsers.put(originalIndex, validUsers.get(validIndex));
            } else {
                errorMap.put(originalIndex, ErrorEnum.CREATE_FAIL);
            }
        });

        // 创建存储桶并处理失败
        List<Integer> toRemoveFromSuccess = new ArrayList<>();
        successUsers.forEach((originalIndex, user) -> {
            String bucketName = "user-" + user.getUuid();
            try {
                if (s3BucketService.bucketExists(bucketName)) {
                    throw new ObjectStorageException("Bucket already exists");
                }
                s3BucketService.createBucket(bucketName);
            } catch (Exception e) {
                // 记录错误并标记需要删除用户
                errorMap.put(originalIndex, ErrorEnum.BUCKET_CREATE_FAIL);
                toRemoveFromSuccess.add(originalIndex);
                log.error("Bucket creation failed for user {}: {}", user.getUuid(), e.getMessage());
            }
        });

        // 删除存储桶创建失败的用户
        toRemoveFromSuccess.forEach(originalIndex -> {
            EditorUser user = successUsers.get(originalIndex);
            try {
                repository.deleteById(user.getId());
                successUsers.remove(originalIndex);
            } catch (Exception e) {
                log.error("Failed to rollback user {}: {}", user.getUuid(), e.getMessage());
            }
        });

        return errorMap;
    }

    /**
     * 批量更新用户
     *
     * @param t 用户列表
     * @return 删除用户操作的结果
     */
    @Override
    @Transactional
    public Map<Long, ErrorEnum> updateBatch(List<EditorUser> t) {
        Map<Long, ErrorEnum> resultMap = new HashMap<>();

        // 检测UserName是否重复
        Set<String> updatedUserNames = t.stream().map(EditorUser::getUsername).collect(Collectors.toSet());

        List<Long> ids = t.stream().map(EditorUser::getId).collect(Collectors.toList());
        // 查找当前用户信息
        List<EditorUser> editorUsers = repository.selectByIds(ids);

        for (EditorUser editorUser : editorUsers) {
            // 禁止更新超级管理员
            if (SysRoleEnum.SUPER_ADMIN.equals(editorUser.getSysRole())) {
                resultMap.put(editorUser.getId(), ErrorEnum.CHANGE_SUPER_ADMIN);
            }
        }

        for (EditorUser editorUser : t) {
            // 禁止更新超级管理员
            if (SysRoleEnum.SUPER_ADMIN.equals(editorUser.getSysRole())) {
                resultMap.put(editorUser.getId(), ErrorEnum.CHANGE_SUPER_ADMIN);
            }
            // 检测用户名是否在数据库中重复
            if (repository.existsByUsername(editorUser.getUsername())) {
                resultMap.put(editorUser.getId(), ErrorEnum.USER_NAME_ALREADY_EXIST);
            }
            // 检测用户名是否在更新的列表中重复
            if (updatedUserNames.stream().filter(editorUser.getUsername()::equals).count() > 1) {
                resultMap.put(editorUser.getId(), ErrorEnum.USER_NAME_ALREADY_EXIST);
            }
        }
        // 将报错的用户信息从更新列表中删除
        t.removeIf(editorUser -> resultMap.containsKey(editorUser.getId()));

        Map<Long, ErrorEnum> longErrorEnumMap = super.updateBatch(t);

        // 将报错的用户信息添加到结果中
        resultMap.putAll(longErrorEnumMap);
        return resultMap;
    }

    /**
     * 批量删除用户
     * @param ids 用户ID列表
     * @return 删除用户操作的结果
     */
    @Override
    @Transactional
    public Map<Long, ErrorEnum> deleteBatch(List<Long> ids) {
        Map<Long, ErrorEnum> errorMap = new ConcurrentHashMap<>();
        List<Long> allowedIds = new ArrayList<>(ids);

        // 检测用户是否存在
        ids.forEach(this::checkUserExist);

        // 步骤1: 预校验 - 过滤不可删除的用户
        List<EditorUser> users = repository.selectByIdsWithLock(ids); // 悲观锁防止并发修改
        users.forEach(user -> {
            // 禁止删除超级管理员
            if (SysRoleEnum.SUPER_ADMIN.equals(user.getSysRole())) {
                errorMap.put(user.getId(), ErrorEnum.CHANGE_SUPER_ADMIN);
                allowedIds.remove(user.getId());
            }
        });

        // 步骤2: 批量删除存储桶 (失败不影响数据库事务)
        users.parallelStream().forEach(user -> {
            if (!allowedIds.contains(user.getId())) return;

            String bucketName = "user-" + user.getUuid();
            try {
                deleteBucketWithRetry(bucketName, 3); // 带重试的删除
            } catch (Exception e) {
                errorMap.put(user.getId(), ErrorEnum.BUCKET_DELETE_FAIL);
                allowedIds.remove(user.getId()); // 移除此用户ID，避免后续数据库删除
                log.error("Bucket delete failed: {}", bucketName, e);
            }
        });

        // 步骤3: 数据库删除 (仅允许删除存储桶成功的用户)
        Map<Long, ErrorEnum> dbErrors = super.deleteBatch(allowedIds);
        dbErrors.forEach((id, error) -> {
            errorMap.put(id, error);
            allowedIds.remove(id); // 从允许列表中移除失败项
        });

        return errorMap;
    }


    // ===== 非AbstractCrudService中的方法 =====

    @Autowired
    private EditorUserRepository repository;

    /**
     *
     * @param userId
     * @param accountNonExpired
     * @return
     */
    @Override
    public int setAccountNonExpired(Long userId, boolean accountNonExpired) {
        return repository.updateSingle(userId, "accountNonExpired", accountNonExpired);
    }

    /**
     * @param userId
     * @param accountNonLocked
     * @return
     */
    @Override
    public int setAccountNonLocked(Long userId, boolean accountNonLocked) {
        return repository.updateSingle(userId, "accountNonLocked", accountNonLocked);
    }

    /**
     * @param userId
     * @param credentialsNonExpired
     * @return
     */
    @Override
    public int setCredentialsNonExpired(Long userId, boolean credentialsNonExpired) {
        return repository.updateSingle(userId, "credentialsNonExpired", credentialsNonExpired);
    }

    /**
     * @param userId
     * @param enabled
     * @return
     */
    @Override
    public int setEnabled(Long userId, boolean enabled) {
        return repository.updateSingle(userId, "enabled", enabled);
    }


    /**
     * @param userId
     * @param storageQuota
     * @return
     */
    @Override
    public int changeStorageQuota(Long userId, Long storageQuota) {
        return repository.updateSingle(userId, "storage_quota", storageQuota);
    }

    /**
     * @param userId
     * @param newPassword
     * @return
     */
    @Override
    public int changePasswordNotCheck(Long userId, String newPassword) {
//        PasswordFormatEnum passwordFormatEnum = PasswordFormatChecker.checkPasswordFormat(newPassword);
//        if(passwordFormatEnum == PasswordFormatEnum.TOO_SHORT) {
//            throw new AuthServiceException(ErrorEnum.PASSWORD_TOO_SHORT);
//        } else if(passwordFormatEnum == PasswordFormatEnum.TOO_LONG) {
//            throw new AuthServiceException(ErrorEnum.PASSWORD_TOO_LONG);
//        } else if(passwordFormatEnum == PasswordFormatEnum.TOO_WEAL) {
//            throw new AuthServiceException(ErrorEnum.PASSWORD_TOO_WEAK);
//        } else if(passwordFormatEnum == PasswordFormatEnum.NOT_SUPPORTED) {
//            throw new AuthServiceException(ErrorEnum.PASSWORD_NOT_SUPPORTED);
//        }
        String encode = passwordEncoder.encode(newPassword);
        if(repository.updateSingle(userId, "password", encode) > 0) {
            return 1;
        } else {
            throw new AuthServiceException(ErrorEnum.USER_NOT_FOUND);
        }
    }

    @Override
    public void initBucket(Long id) {
        String uuid = repository.selectById(id).getUuid();
        String bucketName = "user-" + uuid;
        baseFileService.initBucket(bucketName);
    }

    // ===== 私有方法 =====

    private void checkUserNotExist(EditorUser editorUser) {
        // 检测用户的ID是否已经存在
        if (editorUser.getId() != null && repository.existsById(editorUser.getId())) {
            throw new UserServiceException(ErrorEnum.USER_ID_ALREADY_EXIST);
        }
        // 检测用户的用户名是否已经存在
        else if (editorUser.getUsername() != null && repository.existsByUsername(editorUser.getUsername())){
            throw new UserServiceException(ErrorEnum.USER_NAME_ALREADY_EXIST);
        }
    }


    private EditorUser checkUserExist(Long id) {
        // 检测用户的ID是否已经存在
        EditorUser editorUser = repository.selectByIdWithLock(id);
        if (Objects.isNull(editorUser)) {
            throw new UserServiceException(ErrorEnum.USER_NOT_FOUND);
        } // 检测用户是否为超级管理员
        else {
            if (editorUser.getSysRole().equals(SysRoleEnum.SUPER_ADMIN)) {
                throw new UserServiceException(ErrorEnum.CHANGE_SUPER_ADMIN);
            }
        }
        return editorUser;
    }
}
