package wang.jinjing.editor.service.impl.file;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.parameters.P;
import org.springframework.web.multipart.MultipartFile;
import wang.jinjing.editor.pojo.VO.OssFileMetadataVO;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.editor.pojo.entity.OssFileMetadata;
import wang.jinjing.editor.repository.EditorUserRepository;
import wang.jinjing.editor.repository.OssFileMetadataRepository;
import wang.jinjing.editor.repository.impl.EditorUserRepositoryImpl;
import wang.jinjing.editor.service.file.BaseFileService;
import wang.jinjing.editor.service.oss.OssBucketService;
import wang.jinjing.editor.service.oss.OssObjectService;
import wang.jinjing.editor.util.SecurityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Slf4j
class BaseFileServiceImplTest {

    private static final String BUCKET_NAME = "test-bucket-55555";

    @Mock
    private OssObjectService ossObjectService;

    @Mock
    private OssBucketService ossBucketService;

    @Mock
    private OssFileMetadataRepository ofmRepository;

    @Mock
    private EditorUserRepository editorUserRepository;


    @Spy
    @InjectMocks
    private BaseFileServiceImpl fileService;

    private EditorUser mockUser;

    private MultipartFile mockFile;



    @BeforeEach
    void setUp() throws IOException {
        mockUser = new EditorUser();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
    }

    @Test
    void getParentPath() {
        String[] paths = new String[]{
                "/a/b/c/d/e","/a/b/c/d","/","file.txt"
        };

        List<String> results = new ArrayList<>();
        for (String path : paths) {
            List<String> pathSegments = BaseFileServiceImpl.getPathSegments(path);
            String parentPath = BaseFileServiceImpl.getParentPath(pathSegments,1);
            results.add(parentPath);
        }

        List<String> pathSegments = BaseFileServiceImpl.getPathSegments(paths[0]);
        String parentPath = BaseFileServiceImpl.getParentPath(pathSegments,5);
        results.add(parentPath);
        parentPath = BaseFileServiceImpl.getParentPath(pathSegments,6);
        results.add(parentPath);

        assertEquals("/a/b/c/d/",results.get(0));
        assertEquals("/a/b/c/",results.get(1));
        assertEquals("",results.get(2));
        assertEquals("/",results.get(3));
        assertEquals("/",results.get(4));
        assertEquals("",results.get(5));

    }

    @Test
    void getPathSegments() {
        String[] paths = new String[]{
                "a/b/c/","/a/b/c","//a//b///c////","/","file.txt"
        };

        List<String> reformedPaths = new ArrayList<>();
        for (String path : paths) {
            List<String> pathSegments = BaseFileServiceImpl.getPathSegments(path);
            StringJoiner joiner = new StringJoiner("/","/","");
            for (String pathSegment : pathSegments) {
                joiner.add(pathSegment);
            }
            reformedPaths.add(joiner.toString());

            log.info("path segments: {}", JSONUtil.toJsonPrettyStr(pathSegments));
        }

        log.info("reformed paths: {}", reformedPaths);
        assertEquals(reformedPaths.size(), paths.length);
        assertEquals("/a/b/c", reformedPaths.get(0));

        assertEquals("/a/b/c", reformedPaths.get(1));
        assertEquals("/a/b/c", reformedPaths.get(2));
        assertEquals("/", reformedPaths.get(3));
        assertEquals("/file.txt", reformedPaths.get(4));
    }

    @Test
    void normalizePath() {
        String[] paths = new String[]{
                "a/b/c/",
                "/a/b/c",
                "//a//b///c////",
                "/file.txt",
                "file.txt",
                "",
                "/a///s//file.txt"
        };

        String[] expected = new String[]{
                "/a/b/c/",
                "/a/b/c/",
                "/a/b/c/",
                "/file.txt",
                "/file.txt",
                "/",
                "/a/s/file.txt"
        };

        boolean[] booleans = new boolean[]{
                true, true, true, false, false, true, false
        };

        for (int i = 0; i < paths.length; i++) {
            String s = BaseFileServiceImpl.normalizePath(paths[i], booleans[i]);
            assertEquals(expected[i], s);
        }


    }



