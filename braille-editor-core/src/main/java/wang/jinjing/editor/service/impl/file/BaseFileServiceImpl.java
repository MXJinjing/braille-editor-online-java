package wang.jinjing.editor.service.impl.file;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import wang.jinjing.common.exception.ServiceException;
import wang.jinjing.common.pojo.ErrorEnum;
import wang.jinjing.common.util.BeanConvertUtil;
import wang.jinjing.editor.exception.ObjectStorageException;
import wang.jinjing.editor.pojo.VO.OssFileMetadataVO;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.editor.pojo.entity.OssFileMetadata;
import wang.jinjing.editor.pojo.entity.OssFileRecycle;
import wang.jinjing.editor.repository.EditorUserRepository;
import wang.jinjing.editor.repository.OssFileMetadataRepository;
import wang.jinjing.editor.repository.OssFileRecycleRepository;
import wang.jinjing.editor.service.file.BaseFileService;
import wang.jinjing.editor.service.oss.OssBucketService;
import wang.jinjing.editor.service.oss.OssObjectService;
import wang.jinjing.editor.util.FileHashUtils;
import wang.jinjing.editor.util.SecurityUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@Slf4j
public class BaseFileServiceImpl implements BaseFileService {

    @Autowired
    private OssObjectService ossObjectService;
    @Autowired
    private OssFileMetadataRepository ofmRepository;
    @Autowired
    private OssFileRecycleRepository ofrRepository;
    @Autowired
    private OssBucketService ossBucketService;
    @Autowired
    private EditorUserRepository editorUserRepository;
    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @NotNull
    protected static List<String> getPathSegments(String path) {
        String normalizedPath = path.replaceAll("/+", "/").replaceAll("\\\\", "/");
        return Arrays.stream(normalizedPath.split("/"))
                .filter(s -> !s.isEmpty()).toList();
    }

    protected static String  getParentPath(List<String> segments, int depth) {
        return segments.size()-depth <0 ? "" : segments.isEmpty() || segments.size()-depth == 0 ? "/":
                segments.stream().limit(segments.size()-depth).
                        collect(Collectors.joining("/","/","/"));
    }

    protected static String normalizePath(@NotBlank String path, boolean isDir) {
        List<String> pathSegments = getPathSegments(path);
        if(pathSegments.isEmpty()) {
            if(isDir) {
                return "/";
            }else {
                throw new IllegalArgumentException();
            }
        }else{
            if(isDir) {
                return pathSegments.stream().collect(Collectors.joining("/","/","/"));
            }else{
                return pathSegments.stream().collect(Collectors.joining("/","/",""));
            }
        }
    }

    @Override
    public void uploadFile(@NotBlank String bucketName, @NotBlank String path, @NotNull MultipartFile file, boolean overwrite) {
        try (InputStream inputStream = file.getInputStream()) {

            // 1. 准备用户元数据
            long contentLength = file.getSize();
            Date currentTime = new Date();
            EditorUser currentUser = SecurityUtils.getCurrentUser();

            String fileHash = FileHashUtils.calculateMD5(inputStream);
            inputStream.reset();
            path = normalizePath(path, true);

            // 2. 构造文件元数据

            Map<String, String> userMetadata = new HashMap<String, String>();
            userMetadata.put("b-e-create-user-id",String.valueOf(currentUser.getId()));
            userMetadata.put("b-e-create-user-name",String.valueOf(currentUser.getUsername()));
            userMetadata.put("b-e-content-hash", fileHash);


            // 3. 检查并创建父目录
            long lastParentId = findOrCreateParentFolder(bucketName, path);

            String fullPath = path + file.getOriginalFilename();
            String realFileName = file.getOriginalFilename();

            // 4. 检查文件是否存在及覆盖逻辑
            if (ossObjectService.objectExists(bucketName, fullPath)) {
                if (overwrite) {
                    // 原子性操作：删除旧文件及元数据
                    this.deleteObjectFromS3(bucketName, fullPath);
                } else {
                    fullPath = generateUniquePath(bucketName,fullPath,false);
                    List<String> pathSegments = getPathSegments(fullPath);
                    realFileName = pathSegments.get(pathSegments.size()-1);
                }
            }

            OssFileMetadata metadata = OssFileMetadata.builder()
                .fileSize(contentLength)
                .mimeType(file.getContentType())
                .s3Bucket(bucketName)
                .s3Key(fullPath)
                .createAt(currentTime)
                .createBy(currentUser.getId())
                .fileHash(fileHash)
                .realFileName(realFileName)
                .parentId(lastParentId)
                .parentPath(path)
                .build();



            // 5. 上传文件内容
            userMetadata.put("b-e-real-name",realFileName);
            ossObjectService.uploadStream(bucketName,fullPath, inputStream,contentLength,userMetadata);

            // 6. 保存元数据到数据库表中
            ofmRepository.insert(metadata);

        } catch (IOException | ServiceException e) {
            log.error(e.getMessage());
            throw new ServiceException(e);
        }

    }

