package wang.jinjing.editor.controller.publics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wang.jinjing.editor.pojo.VO.OssFileMetadataVO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/public/test")
public class TestController {

    @GetMapping
    public String testGet() {
        return "GET request successful!";
    }

    @GetMapping("files")
    public List<OssFileMetadataVO> getTestFiles() {
        List<OssFileMetadataVO> fileList = new ArrayList<>();
        String bucketName = "test-bucket";

        // 模拟两个测试用户
        Long adminId = 1001L;
        String adminName = "admin";
        Long userId = 1002L;
        String userName = "user01";

        // 创建两个文件夹（使用admin创建）
        fileList.add(createFolder(
                1L,
                "测试文件夹1",
                bucketName,
                "folder1/",
                adminId,
                adminName
        ));

        fileList.add(createFolder(
                2L,
                "测试文件夹2",
                bucketName,
                "folder2/",
                adminId,
                adminName
        ));

        // 在第一个文件夹下创建5个HTML文件（混合用户操作）
        for (int i = 1; i <= 5; i++) {
            boolean useAdmin = i % 2 == 0;
            Long creatorId = useAdmin ? adminId : userId;
            String creatorName = useAdmin ? adminName : userName;

            fileList.add(createHtmlFile(
                    1000L + i,
                    "file" + i + ".html",
                    bucketName,
                    "folder1/file" + i + ".html",
                    "/测试文件夹1",
                    (long) (Math.random() * 100 * 1024),
                    creatorId,
                    creatorName,
                    // 最后更新者随机设置
                    Math.random() > 0.5 ? adminId : userId,
                    Math.random() > 0.5 ? adminName : userName
            ));
        }

        // 在第二个文件夹下创建5个HTML文件（使用user01创建）
        for (int i = 6; i <= 10; i++) {
            fileList.add(createHtmlFile(
                    1000L + i,
                    "file" + i + ".html",
                    bucketName,
                    "folder2/file" + i + ".html",
                    "/测试文件夹2",
                    (long) (Math.random() * 100 * 1024),
                    userId,
                    userName,
                    userId,
                    userName
            ));
        }

        return fileList;
    }

    private OssFileMetadataVO createFolder(Long id, String name, String bucket, String key,
                                           Long createBy, String createByUsername) {
        OssFileMetadataVO folder = new OssFileMetadataVO();
        folder.setRealFileName(name);
        folder.setPath(key);
        folder.setIsDir(true);
        folder.setParentPath("");
        folder.setCreateAt(new Date());
        folder.setLastModifiedAt(new Date());
        folder.setFileSize(0L);
        folder.setMimeType("directory");

        // 用户信息
        folder.setCreateBy(createBy);
        folder.setCreateByUsername(createByUsername);
        folder.setLastModifiedBy(createBy); // 目录创建后未修改
        folder.setLastModifiedByUsername(createByUsername);

        return folder;
    }

    private OssFileMetadataVO createHtmlFile(Long id, String name, String bucket, String key,
                                             String parentPath, Long size,
                                             Long createBy, String createByUsername,
                                             Long lastUpdateBy, String lastUpdateByUsername) {
        OssFileMetadataVO file = new OssFileMetadataVO();
        file.setRealFileName(name);
        file.setPath(key);
        file.setIsDir(false);
        file.setParentPath(parentPath);
        file.setFileSize(size);
        file.setMimeType("text/html");
        file.setCreateAt(new Date());
        file.setLastModifiedAt(new Date());

        // 用户信息
        file.setCreateBy(createBy);
        file.setCreateByUsername(createByUsername);
        file.setLastModifiedBy(lastUpdateBy);
        file.setLastModifiedByUsername(lastUpdateByUsername);

        return file;
    }
}