    @Test
    void mkDir_ShouldCreateRootFolder() {
        when(ofmRepository.selectByPath(eq("bucket"), eq(""), eq(true)))
                .thenReturn(null); // 父目录（不存在的虚拟路径）

        AtomicLong generatedId = new AtomicLong(199L);
        doAnswer(invocation -> {
            OssFileMetadata meta = invocation.getArgument(0);
            meta.setId(generatedId.get()); // 关键：设置 ID 到对象中
            return null;
        }).when(ofmRepository).insert(any(OssFileMetadata.class));

        try (MockedStatic<SecurityUtils> securityUtilsMock = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            long id = fileService.mkDir("bucket", "/");
            assertEquals(199L, id);
        }

        ArgumentCaptor<OssFileMetadata> captor = ArgumentCaptor.forClass(OssFileMetadata.class);
        verify(ofmRepository).insert(captor.capture());

        OssFileMetadata savedMeta = captor.getValue();
        assertAll("根目录元数据校验",
                () -> assertEquals("/", savedMeta.getS3Key()),
                () -> assertEquals("", savedMeta.getRealFileName()),
                () -> assertEquals("", savedMeta.getParentPath()),
                () -> assertEquals(199L, savedMeta.getId()) // 确保 ID 被正确设置
        );
    }

