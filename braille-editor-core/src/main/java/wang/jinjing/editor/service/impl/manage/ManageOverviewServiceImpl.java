package wang.jinjing.editor.service.impl.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.jinjing.editor.pojo.VO.ManageOverviewVO;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.editor.pojo.entity.OssFileMetadata;
import wang.jinjing.editor.repository.EditorUserRepository;
import wang.jinjing.editor.repository.OssFileMetadataRepository;
import wang.jinjing.editor.service.ManageOverviewService;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ManageOverviewServiceImpl implements ManageOverviewService {

    @Autowired
    private EditorUserRepository editorUserRepository;

    @Autowired
    private OssFileMetadataRepository ossFileMetadataRepository;

    @Override
    public ManageOverviewVO getOverview() {
        ManageOverviewVO manageOverviewVO = new ManageOverviewVO();
        // 1. 获取所有用户数量
        QueryWrapper<EditorUser> emptyWrapper = new QueryWrapper<>();
        manageOverviewVO.setUserCount(editorUserRepository.selectCount(emptyWrapper));

        // 2. 获取所有文件数量
        QueryWrapper<OssFileMetadata> emptyWrapper2 = new QueryWrapper<>();
        emptyWrapper2.eq("is_deleted", 0).eq("is_dir",false);
        manageOverviewVO.setFileCount(ossFileMetadataRepository.selectCount(emptyWrapper2));

        // 3. 获取今日访问用户数量
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        QueryWrapper<EditorUser> todayWrapper = new QueryWrapper<>();
        todayWrapper.between("last_login_at", startOfDay, endOfDay);

        manageOverviewVO.setVisitCount(editorUserRepository.selectCount(todayWrapper));

        // 4. 获取今日新建文件数量
        QueryWrapper<OssFileMetadata> todayWrapper2 = new QueryWrapper<>();
        todayWrapper2.eq("is_deleted", 0).eq("is_dir",false);
        todayWrapper2.between("create_at", startOfDay, endOfDay);

        manageOverviewVO.setFileTodayCount(ossFileMetadataRepository.selectCount(todayWrapper2));

        return manageOverviewVO;
    }
}