    @Override
    public void uploadFileByBytes(String bucketName, String path, String fileName, String mimeType, byte[] bytes, boolean overwrite
    ) throws ObjectStorageException {
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            // 1. 准备用户元数据
            Date currentTime = new Date();
            EditorUser currentUser = SecurityUtils.getCurrentUser();

            String fileHash = FileHashUtils.calculateMD5(inputStream);
            inputStream.reset();
            path = normalizePath(path, true);

            // 2. 构造文件元数据

            Map<String, String> userMetadata = new HashMap<String, String>();
            userMetadata.put("b-e-create-user-id",String.valueOf(currentUser.getId()));
            userMetadata.put("b-e-create-user-name",String.valueOf(currentUser.getUsername()));
            userMetadata.put("b-e-content-hash", fileHash);

            // 3. 检查并创建父目录
            long lastParentId = findOrCreateParentFolder(bucketName, path);

            String fullPath = path + fileName;
            String realFileName = fileName;

            // 检查文件是否存在及覆盖逻辑（类似 uploadFile）
            if (ossObjectService.objectExists(bucketName, fullPath)) {
                if (overwrite) {
                    ossObjectService.deleteObject(bucketName, fullPath);
                } else {
                    // 处理重命名逻辑
                    fullPath = generateUniquePath(bucketName,fullPath,false);
                    List<String> pathSegments = getPathSegments(fullPath);
                    realFileName = pathSegments.get(pathSegments.size()-1);
                }
            }



            // 保存元数据
            OssFileMetadata metadata = OssFileMetadata.builder()
                .fileSize((long) bytes.length)
                .mimeType(mimeType)
                .s3Bucket(bucketName)
                .s3Key(fullPath)
                .createAt(currentTime)
                .createBy(currentUser.getId())
                .fileHash(fileHash)
                .realFileName(realFileName)
                .parentId(lastParentId)
                .parentPath(path)
                .build();

            // 上传文件流
            userMetadata.put("b-e-real-name",realFileName);
            ossObjectService.uploadStream(bucketName, fullPath, inputStream, (long) bytes.length, userMetadata);

            ofmRepository.insert(metadata);

        } catch (IOException | ServiceException e) {
            throw new ServiceException(e);
        }
    }


    @Override
    public InputStream downloadFile(@NotBlank String bucketName, @NotBlank String path) {
        try {
            return ossObjectService.downloadAsStream(bucketName, path);
        } catch (ObjectStorageException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] downloadAsBytes(@NotBlank String bucketName,@NotBlank String path) {
        try {
            return ossObjectService.downloadAsBytes(bucketName, path);
        } catch (ObjectStorageException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OssFileMetadataVO getMetadataByPath(String bucketName, String filePath, boolean isDir) {
        filePath = normalizePath(filePath,isDir);
        if (!ossBucketService.bucketExists(bucketName)) {
            throw new RuntimeException("存储桶不存在: " + bucketName);
        }

        OssFileMetadata metadata = ofmRepository.selectByPath(bucketName, filePath, isDir);
        boolean storageExists  = existsByPath(bucketName, filePath, isDir);

        if(!storageExists) {
           throw new RuntimeException("文件不存在:" + filePath);
        }
        OssFileMetadataVO vo = BeanConvertUtil.convertToVo(OssFileMetadataVO.class,metadata);


        Set<Long> userIds = new HashSet<>();
        userIds.add(metadata.getLastUpdateBy());
        userIds.add(metadata.getCreateBy());
        Map<Long, String> userMap = editorUserRepository.selectByIds(userIds)
                .stream()
                .collect(Collectors.toMap(EditorUser::getId, EditorUser::getUsername));

        // 填充用户数据
        fillVoUserName(userMap, metadata, vo);
        return vo;
    }


    @Override
    public boolean existsByPath(@NotBlank String bucketName, @NotBlank String filePath, boolean isDir) {
        filePath = normalizePath(filePath, isDir);

        // 1. 检查存储桶是否存在
        if (!ossBucketService.bucketExists(bucketName)) {
            throw new RuntimeException("BucketNotExists: 存储桶不存在: " + bucketName);
        }

        // 2. 获取存储层和数据库存在状态
        boolean metadataExists = ofmRepository.existsByPath(bucketName, filePath, isDir);

        if(!isDir){
            boolean storageExists = ossObjectService.objectExists(bucketName, filePath);
            if (!(Boolean) metadataExists && !storageExists) {
                throw new RuntimeException("FileNotFound: 文件不存在: " + filePath);
            }

            if (metadataExists && !storageExists) {
                throw new RuntimeException(
                        "FileNotInStorage: 元数据存在但存储中缺失 [bucket=" + bucketName + ", path=" + filePath + "]"
                );
            }

            if (!metadataExists) {
                throw new RuntimeException(
                        "FileNotInDatabase: 存储中存在但元数据缺失 [bucket=" + bucketName + ", path=" + filePath + "]"
                );
            }
        }

        return metadataExists;
    }



    @Override
    public List<OssFileMetadataVO> listFiles(@NotBlank String bucketName, @NotBlank String folderPath, Sort sort) {
        List<OssFileMetadataVO> list = new ArrayList<>();
        String path = normalizePath(folderPath, true);

        // 检测桶是否存在
        if (!ossBucketService.bucketExists(bucketName)) {
            throw new RuntimeException("BucketNotExists: 存储桶不存在: " + bucketName);
        }

        List<OssFileMetadata> metadataList = ofmRepository.listByPath(bucketName, path, sort);

        Map<Long, String> userMap = getUserMap(metadataList);


        for (OssFileMetadata metadata : metadataList) {
            OssFileMetadataVO vo = BeanConvertUtil.convertToVo(OssFileMetadataVO.class,metadata);
            fillVoUserName(userMap, metadata, vo);
            list.add(vo);
        }

        return list;
    }

    @NotNull Map<Long, String> getUserMap(List<OssFileMetadata> metadataList) {
        Set<Long> userIds = metadataList.stream()
                .flatMap(m -> Stream.of(m.getCreateBy(), m.getLastUpdateBy()))
                .collect(Collectors.toSet());
        Map<Long, String> map = new HashMap<>();
        List<EditorUser> users = editorUserRepository.selectByIds(userIds);
        for (EditorUser user : users) {
            map.put(user.getId(), user.getUsername());
        }
        return map;
    }

    @Override
    public Page<OssFileMetadataVO> listPagedFiles(String bucketName, String folderPath, int page, int size, Sort sort) {
        List<OssFileMetadataVO> list = new ArrayList<>();
        String path = normalizePath(folderPath, false);

        // 检测桶是否存在
        if (!ossBucketService.bucketExists(bucketName)) {
            throw new RuntimeException("BucketNotExists: 存储桶不存在: " + bucketName);
        }


        Page<OssFileMetadata> metadataList = ofmRepository.listPageByPath(bucketName, path, new Page<>(page,size), sort);

        Map<Long, String> userMap = getUserMap(metadataList.getRecords());

        for (OssFileMetadata metadata : metadataList.getRecords()) {
            OssFileMetadataVO vo = BeanConvertUtil.convertToVo(OssFileMetadataVO.class,metadata);
            fillVoUserName(userMap, metadata, vo);
            list.add(vo);
        }

        Page<OssFileMetadataVO> page1 = new Page<>(page,size);
        page1.setTotal(metadataList.getTotal());
        page1.setSize(metadataList.getSize());
        page1.getRecords().addAll(list);

        return page1;
    }

    @Override
    @Transactional
    public void moveToTrash(String bucketName, String path, Boolean isDir) {

        path = normalizePath(path, isDir);

        // 1. 获取文件信息，检测文件是否存在（方法内）
        OssFileMetadataVO metadata = getMetadataByPath(bucketName, path, isDir);

        // 2. 生成唯一的回收站路径
        // 如果是folder，以 "/" 结尾
        String trashPath = generateTrashPath(metadata);

        // 3. 将文件、目录移动到回收站
        moveObjectInBucket(bucketName, metadata.getS3Key(), trashPath, isDir);

        // 4. 更新元数据，将s3Key更新到新的路径
        ofmRepository.updatePath(metadata.getId(),trashPath);

        // 5. 创建回收站记录
        EditorUser currentUser = SecurityUtils.getCurrentUser();
        Date currentTime = new Date();
        OssFileRecycle ossFileRecycle = new OssFileRecycle();
        ossFileRecycle.setOriginFileId(metadata.getId());
        ossFileRecycle.setOperatedBy(currentUser.getId());
        ossFileRecycle.setRecycleAt(currentTime);
        ossFileRecycle.setRemainingDays(30);

        ofrRepository.insert(ossFileRecycle);
    }


    @Override
    public Map<String, ErrorEnum> moveToTrash(String bucketName, List<String> paths, List<Boolean> isDirs) {

        if (paths.size() != isDirs.size()) {
            throw new IllegalArgumentException("路径列表与目录标记列表长度不一致");
        }

        Map<String, ErrorEnum> errorMap = new ConcurrentHashMap<>();
        IntStream.range(0, paths.size()).parallel().forEach(i -> {
            String path = paths.get(i);
            boolean isDir = isDirs.get(i);

            try {
                // 复用单个移动逻辑
                this.moveToTrash(bucketName, path, isDir);
            } catch (Exception e) {
                errorMap.put(path, ErrorEnum.DELETE_FAIL);
            }
        });
        return errorMap;
    }

    @Override
    public Map<Long, ErrorEnum> recoveryFromTrash(String bucketName, List<Long> recycleIds) {
        Map<Long, ErrorEnum> errorMap = new ConcurrentHashMap<>();

        recycleIds.parallelStream().forEach(recycleId -> {
            try {
                OssFileRecycle recycle = ofrRepository.selectById(recycleId);
                if (recycle == null) {
                    errorMap.put(recycleId, ErrorEnum.RECYCLE_RECORD_NOT_FOUND);
                    return;
                }

                OssFileMetadata metadata = ofmRepository.selectById(recycle.getOriginFileId());
                if (metadata == null) {
                    errorMap.put(recycleId, ErrorEnum.FILE_METADATA_NOT_FOUND);
                    return;
                }

                // 检查原路径是否被占用
                String originPath = metadata.getS3Key();
                String recoveryPath = originPath;

                // 冲突检测与重命名（复用上传逻辑）
                while (existsByPath(bucketName, recoveryPath, metadata.getIsDir())) {
                    recoveryPath = generateRenamedPath(recoveryPath, metadata.getIsDir());
                }

                // 移动回原路径
                moveObjectInBucket(bucketName, metadata.getS3Key(), metadata.getS3Key(), metadata.getIsDir());

                // 删除回收站记录
                ofrRepository.deleteById(recycleId);
            } catch (Exception e) {
                errorMap.put(recycleId, ErrorEnum.CREATE_FAIL);
            }
        });
        return errorMap;
    }

    @Override
    public Map<Long, ErrorEnum> realDeleteFiles(String bucketName, List<Long> deleteIds) {
        Map<Long, ErrorEnum> errorMap = new ConcurrentHashMap<>();

        deleteIds.parallelStream().forEach(deleteId -> {
            try {
                OssFileRecycle recycle = ofrRepository.selectById(deleteId);
                if (recycle == null) {
                    errorMap.put(deleteId, ErrorEnum.RECYCLE_RECORD_NOT_FOUND);
                    return;
                }

                // 获取元数据并验证
                OssFileMetadata metadata = ofmRepository.selectById(recycle.getOriginFileId());
                if (metadata == null || !metadata.getS3Bucket().equals(bucketName)) {
                    errorMap.put(deleteId, ErrorEnum.FILE_METADATA_NOT_FOUND);
                    return;
                }

                // 物理删除存储对象
                if (metadata.getIsDir()) {
                    List<String> objects = ofmRepository.listByPath(bucketName, metadata.getS3Key(), Sort.unsorted())
                            .stream().map(OssFileMetadata::getS3Key).toList();
                    objects.forEach(obj -> deleteObjectFromS3(bucketName, obj));
                } else {
                    deleteObjectFromS3(bucketName, metadata.getS3Key());
                }

                // 删除元数据
                ofmRepository.deleteById(metadata.getId());
                // 删除回收站记录
                ofrRepository.deleteById(deleteId);

            } catch (Exception e) {
                errorMap.put(deleteId, ErrorEnum.DELETE_FAIL);
            }
        });
        return errorMap;
    }

    @Override
    public long mkDir(String bucketName, String folderPath) {

        List<String> pathSegments = getPathSegments(folderPath);
        String parentPath = getParentPath(pathSegments, 1);
        String realName = parentPath.isEmpty()? "" : pathSegments.get(pathSegments.size()-1);

        OssFileMetadata parentDir = ofmRepository.selectByPath(bucketName, parentPath, true);
        if (parentDir == null && !parentPath.isEmpty()) {
            throw new ServiceException("父目录不存在: " + parentPath);
        }


        Date currentTime = new Date();
        EditorUser currentUser = SecurityUtils.getCurrentUser();


        OssFileMetadata newFolder = new OssFileMetadata();
        newFolder.setS3Bucket(bucketName);
        newFolder.setS3Key(folderPath);
        newFolder.setCreateAt(currentTime);
        newFolder.setUpdateAt(currentTime);
        newFolder.setIsDir(true);
        newFolder.setCreateBy(currentUser.getId());
        newFolder.setLastUpdateBy(currentUser.getId());
        newFolder.setRealFileName(realName);
        if(Objects.isNull(parentDir)){
            newFolder.setParentPath("");
        }else{
            newFolder.setParentId(parentDir.getId());
            newFolder.setParentPath(parentPath);
        }


        ofmRepository.insert(newFolder);

        return newFolder.getId();
    }

    @Override
    public long mkDirsWithParent(String bucketName, String pathWithFolderName) {
        return findOrCreateParentFolder(bucketName, pathWithFolderName);
    }

    protected long findOrCreateParentFolder(String bucketName, String pathWithFolderName) {
        List<String> segments = getPathSegments(pathWithFolderName);
        ArrayDeque<String> parentToCreate = new ArrayDeque<>();
        Long lastPathId = 0L;

        // 从深到浅检查路径
        for(int i=1; i<=segments.size(); i++){
            String parentPath = getParentPath(segments,i);
            OssFileMetadata parent = ofmRepository.selectByPath(bucketName,parentPath,true);
            if(parent == null){
                log.info("to create: "+parentPath);
                parentToCreate.push(parentPath);
            }else{
                lastPathId = parent.getId();
                break;
            }
        }

        // 创建缺失的父目录
        while(!parentToCreate.isEmpty()){
            String pathToCreate = parentToCreate.pop();
            lastPathId = mkDir(bucketName,pathToCreate);
        }
        return lastPathId;
    }

    @Transactional(rollbackFor = Exception.class)
    protected void moveObjectInBucket(String bucketName, String srcPath, String destPath, Boolean isDir) {
        try {
            if (isDir) {
                // 获取目录下所有元数据记录（包含子目录）
                List<OssFileMetadata> children = ofmRepository.listRecursiveByPath(bucketName, srcPath);

                // 批量移动元数据
                children.forEach(metadata -> {
                    String newPath = metadata.getS3Key().replace(srcPath, destPath);
                    // 更新元数据路径，从 srcPath 到 newPath
                    updatePath(metadata.getId(), newPath, bucketName);
                    // 物理移动存储对象
                    ossObjectService.copyObject(bucketName, metadata.getS3Key(), bucketName, newPath);
                    ossObjectService.deleteObject(bucketName, metadata.getS3Key());
                });
                // 删除原目录元数据
                ofmRepository.deleteByPath(bucketName, srcPath);
            } else {
                // 文件移动逻辑
                OssFileMetadata meta = ofmRepository.selectByPath(bucketName, srcPath, false);
                updatePath(meta.getId(), destPath, bucketName); // 更新路径和父级
                ossObjectService.copyObject(bucketName, srcPath, bucketName, destPath);
                ossObjectService.deleteObject(bucketName, srcPath);
            }
        } catch (ObjectStorageException e) {
            throw new ServiceException(ErrorEnum.FILE_MOVE_FAIL);
        }
    }

    /**
     * 在OSS存储进行移动文件之前，设置metaData的路径为新路径
     * @param fileId
     * @param newPath
     * @param bucketName
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePath(Long fileId, String newPath, String bucketName) {
        // 获取原始元数据
        OssFileMetadata metadata = ofmRepository.selectById(fileId);
        if (metadata == null) {
            throw new ServiceException(ErrorEnum.FILE_METADATA_NOT_FOUND);
        }

        // 解析新路径的父目录
        String newParentPath = getParentPathFromFullPath(newPath);

        // 创建或获取父目录ID
        Long newParentId = findOrCreateParentFolder(bucketName, newParentPath);

        // 构建更新参数
        OssFileMetadata update = new OssFileMetadata();
        update.setId(fileId);
        update.setS3Key(newPath);
        update.setParentId(newParentId);
        update.setParentPath(newParentPath);

        // 执行更新
        if (ofmRepository.updateById(update) <= 0) {
            throw new ServiceException(ErrorEnum.FILE_UPDATE_FAILED);
        }

        // 如果是目录，递归更新子项
        if (metadata.getIsDir()) {
            updateChildrenPathRecursive(bucketName, metadata.getS3Key(), newPath, newParentId);
        }
    }

    /**
     * 递归更新子项路径
     */
    private void updateChildrenPathRecursive(String bucketName, String oldParentPath,
                                             String newParentPath, Long newParentId) {
        // 查询所有子项
        List<OssFileMetadata> children = ofmRepository.listByPath(bucketName, oldParentPath,Sort.unsorted());

        children.parallelStream().forEach(child -> {
            // 计算新路径
            String newChildPath = child.getS3Key().replace(oldParentPath, newParentPath);

            // 构建更新参数
            OssFileMetadata update = new OssFileMetadata();
            update.setId(child.getId());
            update.setS3Key(newChildPath);

            String actualParentPath = getParentPathFromFullPath(newChildPath);
            Long actualParentId = findOrCreateParentFolder(bucketName, actualParentPath);

            update.setParentId(actualParentId);
            update.setParentPath(actualParentPath);

            // 执行更新
            if (ofmRepository.updateById(update) <= 0) {
                throw new ServiceException(ErrorEnum.FILE_UPDATE_FAILED);
            }

            // 递归处理子目录
            if (child.getIsDir()) {
                updateChildrenPathRecursive(bucketName, child.getS3Key(), newChildPath, child.getId());
            }
        });
    }


    @Override
    public void copyObject(String sourceBucket, String sourcePath,
                           String destBucket, String destPath,
                           boolean overwrite, boolean isDir) {
        // 规范化路径并校验参数
        sourcePath = normalizePath(sourcePath, isDir);
        destPath = normalizePath(destPath, isDir);

        // 校验源对象存在性
        OssFileMetadata sourceMeta = ofmRepository.selectByPathWithLock(sourceBucket, sourcePath, isDir);
        if (sourceMeta == null)
            throw new ServiceException(ErrorEnum.FILE_METADATA_NOT_FOUND);

        // 处理目标路径冲突
        String finalDestPath = resolveDestinationConflict(destBucket, destPath, overwrite, isDir);

        try {
            if (isDir) {
                copyDirectoryRecursive(sourceBucket, sourcePath, destBucket, finalDestPath);
            } else {
                // 创建目标父目录并获取parentId
                long parentId = findOrCreateParentFolder(destBucket, getParentPathFromFullPath(finalDestPath));

                // 执行文件复制
                ossObjectService.copyObject(sourceBucket, sourcePath, destBucket, finalDestPath);

                // 创建带有正确parentId的元数据
                createDestinationMetadata(sourceMeta, destBucket, finalDestPath, parentId);
            }
        } catch (ObjectStorageException e) {
            throw new ServiceException(ErrorEnum.FILE_COPY_FAIL);
        }
    }

    private void copyDirectoryRecursive(String srcBucket, String srcDir,
                                        String destBucket, String destDir) {
        // 1. 标准化目录路径格式
        srcDir = normalizePath(srcDir, true);
        destDir = normalizePath(destDir, true);

        // 2. 创建目标根目录并获取其ID
        long destRootId = mkDirsWithParent(destBucket, destDir);

        // 3. 查询源目录所有子项（使用高效前缀查询）
        List<OssFileMetadata> children = ofmRepository.listRecursiveByPath(srcBucket, srcDir);

        // 4. 按路径层级排序，确保先创建父目录
        children.sort(Comparator.comparing(OssFileMetadata::getS3Key));

        for (OssFileMetadata child : children) {
            // 5. 生成目标路径
            String destPath = child.getS3Key().replace(srcDir, destDir);

            // 6. 计算父目录信息
            String destParentPath = getParentPathFromFullPath(destPath);
            long destParentId = destParentPath.equals(destDir) ? destRootId
                    : mkDirsWithParent(destBucket, destParentPath);

            try {
                // 7. 物理文件复制
                ossObjectService.copyObject(srcBucket, child.getS3Key(),
                        destBucket, destPath);

                // 8. 创建带正确层级关系的元数据
                createDestinationMetadata(child, destBucket, destPath, destParentId);
            } catch (ObjectStorageException e) {
                throw new ServiceException(ErrorEnum.SUB_COPY_FAIL);
            }
        }
    }


    /**
     * 创建目标元数据（增强版）
     */
    private void createDestinationMetadata(OssFileMetadata sourceMetadata,
                                           String destBucket, String destPath,
                                           long parentId) {
        EditorUser currentUser = SecurityUtils.getCurrentUser();
        Date now = new Date();

        OssFileMetadata destMetadata = new OssFileMetadata();
        destMetadata.setS3Bucket(destBucket);
        destMetadata.setS3Key(destPath);
        destMetadata.setRealFileName(sourceMetadata.getRealFileName());
        destMetadata.setFileSize(sourceMetadata.getFileSize());
        destMetadata.setMimeType(sourceMetadata.getMimeType());
        destMetadata.setCreateAt(now);
        destMetadata.setUpdateAt(now);
        destMetadata.setCreateBy(currentUser.getId());
        destMetadata.setLastUpdateBy(currentUser.getId());
        destMetadata.setParentId(parentId); // 关键修改：使用传入的parentId
        destMetadata.setParentPath(getParentPathFromFullPath(destPath));
        destMetadata.setFileHash(sourceMetadata.getFileHash());

        ofmRepository.insert(destMetadata);
    }


    private String resolveDestinationConflict(String bucket, String path,
                                              boolean overwrite, boolean isDir) {
        // 检查路径冲突
        boolean exists = ofmRepository.existsByPath(bucket, path, isDir)
                || ossObjectService.objectExists(bucket, path);

        if (!exists) return path;

        if (overwrite) {
            // 强制覆盖逻辑
            if (isDir) {
                // 强制检测是否以/结尾
                if(path.endsWith("/")){
                    ofmRepository.deleteByPath(bucket, path);
                    ossObjectService.deleteObjectsByPrefix(bucket, path);
                }
            } else {
                ofmRepository.deleteByPath(bucket, path);
                ossObjectService.deleteObject(bucket, path);
            }
            return path;
        } else {
            // 智能重命名策略
            return generateUniquePath(bucket, path, isDir);
        }
    }

    private String generateUniquePath(String bucket, String basePath, boolean isDir) {
        String newPath = basePath;
        int attempt = 1;

        if(isDir) {
            while (ofmRepository.existsByPath(bucket, newPath, isDir)
                    || ossObjectService.objectExists(bucket, newPath)) {
                String suffix = String.format("(%d)", attempt++);
                newPath = insertSuffixBeforeExtension(basePath, suffix, isDir);
            }
            return newPath;
        }else {
            while(ossObjectService.objectExists(bucket, basePath)) {
                newPath = handleDestinationExistenceAndRename(bucket, basePath, false, false);
            }
            return newPath;
        }
    }

    private String insertSuffixBeforeExtension(String path, String suffix, boolean isDir) {
        if (isDir) {
            return path.replaceAll("/$", "") + suffix + "/";
        }

        int dotIndex = path.lastIndexOf('.');
        if (dotIndex > 0) {
            return path.substring(0, dotIndex) + suffix + path.substring(dotIndex);
        }
        return path + suffix;
    }

    protected String handleDestinationExistenceAndRename(String destBucket, String destPath,
                                                boolean overwrite, boolean isDir) {
        boolean destExists = (ofmRepository.selectByPath(destBucket, destPath, false) != null)
                && ossObjectService.objectExists(destBucket, destPath);

        if (destExists) {
            if (overwrite) {
                // 原子化删除目标文件
                deleteObjectFromS3(destBucket, destPath);
                ofmRepository.deleteByPath(destBucket, destPath);
                return destPath;
            } else {
                return generateRenamedPath(destPath, isDir);
            }
        }
        return destPath;
    }

    private String generateRenamedPath(String originalPath, boolean isDir) {
        try {
            List<String> segments = getPathSegments(originalPath);
            String fileName = segments.get(segments.size()-1);

            // 处理带扩展名的文件
            int dotIndex = fileName.lastIndexOf('.');
            String baseName = (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
            String extension = (dotIndex > 0) ? fileName.substring(dotIndex) : "";

            // 查找已有编号
            Matcher m = Pattern.compile("(.*?)(\\$(\\d+)\\$)?").matcher(baseName);
            if (m.matches()) {
                baseName = m.group(1);
                int number = (m.group(3) != null) ? Integer.parseInt(m.group(3)) : 0;
                return getParentPath(segments, 1) +
                        baseName + "(" + (number+1) + ")" + extension +
                        (isDir ? "/" : "");
            }
            return originalPath + "(1)";
        } catch (NumberFormatException e) {
            log.error("路径重命名失败 originalPath={}, isDir={}", originalPath, isDir, e);
            throw new ServiceException("自动重命名失败");
        }
    }

    protected String generateTrashPath(OssFileMetadataVO ossFileMetadata) {
        return  ".trash/"+ System.currentTimeMillis() + ossFileMetadata.getS3Key();
    }

    private String getParentPathFromFullPath(String path) {
        List<String> segments = getPathSegments(path);
        return segments.size() > 1 ?
                "/" + String.join("/", segments.subList(0, segments.size()-1)) + "/" :
                "/";
    }

    void deleteObjectFromS3(@NotBlank String bucketName, @NotBlank String path) {
        try {
            ossObjectService.deleteObject(bucketName, path);
        } catch (ObjectStorageException e) {
            if(ossObjectService.objectExists(bucketName, path)){
                throw new RuntimeException(e);
            }
        }
    }

    private void fillVoUserName(@NotNull Map<Long,String> userMap, @NotNull OssFileMetadata metadata, @NotNull OssFileMetadataVO vo) {
        vo.setCreateByUsername(userMap.getOrDefault(vo.getCreateBy(),""));
        vo.setLastUpdateByUsername(userMap.getOrDefault(vo.getLastUpdateBy(),""));
    }
}