    @Test
    void mkDir_ShouldCreateNestedFolderWithParent() {
        // 1. 准备测试数据
        final String BUCKET = "test-bucket";
        final String TARGET_PATH = "/apple/banana/";
        final long MOCK_PARENT_ID = 100L;
        final long MOCK_NEW_ID = 200L;

        // 2. 模拟依赖行为
        // 2.1 模拟父目录存在
        OssFileMetadata mockParent = new OssFileMetadata();
        mockParent.setId(MOCK_PARENT_ID);
        mockParent.setS3Key("/apple/");
        when(ofmRepository.selectByPath(BUCKET, "/apple/", true))
                .thenReturn(mockParent);

        // 2.2 模拟当前用户
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            // 2.3 模拟数据库插入返回ID
            doAnswer(invocation -> {
                OssFileMetadata param = invocation.getArgument(0);
                param.setId(MOCK_NEW_ID); // 模拟生成ID
                return null;
            }).when(ofmRepository).insert(any(OssFileMetadata.class));

            // 3. 执行测试方法
            long newFolderId = fileService.mkDir(BUCKET, TARGET_PATH);

            // 4. 验证结果
            assertEquals(MOCK_NEW_ID, newFolderId);

            // 5. 验证元数据正确性
            ArgumentCaptor<OssFileMetadata> metaCaptor = ArgumentCaptor.forClass(OssFileMetadata.class);
            verify(ofmRepository).insert(metaCaptor.capture());

            OssFileMetadata actualMeta = metaCaptor.getValue();
            assertAll("验证文件夹元数据",
                    () -> assertEquals(BUCKET, actualMeta.getS3Bucket()),
                    () -> assertEquals(TARGET_PATH, actualMeta.getS3Key()),
                    () -> assertEquals("banana", actualMeta.getRealFileName()),
                    () -> assertEquals(MOCK_PARENT_ID, actualMeta.getParentId()),
                    () -> assertEquals("/apple/", actualMeta.getParentPath()),
                    () -> assertTrue(actualMeta.getIsDir()),
                    () -> assertEquals(1L, actualMeta.getCreateBy()),
                    () -> assertNotNull(actualMeta.getCreateAt())
            );
        }
    }

    @Test
    void findOrCreateParentFolder_ShouldCreateAllMissingParents() {
        long result;
        final long MOCK_NEW_ID = 200L;

        try (MockedStatic<SecurityUtils> securityUtilsMock = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            // 1. 模拟依赖项
            when(ofmRepository.selectByPath(anyString(), anyString(), eq(true)))
                    .thenReturn(null);

            AtomicLong idCounter = new AtomicLong(1);

            doAnswer( invocation->{
                OssFileMetadata param = invocation.getArgument(0);
                param.setId(MOCK_NEW_ID); // 模拟生成ID
                return null;
            }).when(ofmRepository).insert(any(OssFileMetadata.class));

            // 2. 使用 thenAnswer 正确记录调用顺序
            when(fileService.mkDir(anyString(), anyString())).thenAnswer(invocation -> {
                String path = invocation.getArgument(1);
                System.out.println("Creating directory: " + path);
                return idCounter.getAndIncrement();
            });

            // 3. 执行测试方法
            result = fileService.findOrCreateParentFolder("testBucket", "/a/b/c/d/e");
            securityUtilsMock.verify(SecurityUtils::getCurrentUser);
        }
        // 4. 验证结果
        assertEquals(5, result);

        // 5. 验证调用顺序
        InOrder inOrder = inOrder(fileService);
        inOrder.verify(fileService).mkDir("testBucket", "/");
        inOrder.verify(fileService).mkDir("testBucket", "/a/");
        inOrder.verify(fileService).mkDir("testBucket", "/a/b/");
        inOrder.verify(fileService).mkDir("testBucket", "/a/b/c/");
        inOrder.verify(fileService).mkDir("testBucket", "/a/b/c/d/");
    }

    /**
     * 测试正常上传新文件
     */
    @Test
    void uploadFile_Success() throws Exception {
        final long MOCK_NEW_ID = 100L;
        final long MOCK_PATH_ID = 200L;
        final String BUCKET = "testBucket";
        final String FILE_NAME = "test.txt";
        final String EXPECTED_PATH = "/docs/myDoc/day1/test.txt";
        final String PARENT_PATH = "/docs/myDoc/day1/";

        try (MockedStatic<SecurityUtils> securityUtilsMock = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            // Mock存储不存在该文件
            when(ossObjectService.objectExists(eq(BUCKET), eq(EXPECTED_PATH))).thenReturn(false);

            // 文件内容
            mockFile = mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn(FILE_NAME);
            when(mockFile.getContentType()).thenReturn("text/plain");
            when(mockFile.getSize()).thenReturn(1024L);
            when(mockFile.getInputStream())
                    .thenReturn(new ByteArrayInputStream("content content 121212".getBytes()));

            doReturn(MOCK_PATH_ID)
                    .when(fileService).findOrCreateParentFolder(eq(BUCKET),any());

            doAnswer(invocation -> {
                OssFileMetadata ossFileMetadata = invocation.getArgument(0);
                ossFileMetadata.setId(MOCK_NEW_ID);
                return 1;
            }).when(ofmRepository).insert(any(OssFileMetadata.class));

            // 3. 执行测试逻辑
            fileService.uploadFile(BUCKET, PARENT_PATH, mockFile, false);

            // 4. 验证静态方法调用
            securityUtilsMock.verify(SecurityUtils::getCurrentUser);
        }

        // 验证对象存储上传调用
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(ossObjectService).uploadStream(
                eq(BUCKET),  // 验证存储桶
                pathCaptor.capture(),  // 捕获实际路径参数
                any(InputStream.class),  // 允许任意输入流
                eq(1024L),  // 验证文件大小
                argThat(meta ->  // 元数据验证
                        meta.getOrDefault("b-e-real-name", "").equals("test.txt") &&
                                meta.getOrDefault("b-e-create-user-id", "").equals("1")
                )
        );

        final String actualPath = pathCaptor.getValue();

        // 验证路径规范化
        assertThat(actualPath)
                .as("文件上传路径校验")
                .isEqualTo(EXPECTED_PATH);

        // 验证元数据存储
        ArgumentCaptor<OssFileMetadata> metaCaptor = ArgumentCaptor.forClass(OssFileMetadata.class);

        verify(ofmRepository).insert(metaCaptor.capture());
        OssFileMetadata savedMeta = metaCaptor.getValue();

        assertAll(
                () -> assertEquals(1L, savedMeta.getCreateBy()),
                () -> assertEquals(EXPECTED_PATH, savedMeta.getS3Key()),
                () -> assertEquals(PARENT_PATH, savedMeta.getParentPath())
        );

    }

    @Test
    void uploadFile_ShouldRenameWhenConflict() throws Exception {
        final String BUCKET = "testBucket";
        final long MOCK_NEW_ID = 100L;
        final long MOCK_PATH_ID = 200L;
        final String ORIGINAL_PATH = "/docs/myDoc/day1/test.txt";
        final String PARENT_PATH = "/docs/myDoc/day1/";
        final String RENAMED_PATH = "/docs/myDoc/day1/test(1).txt";

        try (MockedStatic<SecurityUtils> securityUtilsMock = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            // Mock 文件存在性检查：第一次存在，后续不存在
            when(ossObjectService.objectExists(eq(BUCKET), anyString()))
                    .thenReturn(true)  // 第一次检查原始路径存在
                    .thenReturn(true)  // 第二次检查原始路径存在
                    .thenReturn(false); // 后续检查新路径不存在

            // Mock 文件内容
            mockFile = mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn("test.txt");
            when(mockFile.getContentType()).thenReturn("text/plain");
            when(mockFile.getSize()).thenReturn(1024L);
            when(mockFile.getInputStream())
                    .thenReturn(new ByteArrayInputStream("content".getBytes()));

            // Mock 父目录创建
            doReturn(MOCK_PATH_ID)
                    .when(fileService).findOrCreateParentFolder(eq(BUCKET), anyString());

            doAnswer(invocation -> {
                OssFileMetadata ossFileMetadata = invocation.getArgument(0);
                ossFileMetadata.setId(MOCK_NEW_ID);
                return 1;
            }).when(ofmRepository).insert(any(OssFileMetadata.class));

            doAnswer(invocation -> {
                return RENAMED_PATH;
            }).when(fileService).handleDestinationExistenceAndRename(anyString(),anyString(),anyBoolean(),anyBoolean());


            // 执行上传
            fileService.uploadFile(BUCKET, PARENT_PATH, mockFile, false);

            // 验证用户上下文调用
            securityUtilsMock.verify(SecurityUtils::getCurrentUser);
        }

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(ossObjectService).uploadStream(
                eq(BUCKET),  // 验证存储桶
                pathCaptor.capture(),  // 捕获实际路径参数
                any(InputStream.class),  // 允许任意输入流
                eq(1024L),  // 验证文件大小
                argThat(meta ->  // 元数据验证
                        meta.getOrDefault("b-e-real-name", "").equals("test(1).txt") &&
                                meta.getOrDefault("b-e-create-user-id", "").equals("1")
                )
        );

        // 获取实际调用的路径参数
        final String actualPath = pathCaptor.getValue();

        // 验证路径是否符合预期（使用明确的断言消息）
        assertThat(actualPath)
                .as("文件上传路径校验")
                .isEqualTo(RENAMED_PATH);

        // 验证元数据存储
        ArgumentCaptor<OssFileMetadata> metaCaptor = ArgumentCaptor.forClass(OssFileMetadata.class);
        verify(ofmRepository).insert(metaCaptor.capture());

        OssFileMetadata savedMeta = metaCaptor.getValue();
        assertAll("重命名文件元数据校验",
                () -> assertEquals(RENAMED_PATH, savedMeta.getS3Key()),
                () -> assertEquals("test(1).txt", savedMeta.getRealFileName()),
                () -> assertEquals(MOCK_PATH_ID, savedMeta.getParentId()),
                () -> assertEquals(PARENT_PATH, savedMeta.getParentPath())
        );
    }

    @Test
    void uploadFile_ShouldOverwriteWhenConflict() throws Exception {
        final String BUCKET = "testBucket";
        final long MOCK_NEW_ID = 100L;
        final long MOCK_PATH_ID = 200L;
        final String TARGET_PATH = "/docs/myDoc/day1/test.txt";
        final String PARENT_PATH = "/docs/myDoc/day1/";

        try (MockedStatic<SecurityUtils> securityUtilsMock = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            // Mock 文件存在性检查：第一次存在，后续不存在
            when(ossObjectService.objectExists(eq(BUCKET), anyString()))
                    .thenReturn(true);  // 第一次检查原始路径存在

            // Mock 文件内容
            mockFile = mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn("test.txt");
            when(mockFile.getContentType()).thenReturn("text/plain");
            when(mockFile.getSize()).thenReturn(1024L);
            when(mockFile.getInputStream())
                    .thenReturn(new ByteArrayInputStream("content".getBytes()));

            // Mock 父目录创建
            doReturn(MOCK_PATH_ID)
                    .when(fileService).findOrCreateParentFolder(eq(BUCKET), anyString());

            doAnswer(invocation -> {
                OssFileMetadata ossFileMetadata = invocation.getArgument(0);
                ossFileMetadata.setId(MOCK_NEW_ID);
                return 1;
            }).when(ofmRepository).insert(any(OssFileMetadata.class));

            doNothing().when(fileService).deleteObjectFromS3(anyString(),anyString());

            // 执行上传
            fileService.uploadFile(BUCKET, PARENT_PATH, mockFile, true);

            // 验证用户上下文调用
            securityUtilsMock.verify(SecurityUtils::getCurrentUser);
        }

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(ossObjectService).uploadStream(
                eq(BUCKET),  // 验证存储桶
                pathCaptor.capture(),  // 捕获实际路径参数
                any(InputStream.class),  // 允许任意输入流
                eq(1024L),  // 验证文件大小
                argThat(meta ->  // 元数据验证
                        meta.getOrDefault("b-e-real-name", "").equals("test.txt") &&
                                meta.getOrDefault("b-e-create-user-id", "").equals("1")
                )
        );

        // 获取实际调用的路径参数
        final String actualPath = pathCaptor.getValue();

        // 验证路径是否符合预期（使用明确的断言消息）
        assertThat(actualPath)
                .as("文件上传路径校验")
                .isEqualTo(TARGET_PATH);

        // 验证元数据存储
        ArgumentCaptor<OssFileMetadata> metaCaptor = ArgumentCaptor.forClass(OssFileMetadata.class);
        verify(ofmRepository).insert(metaCaptor.capture());

        OssFileMetadata savedMeta = metaCaptor.getValue();
        assertAll("重命名文件元数据校验",
                () -> assertEquals(TARGET_PATH, savedMeta.getS3Key()),
                () -> assertEquals("test.txt", savedMeta.getRealFileName()),
                () -> assertEquals(MOCK_PATH_ID, savedMeta.getParentId()),
                () -> assertEquals(PARENT_PATH, savedMeta.getParentPath())
        );
    }


    @Test
    void uploadFileByBytes_Success() {
        final long MOCK_NEW_ID = 100L;
        final long MOCK_PATH_ID = 200L;
        final String BUCKET = "testBucket";
        final String FILE_NAME = "test.txt";
        final String EXPECTED_PATH = "/docs/myDoc/day1/test.txt";
        final String PARENT_PATH = "/docs/myDoc/day1/";

        final String MIME_TYPE = "text/plain";
        final byte[] bytes = "test file content 1231232".getBytes();

        try (MockedStatic<SecurityUtils> securityUtilsMock = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            // Mock存储不存在该文件
            when(ossObjectService.objectExists(eq(BUCKET), eq(EXPECTED_PATH))).thenReturn(false);

            doReturn(MOCK_PATH_ID)
                    .when(fileService).findOrCreateParentFolder(eq(BUCKET),any());

            doAnswer(invocation -> {
                OssFileMetadata ossFileMetadata = invocation.getArgument(0);
                ossFileMetadata.setId(MOCK_NEW_ID);
                return 1;
            }).when(ofmRepository).insert(any(OssFileMetadata.class));

            // 3. 执行测试逻辑
            fileService.uploadFileByBytes(BUCKET, PARENT_PATH, FILE_NAME,MIME_TYPE, bytes, false);

            // 4. 验证静态方法调用
            securityUtilsMock.verify(SecurityUtils::getCurrentUser);
        }

        // 验证对象存储上传调用
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(ossObjectService).uploadStream(
                eq(BUCKET),  // 验证存储桶
                pathCaptor.capture(),  // 捕获实际路径参数
                any(InputStream.class),  // 允许任意输入流
                eq((long)bytes.length),  // 验证文件大小
                argThat(meta ->  // 元数据验证
                        meta.getOrDefault("b-e-real-name", "").equals("test.txt") &&
                                meta.getOrDefault("b-e-create-user-id", "").equals("1")
                )
        );

        final String actualPath = pathCaptor.getValue();

        // 验证路径规范化
        assertThat(actualPath)
                .as("文件上传路径校验")
                .isEqualTo(EXPECTED_PATH);

        // 验证元数据存储
        ArgumentCaptor<OssFileMetadata> metaCaptor = ArgumentCaptor.forClass(OssFileMetadata.class);

        verify(ofmRepository).insert(metaCaptor.capture());
        OssFileMetadata savedMeta = metaCaptor.getValue();

        assertAll(
                () -> assertEquals(1L, savedMeta.getCreateBy()),
                () -> assertEquals(EXPECTED_PATH, savedMeta.getS3Key()),
                () -> assertEquals(PARENT_PATH, savedMeta.getParentPath())
        );

    }

    @Test
    void uploadFileByBytes_ShouldOverwriteWhenConflict() throws Exception {
        final String BUCKET = "testBucket";
        final long MOCK_NEW_ID = 100L;
        final long MOCK_PATH_ID = 200L;
        final String FILE_NAME = "test.txt";
        final String TARGET_PATH = "/docs/myDoc/day1/test.txt";
        final String PARENT_PATH = "/docs/myDoc/day1/";

        final String MIME_TYPE = "text/plain";
        final byte[] bytes = "test file content 1231232".getBytes();

        try (MockedStatic<SecurityUtils> securityUtilsMock = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);

            // Mock 文件存在性检查：第一次存在，后续不存在
            when(ossObjectService.objectExists(eq(BUCKET), anyString()))
                    .thenReturn(true);  // 第一次检查原始路径存在

            // Mock 父目录创建
            doReturn(MOCK_PATH_ID)
                    .when(fileService).findOrCreateParentFolder(eq(BUCKET), anyString());

            doAnswer(invocation -> {
                OssFileMetadata ossFileMetadata = invocation.getArgument(0);
                ossFileMetadata.setId(MOCK_NEW_ID);
                return 1;
            }).when(ofmRepository).insert(any(OssFileMetadata.class));

            doNothing().when(fileService).deleteObjectFromS3(anyString(),anyString());

            // 执行上传
            fileService.uploadFileByBytes(BUCKET,PARENT_PATH,FILE_NAME,MIME_TYPE,bytes,true);

            // 验证用户上下文调用
            securityUtilsMock.verify(SecurityUtils::getCurrentUser);
        }

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(ossObjectService).uploadStream(
                eq(BUCKET),  // 验证存储桶
                pathCaptor.capture(),  // 捕获实际路径参数
                any(InputStream.class),  // 允许任意输入流
                eq((long) bytes.length),  // 验证文件大小
                argThat(meta ->  // 元数据验证
                        meta.getOrDefault("b-e-real-name", "").equals("test.txt") &&
                                meta.getOrDefault("b-e-create-user-id", "").equals("1")
                )
        );

        // 获取实际调用的路径参数
        final String actualPath = pathCaptor.getValue();

        // 验证路径是否符合预期（使用明确的断言消息）
        assertThat(actualPath)
                .as("文件上传路径校验")
                .isEqualTo(TARGET_PATH);

        // 验证元数据存储
        ArgumentCaptor<OssFileMetadata> metaCaptor = ArgumentCaptor.forClass(OssFileMetadata.class);
        verify(ofmRepository).insert(metaCaptor.capture());

        OssFileMetadata savedMeta = metaCaptor.getValue();
        assertAll("重命名文件元数据校验",
                () -> assertEquals(TARGET_PATH, savedMeta.getS3Key()),
                () -> assertEquals("test.txt", savedMeta.getRealFileName()),
                () -> assertEquals(MOCK_PATH_ID, savedMeta.getParentId()),
                () -> assertEquals(PARENT_PATH, savedMeta.getParentPath())
        );
    }

    @Test
    void downloadFile() {
    }

    @Test
    void downloadAsBytes() {
    }

    @Test
    void getMetadataByPath_IsFileSuccess() {
        String bucket = "test-bucket";
        String path = "/docs/myDoc/day1/test.txt";

        OssFileMetadata testFile = OssFileMetadata.builder()
                .id(10018L).realFileName("Sample1.html").createBy(1L).lastUpdateBy(1L)
                .s3Bucket("test-bucket").s3Key("/docs/myDoc/day1/Sample1.html").build();

        EditorUser testUser = EditorUser.builder().id(1L).username("testUser1").build();

        doReturn(true).when(ossBucketService).bucketExists(eq(bucket));
        doReturn(true).when(ossObjectService).objectExists(eq(bucket),eq(path));
        doReturn(testFile).when(ofmRepository).selectByPath(eq(bucket), eq(path), anyBoolean());
        doReturn(Stream.of(testUser).toList()).when(editorUserRepository).selectByIds(any());
        doReturn(Map.of(1L,"testUser1")).when(fileService).getUserMap(any());

        // 调用方法
        OssFileMetadataVO result = fileService.getMetadataByPath(bucket, path,false);

        // 验证结果
        assertAll(
            () -> assertEquals(testFile.getId(), result.getId()),
            () -> assertEquals(testFile.getRealFileName(), result.getRealFileName()),
            () -> assertEquals(testFile.getS3Key(), result.getS3Key()),
            () -> assertEquals(testFile.getS3Bucket(), result.getS3Bucket()),
            () -> assertEquals(testFile.getCreateBy(), result.getCreateBy()),
            () -> assertEquals(testFile.getLastUpdateBy(), result.getLastUpdateBy()),
            () -> assertEquals(testUser.getUsername(), result.getCreateByUsername()),
            () -> assertEquals(testUser.getUsername(), result.getLastUpdateByUsername())
);
    }

    @Test
    void existsByPath_Success() {
        String BUCKET_NAME = "test-bucket";
        String PATH = "/docs/my-game/file1.html";


        doReturn(true).when(ossBucketService).bucketExists(eq(BUCKET_NAME));
        doReturn(true).when(ossObjectService).objectExists(eq(BUCKET_NAME),eq(PATH));
        doReturn(true).when(ofmRepository).existsByPath(eq(BUCKET_NAME),eq(PATH),anyBoolean());

        boolean b = fileService.existsByPath(BUCKET_NAME, PATH, false);
        assertTrue(b);
    }

    @Test
    void listFiles_Success() {
        String bucket = "test-bucket";
        String folderPath = "/docs/myDoc/day1/";

        OssFileMetadata testFile1 = OssFileMetadata.builder()
                .id(10018L).realFileName("Sample1.html").createBy(1L).lastUpdateBy(1L)
                .s3Bucket("test-bucket").s3Key("/docs/myDoc/day1/Sample1.html").build();

        OssFileMetadata testFile2 = OssFileMetadata.builder()
                .id(10019L).realFileName("Sample2.html").createBy(2L).lastUpdateBy(2L)
                .s3Bucket("test-bucket").s3Key("/docs/myDoc/day1/Sample1.html").build();

        EditorUser testUser1 = EditorUser.builder().id(1L).username("testUser1").build();
        EditorUser testUser2 = EditorUser.builder().id(2L).username("testUser2").build();


        doReturn(List.of(testFile1, testFile2))
                .when(ofmRepository).listByPath(eq(bucket), eq(folderPath), any(Sort.class));

        doReturn(true).when(ossBucketService).bucketExists(eq(bucket));

        doReturn(List.of(testUser1, testUser2)).when(editorUserRepository).selectByIds(any());

        // 调用方法
        List<OssFileMetadataVO> result = fileService.listFiles(bucket, folderPath, Sort.unsorted());

        // 验证结果
        assertAll(
            ()->assertEquals(2, result.size()),
            ()->assertEquals(testFile1.getId(), result.get(0).getId()),
            ()->assertEquals(testUser1.getUsername(), result.get(0).getCreateByUsername()),
            ()->assertEquals(testUser1.getUsername(), result.get(0).getLastUpdateByUsername()),
            ()->assertEquals(testFile2.getId(),result.get(1).getId()),
            ()->assertEquals(testUser2.getUsername(), result.get(1).getCreateByUsername()),
            ()->assertEquals(testUser2.getUsername(), result.get(1).getLastUpdateByUsername())
        );
    }

    @Test
    void listPagedFiles() {
    }

    @Test
    void moveToTrash() {
    }

    @Test
    void testMoveToTrash() {
    }

    @Test
    void recoveryFromTrash() {
    }

    @Test
    void realDeleteFiles() {
    }

    @Test
    void mkDir() {
    }

    @Test
    void mkDirsWithParent() {
    }

    @Test
    void copyObject() {
    }

    @Test
    void getUserMap() {
        EditorUser testUser1 = EditorUser.builder().id(1L).username("testUser1").build();
        EditorUser testUser2 = EditorUser.builder().id(2L).username("testUser2").build();
        EditorUser testUser3 = EditorUser.builder().id(3L).username("testUser3").build();
        EditorUser testUser4 = EditorUser.builder().id(4L).username("testUser4").build();

        OssFileMetadata testFile1 = OssFileMetadata.builder()
                .id(10018L).realFileName("Sample1.html").createBy(1L).lastUpdateBy(1L)
                .s3Bucket("test-bucket").s3Key("/docs/myDoc/day1/Sample1.html").build();

        OssFileMetadata testFile2 = OssFileMetadata.builder()
                .id(10019L).realFileName("Sample2.html").createBy(2L).lastUpdateBy(2L)
                .s3Bucket("test-bucket").s3Key("/docs/myDoc/day1/Sample1.html").build();

        OssFileMetadata testFile3 = OssFileMetadata.builder()
                .id(10020L).realFileName("Sample3.html").createBy(3L).lastUpdateBy(3L)
                .s3Bucket("test-bucket").s3Key("/docs/myDoc/day1/Sample1.html").build();

        OssFileMetadata testFile4 = OssFileMetadata.builder()
                .id(10021L).realFileName("Sample4.html").createBy(4L).lastUpdateBy(4L)
                .s3Bucket("test-bucket").s3Key("/docs/myDoc/day1/Sample1.html").build();

        List<OssFileMetadata> fileList = Arrays.asList(testFile1, testFile2, testFile3, testFile4);

        doReturn(List.of(testUser1, testUser2, testUser3, testUser4))
                .when(editorUserRepository).selectByIds(any());


        Map<Long, String> userMap = fileService.getUserMap(fileList);

        assertAll(
            ()-> assertEquals(4, userMap.size()),
            ()-> assertEquals("testUser1", userMap.get(1L)),
            ()-> assertEquals("testUser2", userMap.get(2L)),
            ()-> assertEquals("testUser3", userMap.get(3L)),
            ()-> assertEquals("testUser4", userMap.get(4L))
        );
    }
}