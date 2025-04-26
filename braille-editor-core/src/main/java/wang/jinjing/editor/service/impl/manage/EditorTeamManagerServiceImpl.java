package wang.jinjing.editor.service.impl.manage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wang.jinjing.editor.exception.ObjectStorageException;
import wang.jinjing.editor.exception.TeamServiceException;
import wang.jinjing.common.service.AbstractCRUDService;
import wang.jinjing.common.util.BeanConvertUtil;
import wang.jinjing.editor.pojo.VO.EditorTeamVO;
import wang.jinjing.editor.pojo.VO.EditorUserVO;
import wang.jinjing.editor.pojo.entity.EditorTeam;
import wang.jinjing.editor.pojo.entity.EditorTeamMember;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.common.pojo.ErrorEnum;
import wang.jinjing.editor.pojo.enums.TeamRoleEnum;
import wang.jinjing.editor.repository.EditorTeamMemberRepository;
import wang.jinjing.editor.repository.EditorTeamRepository;
import wang.jinjing.editor.repository.EditorUserRepository;
import wang.jinjing.editor.service.manage.EditorTeamManageService;
import wang.jinjing.editor.service.oss.S3BucketService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class EditorTeamManagerServiceImpl
        extends AbstractCRUDService<EditorTeam, EditorTeamVO, EditorTeamRepository>
        implements EditorTeamManageService {

    @Autowired
    private EditorUserRepository userRepository;

    @Autowired
    private EditorTeamMemberRepository teamMemberRepository;

    @Autowired
    private S3BucketService s3BucketService;


    protected EditorTeamManagerServiceImpl(EditorTeamRepository repository) {
        super(repository, EditorTeamVO.class);
    }

    /** 添加一个团队
     * @param editorTeam
     * @return
     */
    @Override
    @Transactional
    public long addOne(EditorTeam editorTeam) {
        // 为团队分配UUID
        editorTeam.setUuid(UUID.randomUUID().toString());
        String teamBucketName = "team-" + editorTeam.getUuid();

        try {
            // 检测用户是否存在
            EditorUser ownerUser = userRepository.selectById(editorTeam.getOwnerId());
            if (ownerUser == null) {
                throw new TeamServiceException(ErrorEnum.TEAM_OWNER_NOT_FOUND);
            }
            // 检查用户账号是否启用
            if (!ownerUser.isEnabled()) {
                throw new TeamServiceException(ErrorEnum.TEAM_OWNER_ACCOUNT_DISABLED);
            }

            // 创建团队数据
            Date current = new Date();
            editorTeam.setCreateAt(current);

            // 为团队分配UUID
            UUID uuid = UUID.nameUUIDFromBytes("editor_team".getBytes());
            editorTeam.setUuid(uuid.toString());

            long teamId = super.addOne(editorTeam);


            // 维护 团队-成员表
            EditorTeamMember editorTeamMember = new EditorTeamMember();
            editorTeamMember.setTeamId(teamId);
            editorTeamMember.setUserId(ownerUser.getId());
            editorTeamMember.setTeamRole(TeamRoleEnum.OWNER);
            editorTeamMember.setJoinAt(current);

            teamMemberRepository.insert(editorTeamMember);

            return teamId;
        } catch (Exception e){
            // 检测是否已经创建了桶
            cleanupBucket(teamBucketName);
            if (e instanceof TeamServiceException) {
                throw (TeamServiceException) e;
            }
            throw new TeamServiceException(ErrorEnum.BUCKET_CREATE_FAIL, e);
        }
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
     * @param id
     * @return
     */
    @Override
    @Transactional
    public EditorTeamVO findById(Long id) {
        EditorTeam team = repository.selectById(id);
        // 检查team是否存在
        if(team == null){
            throw new TeamServiceException(ErrorEnum.TEAM_NOT_FOUND);
        }
        EditorTeamVO vo = BeanConvertUtil.convertToVo(voClass, team);

        // 为VO 添加所有者用户
        EditorUser ownerUser = userRepository.selectById(vo.getOwnerId());

        // 为VO 统计团队成员数量
        long memberCount = teamMemberRepository.countByTeamId(id);

        vo.setOwner(BeanConvertUtil.convertToVo(EditorUserVO.class, ownerUser));
        vo.setCurrentMembers(Math.toIntExact(memberCount));

        return vo;
    }


    /**
     * @param id
     * @param editorTeam
     * @return
     */
    @Override
    @Transactional
    public int updateOne(Long id, EditorTeam editorTeam) {
        // 检测ID 和 editorTeam 是否匹配
        if(!id.equals(editorTeam.getId())){
            throw new TeamServiceException(ErrorEnum.UPDATE_ID_NOT_MATCH);
        }
        checkTeamExist(id);
        // 检测是否更换了所有者？
        EditorTeam team = repository.selectById(id);
        if(!Objects.equals(team.getOwnerId(), editorTeam.getOwnerId())){
            // 不支持的操作（在这里不支持更换所有者）
            throw new TeamServiceException(ErrorEnum.TEAM_CANNOT_CHANGE_OWNER_IN_UPDATE);
        }
        return super.updateOne(id, editorTeam);
    }

    /**
     * @param id
     * @return
     */
    @Override
    @Transactional
    public int deleteOne(Long id) {
        // 1. 获取团队信息并检查存在性
        EditorTeam team = checkTeamExist(id);
        String bucketName = "team-" + team.getUuid();

        try {
            // 2. 强制删除关联存储桶（带重试机制）
            deleteBucketWithRetry(bucketName, 3);

            // 3. 删除数据库记录
            int deletedCount = super.deleteOne(id);
            if (deletedCount <= 0) {
                throw new TeamServiceException(ErrorEnum.DELETE_FAIL);
            }

            return deletedCount;
        } catch (TeamServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new TeamServiceException(ErrorEnum.DELETE_FAIL, e);
        }
    }

    private void deleteBucketWithRetry(String bucketName, int maxRetries) {
        int attempt = 0;
        while (attempt < maxRetries && s3BucketService.bucketExists(bucketName)) {
            try {
                s3BucketService.deleteBucket(bucketName);
                return;
            } catch (ObjectStorageException e) {

                if (++attempt >= maxRetries) {
                    throw new TeamServiceException(ErrorEnum.BUCKET_DELETE_FAIL, e);
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
     * @param editorTeam
     * @param args
     * @param sort
     * @return
     */
    @Override
    public List<EditorTeamVO> searchList(EditorTeam editorTeam, Map<String, Object> args, Sort sort) {
        List<EditorTeamVO> editorTeamVOS = super.searchList(editorTeam, args, sort);

        // 为EditorTeamVOs 添加额外信息
        for (EditorTeamVO vo : editorTeamVOS) {
            // 为VO 添加所有者用户
            EditorUser ownerUser = userRepository.selectById(vo.getOwnerId());

            // 为VO 统计团队成员数量
            long memberCount = teamMemberRepository.countByTeamId(vo.getId());

            vo.setOwner(BeanConvertUtil.convertToVo(EditorUserVO.class, ownerUser));
            vo.setCurrentMembers(Math.toIntExact(memberCount));
        }

        return editorTeamVOS;
    }

    /**
     * @param editorTeam
     * @param args
     * @param page
     * @param size
     * @param sort
     * @return
     */
    @Override
    public Page<EditorTeamVO> searchPage(EditorTeam editorTeam, Map<String, Object> args, int page, int size, Sort sort) {
        return super.searchPage(editorTeam, args, page, size, sort);
    }

    /**
     * 批量添加团队（含存储桶创建）
     * @param teams 团队列表
     * @return 操作结果（key: 原列表下标, value: 错误类型）
     */
    @Override
    @Transactional
    public Map<Integer, ErrorEnum> addBatch(List<EditorTeam> teams) {
        Map<Integer, ErrorEnum> errorMap = new HashMap<>();
        List<EditorTeam> validTeams = new ArrayList<>();
        List<Integer> originalIndices = new ArrayList<>();
        Date current = new Date();

        // 1. 预处理 - 生成UUID并初始化时间
        for (int i = 0; i < teams.size(); i++) {
            EditorTeam team = teams.get(i);
            team.setCreateAt(current);

            // 生成唯一UUID（可根据业务需求调整生成规则）
            team.setUuid(UUID.randomUUID().toString());
            validTeams.add(team);
            originalIndices.add(i);
        }

        // 2. 批量插入数据库
        Map<Integer, Boolean> insertResults = repository.insertBatch(validTeams);
        Map<Integer, EditorTeam> successTeams = new HashMap<>();

        insertResults.forEach((validIndex, success) -> {
            int originalIndex = originalIndices.get(validIndex);
            if (success) {
                successTeams.put(originalIndex, validTeams.get(validIndex));
            } else {
                errorMap.put(originalIndex, ErrorEnum.CREATE_FAIL);
            }
        });

        // 3. 创建存储桶并处理失败
        List<Integer> toRemoveFromSuccess = new ArrayList<>();
        successTeams.forEach((originalIndex, team) -> {
            String bucketName = "team-" + team.getUuid(); // 团队存储桶命名规则
            try {
                // 检查存储桶是否存在
                if (s3BucketService.bucketExists(bucketName)) {
                    throw new ObjectStorageException("Bucket already exists");
                }
                // 创建存储桶
                s3BucketService.createBucket(bucketName);

                // 维护团队-成员关系（原逻辑保留）
                EditorTeamMember member = new EditorTeamMember();
                member.setTeamId(team.getId());
                member.setUserId(team.getOwnerId());
                member.setTeamRole(TeamRoleEnum.OWNER);
                member.setJoinAt(current);
                teamMemberRepository.insert(member);
            } catch (Exception e) {
                // 记录错误并标记需要删除团队
                errorMap.put(originalIndex, ErrorEnum.BUCKET_CREATE_FAIL);
                toRemoveFromSuccess.add(originalIndex);
                log.error("团队存储桶创建失败: {}", bucketName, e);
            }
        });

        // 4. 清理创建失败的团队
        toRemoveFromSuccess.forEach(originalIndex -> {
            EditorTeam team = successTeams.get(originalIndex);
            try {
                // 删除团队记录
                repository.deleteById(team.getId());
                // 删除关联的成员关系
                teamMemberRepository.deleteByTeamId(team.getId());
                successTeams.remove(originalIndex);
            } catch (Exception e) {
                log.error("团队数据回滚失败: teamId={}", team.getId(), e);
            }
        });

        return errorMap;
    }

    /**
     * @param t
     * @return
     */
    @Override
    @Transactional
    public Map<Long, ErrorEnum> updateBatch(List<EditorTeam> t) {
        // 检测所有待修改的团队是否存在
        t.stream().map(EditorTeam::getId).forEach(this::checkTeamExist);

        Map<Long, ErrorEnum> resultMap = new HashMap<>();
        // 检测是否更换了所有者？
        for(EditorTeam team : t){
            EditorTeam currentTeam = repository.selectById(team.getId());
            if(!Objects.equals(team.getOwnerId(), currentTeam.getOwnerId())){
                // 不支持的操作（在这里不支持更换所有者）
                resultMap.put(team.getId(), ErrorEnum.TEAM_CANNOT_CHANGE_OWNER_IN_UPDATE);
            }
        }
        // 更新
        Map<Long, Boolean> longBooleanMap = repository.updateBatch(t);
        longBooleanMap.forEach((k, v) -> {
            if (!v) {
                resultMap.put(k, ErrorEnum.UPDATE_FAIL);
            }
        });
        return resultMap;
    }

    /**
     * @param ids
     * @return
     */
    @Override
    public Map<Long, ErrorEnum> deleteBatch(List<Long> ids) {
        Map<Long, ErrorEnum> errorMap = new ConcurrentHashMap<>();
        List<Long> allowedIds = new ArrayList<>(ids);

        ids.forEach(this::checkTeamExist);
        List<EditorTeam> teams = repository.selectByIdsWithLock(ids); // 悲观锁防止并发修改

        teams.parallelStream().forEach(team -> {
            String bucketName = "team-" + team.getUuid();
            try {
                deleteBucketWithRetry(bucketName,3);
            } catch (Exception e) {
                errorMap.put(team.getId(), ErrorEnum.DELETE_FAIL);
                allowedIds.remove(team.getId());
                log.error("Bucket delete failed: {}", bucketName, e);
            }
        });

        Map<Long, ErrorEnum> dbErrors = super.deleteBatch(allowedIds);
        dbErrors.forEach((id, error) -> {
            errorMap.put(id, error);
            allowedIds.remove(id); // 从允许列表中移除失败项
        });

        return errorMap;
    }



    @Override
    @Transactional
    public int addUserToTeam(Long userId, Long teamId, TeamRoleEnum teamRoleEnum) {
        // 检测团队是否存在
        checkTeamExist(teamId);
        // 检测用户是否存在
        checkUserAvailable(userId);

        // 检查用户是否在团队中
        EditorTeamMember editorTeamMember = teamMemberRepository.selectByBoth(teamId, userId);
        if(!Objects.isNull(editorTeamMember)){
            throw new TeamServiceException(ErrorEnum.USER_ALREADY_IN_TEAM);
        }

        EditorTeamMember newOne = new EditorTeamMember();
        newOne.setTeamId(teamId);
        newOne.setUserId(userId);
        newOne.setTeamRole(teamRoleEnum);

        return teamMemberRepository.insert(newOne);
    }

    @Override
    public int changeUserRoleInTeam(Long userId, Long teamId, TeamRoleEnum teamRoleEnum) {
        // 检测团队是否存在
        checkTeamExist(teamId);
        // 检测用户是否存在
        checkUserAvailable(userId);

        // 检查用户是否在团队中
        EditorTeamMember editorTeamMember = teamMemberRepository.selectByBoth(teamId, userId);
        if(Objects.isNull(editorTeamMember)){
            throw new TeamServiceException(ErrorEnum.USER_NOT_IN_TEAM);
        }
        if(Objects.equals(editorTeamMember.getTeamRole(), teamRoleEnum)){
            return 0;
        }

        // 检测用户是否是所有者
        if(Objects.equals(editorTeamMember.getTeamRole(), TeamRoleEnum.OWNER)){
            throw new TeamServiceException(ErrorEnum.TEAM_CANNOT_CHANGE_OWNER_IN_UPDATE);
        }

        return teamMemberRepository.updateSingle(editorTeamMember.getId(),"team_role",teamRoleEnum);

    }

    @Override
    public int removeUserFromTeam(Long userId, Long teamId) {
        // 检测团队是否存在
        checkTeamExist(teamId);
        // 检测用户是否存在
        checkUserAvailable(userId);

        // 检查用户是否在团队中
        EditorTeamMember editorTeamMember = teamMemberRepository.selectByBoth(teamId, userId);
        if(Objects.isNull(editorTeamMember)){
            throw new TeamServiceException(ErrorEnum.USER_NOT_IN_TEAM);
        }
        // 检测用户是否是所有者
        if(Objects.equals(editorTeamMember.getTeamRole(), TeamRoleEnum.OWNER)){
            throw new TeamServiceException(ErrorEnum.TEAM_CANNOT_REMOVE_OWNER);
        }

        return teamMemberRepository.deleteById(editorTeamMember.getId());
    }

    @Override
    public int changeTeamOwner(Long ownerUserId, Long teamId) {
        // 检测团队是否存在
        checkTeamExist(teamId);
        // 检测用户是否存在
        checkUserAvailable(ownerUserId);

        // 检查用户是否在团队中
        EditorTeamMember editorTeamMember = teamMemberRepository.selectByBoth(teamId, ownerUserId);
        if(Objects.isNull(editorTeamMember)){
            throw new TeamServiceException(ErrorEnum.USER_NOT_IN_TEAM);
        }
        // 检测用户是否是所有者
        if(Objects.equals(editorTeamMember.getTeamRole(), TeamRoleEnum.OWNER)){
            return 0;
        }

        // 查找原团队主人
        EditorTeam team = repository.selectById(teamId);
        Long oldOwnerId = team.getOwnerId();

        // 将表中的信息更换
        team.setOwnerId(ownerUserId);
        int a1 = repository.update(team,null);

        // 维护团队-成员表，将该用户更变为所有者
        editorTeamMember.setTeamRole(TeamRoleEnum.OWNER);
        int a2 = teamMemberRepository.update(editorTeamMember,null);

        EditorTeamMember editorTeamMember1 = teamMemberRepository.selectByBoth(teamId, oldOwnerId);
        editorTeamMember1.setTeamRole(TeamRoleEnum.ADMIN);

        // 将原有的所有者设置为管理员
        int a3 =teamMemberRepository.update(editorTeamMember1,null);

        return a1+2*a2+4*a3;
    }

    // === 以下是团队成员相关的操作 ===


    private EditorTeam checkTeamExist(Long id) {
        // 检测团队的ID是否已经存在
        EditorTeam team = repository.selectByIdWithLock(id);
        if (Objects.isNull(team)) {
            throw new TeamServiceException(ErrorEnum.TEAM_NOT_FOUND);
        }
        return team;
    }

    private void checkUserAvailable(Long id) {
        // 检测用户的ID是否已经存在
        EditorUser editorUser = userRepository.selectByIdWithLock(id);
        if (Objects.isNull(editorUser) ) {
            throw new TeamServiceException(ErrorEnum.USER_NOT_FOUND);
        }
        if (!editorUser.isEnabled()){
            throw new TeamServiceException(ErrorEnum.USER_ACCOUNT_DISABLED);
        }
    }

}
