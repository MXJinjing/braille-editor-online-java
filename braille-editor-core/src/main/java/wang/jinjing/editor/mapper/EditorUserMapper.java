package wang.jinjing.editor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import wang.jinjing.editor.pojo.entity.EditorUser;

@Mapper
public interface EditorUserMapper extends MPJBaseMapper<EditorUser> {

}
