package wang.jinjing.editor.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.toolkit.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import wang.jinjing.common.repository.AbstractBaseRepositoryImpl;
import wang.jinjing.editor.mapper.EditorUserMapper;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.editor.pojo.enums.TeamRoleEnum;
import wang.jinjing.editor.repository.EditorUserRepository;

import java.util.List;
import java.util.Map;

@Repository
public class EditorUserRepositoryImpl extends AbstractBaseRepositoryImpl<EditorUser, EditorUserMapper> implements EditorUserRepository {

    @Autowired
    public EditorUserRepositoryImpl(EditorUserMapper mapper) {
        super(mapper);
    }

    @Override
    public EditorUser selectByUsername(String username) {
        LambdaQueryWrapper<EditorUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EditorUser::getUsername, username);
        return mapper.selectOne(queryWrapper);
    }

    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<EditorUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EditorUser::getUsername, username);
        return mapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<EditorUser> searchList(EditorUser editorUser, Map<String, Object> args, Sort sort) {

        // 基础条件：非空字段自动匹配
        if (args.isEmpty()) {
            QueryWrapper<EditorUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.setEntity(editorUser);
            return mapper.selectList(queryWrapper);
        }

        return mapper.selectList(buildQueryWrapper(editorUser, args, sort));
    }

    @Override
    public Page<EditorUser> searchPage(EditorUser editorUser, Page<EditorUser> page, Map<String,Object> args , Sort sort) {

        // 基础条件：非空字段自动匹配
        if (args.isEmpty()) {
            QueryWrapper<EditorUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.setEntity(editorUser);
            return mapper.selectPage(page, queryWrapper);
        }

        return mapper.selectPage(page, buildQueryWrapper(editorUser, args, sort));
    }


    private QueryWrapper<EditorUser> buildQueryWrapper(EditorUser editorUser, Map<String, Object> args, Sort sort) {
        QueryWrapper<EditorUser> queryWrapper = new QueryWrapper<>();
        if (StrUtils.isNotBlank(editorUser.getUsername())) {
            boolean fuzzy = (Boolean) args.getOrDefault("fuzzyQueryUsername", false);
            if (fuzzy) {
                queryWrapper.lambda().like(EditorUser::getUsername, editorUser.getUsername());
            } else {
                queryWrapper.lambda().eq(EditorUser::getUsername, editorUser.getUsername());
            }
        }

        if (StrUtils.isNotBlank(editorUser.getNickname())) {
            boolean fuzzy = (Boolean) args.getOrDefault("fuzzyQueryNickname", false);
            if (fuzzy) {
                queryWrapper.lambda().like(EditorUser::getNickname, editorUser.getNickname());
            } else {
                queryWrapper.lambda().eq(EditorUser::getNickname, editorUser.getNickname());
            }
        }

        if (StrUtils.isNotBlank(editorUser.getEmail())) {
            boolean fuzzy = (Boolean) args.getOrDefault("fuzzyQueryEmail", false);
            if (fuzzy) {
                queryWrapper.lambda().like(EditorUser::getEmail, editorUser.getEmail());
            } else {
                queryWrapper.lambda().eq(EditorUser::getEmail, editorUser.getEmail());
            }
        }

        if (StrUtils.isNotBlank(editorUser.getPhone())) {
            boolean fuzzy = (Boolean) args.getOrDefault("fuzzyQueryPhone", false);
            if (fuzzy) {
                queryWrapper.lambda().like(EditorUser::getPhone, editorUser.getPhone());
            } else {
                queryWrapper.lambda().eq(EditorUser::getPhone, editorUser.getPhone());
            }
        }

        //----------- 团队数量范围查询 -----------//
        // 拥有团队数（owner = user_id）
        addTeamCountCondition(queryWrapper,
                args.getOrDefault("ownedTeamCountRangeStart", 0),
                args.getOrDefault("ownedTeamCountRangeEnd", Integer.MAX_VALUE),
                TeamRoleEnum.OWNER);

        // 管理的团队数（role = 'admin'）
        addTeamCountCondition(queryWrapper,
                args.getOrDefault("administratedTeamCountRangeStart", 0),
                args.getOrDefault("administratedTeamCountRangeEnd", Integer.MAX_VALUE),
                TeamRoleEnum.ADMIN);

        // 作为成员的团队数（role = 'member'）
        addTeamCountCondition(queryWrapper,
                args.getOrDefault("memberedTeamCountRangeStart", 0),
                args.getOrDefault("memberedTeamCountRangeEnd", Integer.MAX_VALUE),
                TeamRoleEnum.ADMIN);

        // 总加入团队数（所有角色）
        addTeamCountCondition(queryWrapper,
                args.getOrDefault("joinedTeamCountRangeStart", 0),
                args.getOrDefault("joinedTeamCountRangeEnd", Integer.MAX_VALUE),
                null);

        addSortToWrapper(queryWrapper, sort);

        return queryWrapper;
    }

    /**
     * 添加团队数量条件
     *
     * @param role 角色过滤（null 表示不限制角色）
     */
    private void addTeamCountCondition(QueryWrapper<EditorUser> queryWrapper,
                                       Object rangeStart,
                                       Object rangeEnd,
                                       TeamRoleEnum role) {
        int start = (int) rangeStart;
        int end = (int) rangeEnd;
        if (start <= 0 && end == Integer.MAX_VALUE) return; // 无范围限制时不添加条件

        // 构建子查询SQL
        String subQuerySql = String.format(
                "(SELECT COUNT(*) FROM %s WHERE user_id = editor_user.id %s)",
                "editor_team_member", // 区分团队表和成员表
                role != null ? "AND team_role = '" + role.getValue() + "'" : ""
        );

        // 添加范围条件
        if (start > 0) {
            queryWrapper.apply("({0}) >= {1}", subQuerySql, start);
        }
        if (end < Integer.MAX_VALUE) {
            queryWrapper.apply("({0}) <= {1}", subQuerySql, end);
        }
    }


}
