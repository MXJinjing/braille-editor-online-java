package wang.jinjing.editor.repository.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.toolkit.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import wang.jinjing.common.repository.AbstractBaseRepositoryImpl;
import wang.jinjing.editor.mapper.EditorTeamMapper;
import wang.jinjing.editor.pojo.entity.EditorTeam;
import wang.jinjing.editor.repository.EditorTeamRepository;

import java.util.List;
import java.util.Map;

@Repository
public class EditorTeamRepositoryImpl extends AbstractBaseRepositoryImpl<EditorTeam, EditorTeamMapper> implements EditorTeamRepository {

    @Autowired
    public EditorTeamRepositoryImpl(EditorTeamMapper mapper) {
        super(mapper);
    }

    @Override
    public List<EditorTeam> searchList(EditorTeam editorTeam, Map<String, Object> args, Sort sort) {

        // 基础条件：非空字段自动匹配
        if (args.isEmpty()) {
            QueryWrapper<EditorTeam> queryWrapper = new QueryWrapper<>();
            queryWrapper.setEntity(editorTeam);
            return mapper.selectList(queryWrapper);
        }

        return mapper.selectList(buildQueryWrapper(editorTeam, args, sort));
    }

    @Override
    public Page<EditorTeam> searchPage(EditorTeam editorTeam, Page<EditorTeam> page, Map<String,Object> args , Sort sort) {
        // 基础条件：非空字段自动匹配
        if (args.isEmpty()) {
            QueryWrapper<EditorTeam> queryWrapper = new QueryWrapper<>();
            queryWrapper.setEntity(editorTeam);
            return mapper.selectPage(page, queryWrapper);
        }

        return mapper.selectPage(page, buildQueryWrapper(editorTeam, args, sort));
    }

    private Wrapper<EditorTeam> buildQueryWrapper(EditorTeam editorTeam, Map<String, Object> args, Sort sort) {
        QueryWrapper<EditorTeam> queryWrapper = new QueryWrapper<>();
        if (StrUtils.isNotBlank(editorTeam.getTeamName())) {
            boolean fuzzy = (Boolean) args.getOrDefault("fuzzyQueryTeamName", false);
            if (fuzzy) {
                queryWrapper.lambda().like(EditorTeam::getTeamName, editorTeam.getTeamName());
            } else {
                queryWrapper.lambda().eq(EditorTeam::getTeamName, editorTeam.getTeamName());
            }
        }
        addSortToWrapper(queryWrapper, sort);
        return queryWrapper;
    }
}
