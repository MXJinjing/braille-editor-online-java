package wang.jinjing.editor.service.impl.file;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import wang.jinjing.common.exception.ServiceException;
import wang.jinjing.common.pojo.ErrorEnum;
import wang.jinjing.common.pojo.FileTypeEnum;
import wang.jinjing.common.util.BeanConvertUtil;
import wang.jinjing.editor.exception.ObjectStorageException;
import wang.jinjing.editor.pojo.VO.OssFileMetadataVO;
import wang.jinjing.editor.pojo.VO.OssRecycleMetadataVO;
import wang.jinjing.editor.pojo.entity.EditorUser;
import wang.jinjing.editor.pojo.entity.OssFileMetadata;
import wang.jinjing.editor.repository.EditorUserRepository;
import wang.jinjing.editor.repository.OssFileMetadataRepository;
import wang.jinjing.editor.service.file.BaseFileService;
import wang.jinjing.editor.service.impl.secure.UserDetailServiceImpl;
import wang.jinjing.editor.service.oss.S3BucketService;
import wang.jinjing.editor.service.oss.S3ObjectService;
import wang.jinjing.editor.util.FileExtUtils;
import wang.jinjing.editor.util.FileHashUtils;
import wang.jinjing.editor.util.RandomUtil;
import wang.jinjing.editor.util.SecurityUtils;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class BaseFileServiceImpl implements BaseFileService {

    @Autowired
    private S3ObjectService s3ObjectService;

    @Autowired
    private OssFileMetadataRepository metadataRepository;

    @Autowired
    private S3BucketService s3BucketService;

    @Autowired
    private EditorUserRepository editorUserRepository;

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @NotNull
    protected static List<String> getPathSegments(String path) {
        String normalizedPath = path.replaceAll("/+", "/").replaceAll("\\\\", "/");
        return Arrays.stream(normalizedPath.split("/"))
                .filter(s -> !s.isEmpty()).toList();
    }

    protected static String  getParentPath(List<String> segments, int depth) {
        return segments.size()-depth <0 ? "" : segments.isEmpty() || segments.size()-depth == 0 ? "/":
                segments.stream().limit(segments.size()-depth).
                        collect(Collectors.joining("/","/",""));
    }

    protected static String  getParentPath(String path) {
        List<String> segments = getPathSegments(path);
        return  segments.isEmpty()? "/":
                segments.stream().limit(segments.size()-1).
                        collect(Collectors.joining("/","/",""));
    }

    protected static String getRealName(List<String> segments) {
        return segments.isEmpty() ? "" : segments.get(segments.size()-1);
    }

    protected String getS3KeyFromPath(String bucketName ,String path) {
        OssFileMetadata ossFileMetadata = metadataRepository.selectByPath(bucketName, path, FileTypeEnum.ALL);
        if(ossFileMetadata == null){
            throw new ServiceException("文件不存在: " + path,ErrorEnum.FILE_METADATA_NOT_FOUND);
        }
        return ossFileMetadata.getS3Key();
    }



    protected String generateS3Key(String bucketname, String parentS3Key, String realFileName) {
        String s = RandomUtil.generateDigits(5);
        // 分割文件名和拓展名
        int i = realFileName.lastIndexOf(".");
        String fileName = i == -1 ? realFileName : realFileName.substring(0, i);
        String fileExt = i == -1 ? "" : realFileName.substring(i);

        // 生成唯一的文件名
        String uniqueFileName = String.format("%s@%s%s", fileName, s, fileExt);
        String s3Key = parentS3Key + "/" + uniqueFileName;
        if(metadataRepository.existsByS3Key(bucketname,s3Key)){
            // 如果文件名已存在，递归调用生成新的文件名
            return generateS3Key(bucketname, parentS3Key, realFileName);
        }
        return normalizePath(s3Key);
    }


    protected static String normalizePath(@NotBlank String path) {
        List<String> pathSegments = getPathSegments(path);
        if(pathSegments.isEmpty()) {
            return "/";
        }else{
            return pathSegments.stream().collect(Collectors.joining("/","/",""));
        }
    }



    @Override
    public Page<OssFileMetadataVO> search(String bucket,
                                          String realFileName,
                                          String path,
                                          Boolean isDir,
                                          Date createAtStart, Date createAtEnd,
                                          Date lastModifiedAtStart, Date lastModifiedAtEnd,
                                          String createByUsername, String lastModifiedByUsername,
                                          String mimeType,
                                          Integer page, Integer size, Sort sort) {
        Long createBy = null;
        Long lastModifiedBy = null;
        if(StrUtil.isNotBlank(createByUsername)){
            createBy = ((EditorUser) userDetailService.loadUserByUsername(createByUsername)).getId();
        }
        if(StrUtil.isNotBlank(lastModifiedByUsername)){
            lastModifiedBy = ((EditorUser) userDetailService.loadUserByUsername(lastModifiedByUsername)).getId();
        }

        // 1. 检查存储桶是否存在
        if (!s3BucketService.bucketExists(bucket)) {
            throw new ServiceException(ErrorEnum.BUCKET_NOT_EXIST);
        }

        Page<OssFileMetadata> entities = metadataRepository.searchPage(
                bucket,
                realFileName,
                path,
                isDir,
                createAtStart, createAtEnd,
                lastModifiedAtStart, lastModifiedAtEnd,
                createBy, lastModifiedBy,
                mimeType,
                new Page<>(page, size),
                sort
        );

        Map<Long, String> userMap = getUserMap(entities.getRecords());

        Page<OssFileMetadataVO> vos = BeanConvertUtil.convertToVoPage(OssFileMetadataVO.class, entities);
        for(OssFileMetadataVO vo: vos.getRecords()){
            // 填充用户信息
            fillVoUserName(userMap, vo);
        }

        return vos;
    }

    @Transactional
    @Override
    public OssFileMetadataVO uploadFile(@NotBlank String bucketName, @NotBlank String path, @NotNull MultipartFile file) {
        // 检测桶是否存在
        if (!s3BucketService.bucketExists(bucketName)) {
            throw new ServiceException(ErrorEnum.USER_BUCKET_NOT_INIT);
        }

        try {
            InputStream inputStream = file.getInputStream();

            // 1. 准备用户元数据
            long contentLength = file.getSize();
            Date currentTime = new Date();
            EditorUser currentUser = SecurityUtils.getCurrentUser();


            String fileHash = FileHashUtils.calculateMD5(file.getInputStream());
            path = normalizePath(path);

            // 2. 构造文件元数据
            Map<String, String> userMetadata = getUserMetadataMap(currentUser, fileHash);

            // 3. 检查并创建父目录
            mkdirWithParent(bucketName, path, currentUser);
            String realFileName = file.getOriginalFilename();

            // 4. 检查文件是否存在及覆盖逻辑
            boolean existsByPath = metadataRepository.existsByPath(bucketName, path, FileTypeEnum.ALL);

            if (existsByPath) {
                realFileName = generateUniqueFileName(bucketName,path,realFileName,false);
            }

            String fullPath = normalizePath(path + "/" + realFileName);
            String s3Key = getGeneratedS3KeyFromPath(bucketName, path, realFileName);

            OssFileMetadata metadata = OssFileMetadata.builder()
                .path(fullPath)
                .fileSize(contentLength)
                .mimeType(file.getContentType())
                .s3Bucket(bucketName)
                .s3Key(s3Key)
                .createAt(currentTime).lastModifiedAt(currentTime)
                .createBy(currentUser.getId()).lastModifiedBy(currentUser.getId())
                .fileHash(fileHash)
                .realFileName(realFileName)
                .parentPath(path)
                .build();

            // 5. 上传文件内容
            userMetadata.put("b-e-real-name",realFileName);
            s3ObjectService.uploadStream(bucketName, s3Key, inputStream,contentLength,userMetadata);

            // 6. 保存元数据到数据库表中
            metadataRepository.insert(metadata);
            inputStream.close();

            // 7. 返回文件元数据
            OssFileMetadataVO ossFileMetadataVO = BeanConvertUtil.convertToVo(OssFileMetadataVO.class, metadata);
            Map<Long, String> userMap = getUserMap(Collections.singletonList(metadata));
            fillVoUserName(userMap, ossFileMetadataVO);
            return ossFileMetadataVO;
        } catch (IOException | ServiceException e) {
            throw new ServiceException(e);
        }

    }

    @Override
    public OssFileMetadataVO updateFile(String bucketName, String path, MultipartFile file) {
        // 检测桶是否存在
        if (!s3BucketService.bucketExists(bucketName)) {
            throw new ServiceException(ErrorEnum.USER_BUCKET_NOT_INIT);
        }
        path = normalizePath(path);

        // 获取文件元信息
        OssFileMetadata metadata = metadataRepository.selectByPath(bucketName, path, FileTypeEnum.ALL);
        if(Objects.nonNull(metadata)){
            if(metadata.getIsDir()){
                throw new ServiceException(ErrorEnum.DEST_FILE_TYPE_CONFLICT);
            }
            try {
                InputStream inputStream = file.getInputStream();

                // 1. 准备用户元数据
                long contentLength = file.getSize();
                Date currentTime = new Date();
                EditorUser currentUser = SecurityUtils.getCurrentUser();
                String fileHash = FileHashUtils.calculateMD5(file.getInputStream());

                // 如果MD5相同，则不需要上传
                if(fileHash.equals(metadata.getFileHash())){
                    return BeanConvertUtil.convertToVo(OssFileMetadataVO.class, metadata);
                }

                Map<String, String> userMetadata = getUserMetadataMap(currentUser, fileHash);


                String oldS3Key = metadata.getS3Key();
                String newS3Key = getGeneratedS3KeyFromPath(bucketName,metadata.getParentPath(),metadata.getRealFileName());

                // 2. 构造文件元数据
                Map<String, String> userMetadataMap = getUserMetadataMap(currentUser, fileHash);
                userMetadataMap.put("b-e-real-name", metadata.getRealFileName());

                // 3. 上传文件内容
                try {
                    s3ObjectService.uploadStream(bucketName, newS3Key, inputStream, contentLength, userMetadataMap);
                    s3ObjectService.deleteObject(bucketName, oldS3Key);
                } catch (ObjectStorageException e) {
                    // 如果上传失败则回退，删除新的文件
                    if(s3ObjectService.objectExists(bucketName, newS3Key)){
                        s3ObjectService.deleteObject(bucketName, newS3Key);
                    }
                    throw new ServiceException(e);
                }

                // 4. 更新元数据
                metadata.setFileSize(contentLength);
                metadata.setFileHash(fileHash);
                metadata.setMimeType(file.getContentType());
                metadata.setS3Key(newS3Key);
                metadata.setLastModifiedAt(currentTime);
                metadata.setLastModifiedBy(currentUser.getId());

                int i = metadataRepository.updateById(metadata);

                // 填充用户信息
                OssFileMetadataVO ossFileMetadataVO = BeanConvertUtil.convertToVo(OssFileMetadataVO.class, metadata);
                Map<Long, String> userMap = getUserMap(Collections.singletonList(metadata));
                fillVoUserName(userMap,ossFileMetadataVO);

                // 5. 返回文件元数据
                return ossFileMetadataVO;
            } catch (IOException | ServiceException e) {
                throw new ServiceException(e);
            }
        }else{
            // 如果文件不存在, 调用上传方法
            return uploadFile(bucketName, path, file);
        }
    }

    @Transactional
    @Override
    public OssFileMetadataVO uploadFileByBytes(String bucketName, String path, String fileName, String mimeType, byte[] bytes) {
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            // 1. 准备用户元数据
            Date currentTime = new Date();
            EditorUser currentUser = SecurityUtils.getCurrentUser();

            String fileHash = FileHashUtils.calculateMD5(inputStream);
            inputStream.reset();
            path = normalizePath(path);

            // 2. 构造文件元数据
            Map<String, String> userMetadata = getUserMetadataMap(currentUser, fileHash);

            // 3. 检查并创建父目录
            mkdirWithParent(bucketName, path, currentUser);

            String fullPath = normalizePath(path + "/" + fileName);
            String realFileName = fileName;
            String s3Key = getGeneratedS3KeyFromPath(bucketName, path, fileName);

            // 检查文件是否存在及覆盖逻辑（类似 uploadFile）
            if (metadataRepository.existsByPath(bucketName,path,FileTypeEnum.ALL)) {
                // 处理重命名逻辑
                fullPath = generateUniquePath(bucketName,fullPath,false);
                List<String> pathSegments = getPathSegments(fullPath);
                realFileName = pathSegments.get(pathSegments.size()-1);
            }

            // 保存元数据
            OssFileMetadata metadata = OssFileMetadata.builder()
                .path(fullPath)
                .fileSize((long) bytes.length)
                .mimeType(mimeType)
                .s3Bucket(bucketName)
                .s3Key(s3Key)
                .createAt(currentTime).lastModifiedAt(currentTime)
                .createBy(currentUser.getId()).lastModifiedBy(currentUser.getId())
                .fileHash(fileHash)
                .realFileName(realFileName)
                .parentPath(path)
                .build();

            // 上传文件流
            userMetadata.put("b-e-real-name",realFileName);
            s3ObjectService.uploadStream(bucketName, s3Key, inputStream, bytes.length, userMetadata);

            metadataRepository.insert(metadata);

            // 填充用户信息
            OssFileMetadataVO ossFileMetadataVO = BeanConvertUtil.convertToVo(OssFileMetadataVO.class, metadata);
            Map<Long, String> userMap = getUserMap(Collections.singletonList(metadata));
            fillVoUserName(userMap,ossFileMetadataVO);

            // 5. 返回文件元数据
            return ossFileMetadataVO;

        } catch (IOException | ServiceException e) {
            throw new ServiceException(e);
        }
    }

    private String getGeneratedS3KeyFromPath(String bucketName, String path, String fileName) {
        return generateS3Key(bucketName,getS3KeyFromPath(bucketName, path), fileName);
    }

    @NotNull
    private static Map<String, String> getUserMetadataMap(EditorUser currentUser, String fileHash) {
        Map<String, String> userMetadata = new HashMap<>();
        userMetadata.put("b-e-create-user-id",String.valueOf(currentUser.getId()));
        userMetadata.put("b-e-create-user-name",String.valueOf(currentUser.getUsername()));
        userMetadata.put("b-e-content-hash", fileHash);
        return userMetadata;
    }


    @Override
    public InputStream downloadFile(@NotBlank String bucketName, @NotBlank String path) {
        try {
            return s3ObjectService.downloadAsStream(bucketName, getS3KeyFromPath(bucketName, path));
        } catch (ObjectStorageException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public byte[] downloadAsBytes(@NotBlank String bucketName,@NotBlank String path) {
        try {
            return s3ObjectService.downloadAsBytes(bucketName, getS3KeyFromPath(bucketName, path));
        } catch (ObjectStorageException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    @Transactional
    public OssFileMetadataVO getMetadataByPath(String bucketName, String filePath, FileTypeEnum file) {
        filePath = normalizePath(filePath);
        if (!s3BucketService.bucketExists(bucketName)) {
            throw new ServiceException(ErrorEnum.BUCKET_NOT_EXIST);
        }

        OssFileMetadata metadata = metadataRepository.selectByPath(bucketName, filePath, FileTypeEnum.ALL);
        if (metadata == null) {
            throw new ServiceException(ErrorEnum.FILE_METADATA_NOT_FOUND);
        }
        OssFileMetadataVO vo = BeanConvertUtil.convertToVo(OssFileMetadataVO.class,metadata);


        Map<Long, String> userMap = getUserMap(Collections.singletonList(metadata));
        // 填充用户数据
        fillVoUserName(userMap, vo);
        return vo;
    }


    @Override
    public boolean existsByPath(@NotBlank String bucketName, @NotBlank String filePath, FileTypeEnum fileType) {
        filePath = normalizePath(filePath);

        // 1. 检查存储桶是否存在
        if (!s3BucketService.bucketExists(bucketName)) {
            throw new ServiceException("BucketNotExists: 存储桶不存在: " + bucketName);
        }

        // 2. 获取存储层和数据库存在状态
        return metadataRepository.existsByPath(bucketName,filePath,fileType);
    }



    @Override
    public List<OssFileMetadataVO> listFiles(@NotBlank String bucketName, @NotBlank String folderPath, Sort sort) {
        List<OssFileMetadataVO> list = new ArrayList<>();
        String path = normalizePath(folderPath);

        // 检测桶是否存在
        if (!s3BucketService.bucketExists(bucketName)) {
            throw new ServiceException(ErrorEnum.USER_BUCKET_NOT_INIT);
        }

        // 检测路径是否存在
        if (!metadataRepository.existsByPath(bucketName, path, FileTypeEnum.FOLDER)) {
            throw new ServiceException(ErrorEnum.PATH_NOT_EXISTS);
        }

        List<OssFileMetadata> metadataList = metadataRepository.listByPath(bucketName, path, sort);

        Map<Long, String> userMap = getUserMap(metadataList);


        for (OssFileMetadata metadata : metadataList) {
            OssFileMetadataVO vo = BeanConvertUtil.convertToVo(OssFileMetadataVO.class,metadata);
            fillVoUserName(userMap, vo);
            list.add(vo);
        }

        return list;
    }

    @NotNull Map<Long, String> getUserMap(List<OssFileMetadata> metadataList) {
        Set<Long> userIds = metadataList.stream()
                .flatMap(m -> Stream.of(m.getCreateBy(), m.getLastModifiedBy(), m.getDeletedBy()).filter(Objects::nonNull))
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> userMap = editorUserRepository.selectByIds(userIds)
                .stream()
                .collect(Collectors.toMap(EditorUser::getId, EditorUser::getUsername));
        return userMap;
    }


    @Override
    public Page<OssFileMetadataVO> listPagedFiles(String bucketName, String folderPath, int page, int size, Sort sort) {
        List<OssFileMetadataVO> list = new ArrayList<>();
        String path = normalizePath(folderPath);

        // 检测桶是否存在
        if (!s3BucketService.bucketExists(bucketName)) {
            throw new ServiceException(ErrorEnum.USER_BUCKET_NOT_INIT);
        }


        Page<OssFileMetadata> metadataList = metadataRepository.listPageByPath(bucketName, path, new Page<>(page,size), sort);

        Map<Long, String> userMap = getUserMap(metadataList.getRecords());

        for (OssFileMetadata metadata : metadataList.getRecords()) {
            OssFileMetadataVO vo = BeanConvertUtil.convertToVo(OssFileMetadataVO.class,metadata);
            fillVoUserName(userMap, vo);
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
    public void softDeleteFile(String bucketName, String path) {
        // 1. 检测文件/文件夹是否存在
        OssFileMetadata metadata = metadataRepository.selectByPath(bucketName, path, FileTypeEnum.ALL);
        if(Objects.isNull(metadata)) {
            throw new ServiceException(ErrorEnum.FILE_METADATA_NOT_FOUND);
        }

        Date deleteAt = new Date();
        EditorUser currentUser = SecurityUtils.getCurrentUser();
        Long deleteId = metadata.getId();

        if(metadata.getIsDir()){
            softDeleteDirectory(metadata,deleteAt,currentUser,deleteId);
        }else{
            softDeleteSingleFile(metadata,deleteAt,currentUser,false);
        }
    }

    @Transactional
    protected void softDeleteSingleFile(OssFileMetadata metadata, Date deleteAt, EditorUser deleteBy, boolean fromDirectory) {
        try {
            // 标记软删除，更新元数据
            if(fromDirectory){
                metadata.setIsDeleted(2);
            }else{
                metadata.setIsDeleted(1);
            }
            metadata.setDeleteAt(deleteAt);
            metadata.setDeletedBy(deleteBy.getId());
            metadata.setDeleteId(metadata.getId());
            metadataRepository.updateById(metadata);
        }catch (Exception e) {
            throw new ServiceException(ErrorEnum.DELETE_FAIL,e);
        }
    }

    protected void softDeleteDirectory(OssFileMetadata metadata, Date deleteAt, EditorUser deleteBy, Long deleteId) {
        String bucketName = metadata.getS3Bucket();
        Deque<String> pathQueue = new ArrayDeque<>();
        pathQueue.add(metadata.getPath());

        boolean firstDir = true;

        try {
            while (!pathQueue.isEmpty()) {
                String path = pathQueue.pop();

                // 查找当前目录对应的目录信息
                OssFileMetadata dir = metadataRepository.selectByPath(bucketName, path, FileTypeEnum.FOLDER);
                List<OssFileMetadata> children = metadataRepository.listByPath(bucketName, path, Sort.unsorted());

                // 处理文件
                children.stream()
                        .filter(m -> !m.getIsDir() && m.getIsDeleted() == 0)
                        .forEach(meta -> {
                            softDeleteSingleFile(meta,deleteAt,deleteBy,true);
                        });

                // 处理子目录
                children.stream()
                        .filter(m -> !m.getIsDir() && m.getIsDeleted() == 0)
                        .forEach(m -> {
                            String next = path + "/" + m.getRealFileName();
                            pathQueue.push(next);
                        });

                // 子目录全部处理完成后删除自身目录
                if(firstDir){
                    dir.setIsDeleted(1);
                }else{
                    dir.setIsDeleted(2);
                }
                dir.setDeleteAt(deleteAt);
                dir.setDeletedBy(deleteBy.getId());
                dir.setDeleteId(deleteId);
                metadataRepository.updateById(dir);
            }
        }catch (Exception e) {
            throw new ServiceException(ErrorEnum.DELETE_FAIL,e);
        }
    }

    @Transactional
    @Override
    public OssFileMetadataVO recoveryRecycleFile(String bucketName, Long recycleId) {
        // 先检查对应id是否存在
        OssFileMetadata metadata = metadataRepository.selectById(recycleId);
        EditorUser currentUser = SecurityUtils.getCurrentUser();

        if(bucketName != null && !bucketName.equals(metadata.getS3Bucket())){
            throw new ServiceException("存储桶不匹配",ErrorEnum.FILE_METADATA_NOT_FOUND);
        }
        if(metadata == null || !(metadata.getIsDeleted() == 1) ){
            throw new ServiceException("文件不在回收站中", ErrorEnum.FILE_METADATA_NOT_FOUND);
        }

        // 执行恢复操作
        OssFileMetadata target = metadataRepository.selectById(recycleId);
        if(metadata.getIsDir()){
            target = recoveryDirectory(metadata, currentUser);
        }else {
            target = recoverySingleFile(metadata, currentUser, null);
        }

        OssFileMetadataVO vo = BeanConvertUtil.convertToVo(OssFileMetadataVO.class,target);
        Map<Long, String> userMap = getUserMap(Collections.singletonList(target));
        fillVoUserName(userMap, vo);
        return vo;
    }

    @Transactional
    protected OssFileMetadata recoverySingleFile(OssFileMetadata metadata, EditorUser currentUser, String targetPath1) {

        String bucket = metadata.getS3Bucket();
        String filename = metadata.getRealFileName();
        String targetPath = metadata.getParentPath();

        if(targetPath1 != null){
            targetPath = targetPath1;
        }

        // 如果targetPath被文件占据
        if(existsByPath(bucket,targetPath,FileTypeEnum.FILE)){
            targetPath = generateUniquePath(bucket, targetPath, true);
        }
        mkdirWithParent(bucket, targetPath, currentUser);

        // 分割文件名称和拓展名
        Pair<String, String> filePrefixAndExt = FileExtUtils.getFilePrefixAndExt(filename);
        String filePrefix = filePrefixAndExt.getFirst();
        String fileExt = filePrefixAndExt.getSecond();

        // 检查文件名是否重复
        if(metadataRepository.existsByPath(bucket,metadata.getPath(),FileTypeEnum.ALL)){
            filename = filePrefix + "_恢复" + "." + fileExt;
            String newfilePath = metadata.getPath() + "/" + filename;
            if(metadataRepository.existsByPath(bucket,newfilePath,FileTypeEnum.ALL)){
                filename = generateUniqueFileName(bucket,metadata.getPath(),filename,false);
            }
        }

        // 更改文件信息
        metadata.setRealFileName(filename);
        metadata.setIsDeleted(0);
        metadata.setDeleteAt(null);
        metadata.setDeletedBy(null);
        metadata.setDeleteId(null);

        // 更新文件信息
        metadataRepository.updateById(metadata);
        return metadata;
    }

    @Transactional
    protected OssFileMetadata recoveryDirectory(OssFileMetadata metadata, EditorUser currentUser) {
        OssFileMetadata ossFileMetadata = new OssFileMetadata();
        String bucketName = metadata.getS3Bucket();

        // 遍历目录，查找所有子目录的文件是否有重名
        Deque<String> pathQueue = new ArrayDeque<>();
        pathQueue.add(metadata.getPath());

        while (!pathQueue.isEmpty()) {
            String path = pathQueue.pop();

            OssFileMetadata currentDeletedDir = metadataRepository.selectDeletedByPathAndDeleteId(bucketName, path, FileTypeEnum.ALL, metadata.getDeleteId());

            if(currentDeletedDir == null){
                throw new ServiceException("文件不在回收站中", ErrorEnum.FILE_METADATA_NOT_FOUND);
            }

            List<OssFileMetadata> children = metadataRepository.listDeletedByPath(bucketName, path, Sort.unsorted());

            // 检查当前被删除的子目录是否存在新建的同名文件、文件夹
            OssFileMetadata ossFileMetadata1 = metadataRepository.selectByPath(bucketName, path, FileTypeEnum.ALL);
            OssFileMetadata thisDir = null;
            boolean toDelete = false;
            if(ossFileMetadata1 != null){
                if(ossFileMetadata1.getIsDir()){
                    // 直接删除原来的目录信息
                    toDelete = true;
                    thisDir = ossFileMetadata1;
                }else{
                    // 如果是文件，则子文件夹所有文件都要放在新的目录下
                    path = generateUniqueFileName(bucketName,metadata.getPath(),ossFileMetadata1.getRealFileName(),false);
                    // 新建path
                    mkdirWithParent(bucketName, path, currentUser);
                    thisDir = metadataRepository.selectByPath(bucketName, path, FileTypeEnum.FOLDER);
                }

            }else{
                thisDir = currentDeletedDir;
            }
            String finalPath = path;
            thisDir.setIsDeleted(0);
            thisDir.setDeleteAt(null);
            thisDir.setDeletedBy(null);
            thisDir.setDeleteId(null);
            metadataRepository.updateById(thisDir);

            // 处理文件
            children.stream()
                    .filter(m -> !m.getIsDir() && m.getIsDeleted() == 2)
                    .forEach(meta -> {
                        recoverySingleFile(meta, currentUser, finalPath);
                    });

            // 处理子目录
            children.stream()
                    .filter(m -> !m.getIsDir() && m.getIsDeleted() == 2)
                    .forEach(dir -> {
                        String next = finalPath + "/" + dir.getRealFileName();
                        pathQueue.push(next);
                    });

            if(toDelete){
                metadataRepository.realDeleteByPath(bucketName, path, metadata.getDeleteId());
            }

        }
        return ossFileMetadata;
    }


    @Override
    @Transactional
    public void deleteRecycleFile(String bucketName, Long deleteId) {
        // 先检查对应id是否存在
        OssFileMetadata metadata = metadataRepository.selectById(deleteId);

        if(bucketName != null && !bucketName.equals(metadata.getS3Bucket())){
            throw new ServiceException("存储桶不匹配",ErrorEnum.FILE_METADATA_NOT_FOUND);
        }
        if(metadata == null || !(metadata.getIsDeleted() == 1)) {
            throw new ServiceException("文件不在回收站中", ErrorEnum.FILE_METADATA_NOT_FOUND);
        }

        // 执行删除操作
        if (metadata.getIsDir()) {
            realDeleteDirectory(metadata);
        } else {
            realDeleteSingleFile(metadata);
        }

        return;
    }

    @Transactional
    protected void realDeleteSingleFile(OssFileMetadata metadata) {
        String bucketName = metadata.getS3Bucket();
        String s3Key = metadata.getS3Key();

        try {
            // 删除文件
            s3ObjectService.deleteObject(bucketName, s3Key);

            // 删除元数据
            metadataRepository.deleteById(metadata.getId());
        }catch (Exception e) {
            throw new ServiceException(ErrorEnum.DELETE_FAIL,e);
        }
    }

    @Transactional
    protected void realDeleteDirectory(OssFileMetadata metadata) {
        String bucketName = metadata.getS3Bucket();

        // 广度优先遍历原目录
        Deque<String> pathQueue = new ArrayDeque<>();
        pathQueue.push(metadata.getPath());

        while (!pathQueue.isEmpty()) {
            String path = pathQueue.pop();

            List<OssFileMetadata> children = metadataRepository.listDeletedByPath(bucketName, path, Sort.unsorted());

            // 处理文件
            children.stream()
                    .filter(m -> !m.getIsDir() && m.getIsDeleted() == 2 && m.getDeleteId() == metadata.getId())
                    .forEach(this::realDeleteSingleFile);

            // 处理子目录
            children.stream()
                    .filter(m -> !m.getIsDir() && m.getIsDeleted() == 2 && m.getDeleteId() == metadata.getId())
                    .forEach(dir -> {
                        String next = path + "/" + dir.getRealFileName();
                        pathQueue.push(next);
                    });

            // 子目录全部处理完成后删除自身目录
            metadataRepository.realDeleteByPath(bucketName, path, metadata.getDeleteId());

        }
    }

    @Transactional
    @Override
    public OssFileMetadataVO createFolder(String bucketName, String folderPath, String folderName){
        EditorUser currentUser = SecurityUtils.getCurrentUser();

        OssFileMetadata newFolder = mkdir(bucketName, folderPath, folderName, currentUser);

        // 填充用户信息
        Map<Long,String> usermap = new HashMap<>();
        usermap.put(currentUser.getId(),currentUser.getUsername());
        OssFileMetadataVO vo = BeanConvertUtil.convertToVo(OssFileMetadataVO.class, newFolder);
        fillVoUserName(usermap,vo);
        return vo;
    }

    @Override
    public OssFileMetadataVO createFolderRecursive(String bucketName, String pathWithFolderName) {
        EditorUser currentUser = SecurityUtils.getCurrentUser();
        long l = mkdirWithParent(bucketName, pathWithFolderName, currentUser);
        OssFileMetadata metadata = metadataRepository.selectById(l);
        OssFileMetadataVO vo = BeanConvertUtil.convertToVo(OssFileMetadataVO.class, metadata);
        Map<Long, String> userMap = getUserMap(Collections.singletonList(metadata));
        fillVoUserName(userMap, vo);
        return vo;
    }


    @Transactional
    protected OssFileMetadata mkdir(String bucketName, String folderPath, String folderName, EditorUser currentUser) {
        String newFolderPath = normalizePath(folderPath + "/" + folderName);
        Date currentTime = new Date();

        // 检测文件信息是否存在
        if(metadataRepository.existsByPath(bucketName,newFolderPath,FileTypeEnum.ALL)){
            throw new ServiceException("同名文件已存在", ErrorEnum.FILE_NAME_ALREADY_EXIST);
        }
        OssFileMetadata parentDir = null;

        // 如果路径是根目录，直接获取根目录的ID
        if(Objects.equals(folderPath, "/") && Objects.equals(folderName, "")){
            return createRootFolder(bucketName, currentUser);
        }else{
            // 如果路径不是根目录，检查父目录是否存在
            parentDir = metadataRepository.selectByPath(bucketName, folderPath, FileTypeEnum.FOLDER);
            if (parentDir == null && !folderPath.isEmpty()) {
                throw new ServiceException("父目录不存在: " + folderPath,ErrorEnum.FILE_METADATA_NOT_FOUND);
            }
        }


        String s3Key = getGeneratedS3KeyFromPath(bucketName, folderPath, folderName);

        OssFileMetadata newFolder = OssFileMetadata.builder()
                .path(newFolderPath)
                .s3Bucket(bucketName)
                .s3Key(s3Key)
                .createAt(currentTime)
                .lastModifiedAt(currentTime)
                .isDir(true)
                .createBy(currentUser.getId())
                .lastModifiedBy(currentUser.getId())
                .realFileName(folderName)
                .mimeType("application/x-directory")
                .parentPath(Objects.isNull(parentDir)?"":folderPath)
                .build();

        metadataRepository.insert(newFolder);
        return newFolder;
    }

    protected OssFileMetadata createRootFolder(String bucket, EditorUser currentUser) {
        Date currentTime  = new Date();
        OssFileMetadata rootFolder = OssFileMetadata.builder()
                .s3Bucket(bucket)
                .s3Key("/")
                .path("/")
                .isDir(true)
                .realFileName("")
                .createBy(currentUser.getId())
                .lastModifiedBy(currentUser.getId())
                .createAt(currentTime)
                .lastModifiedAt(currentTime)
                .mimeType("application/x-directory")
                .parentPath("")
                .build();
        metadataRepository.insert(rootFolder);
        return rootFolder;
    }

    @Transactional
    public long mkdirWithParent(String bucketName, String pathWithFolderName, EditorUser currentUser) {
        List<String> segments = getPathSegments(pathWithFolderName);
        ArrayDeque<String> parentToCreate = new ArrayDeque<>();
        OssFileMetadata lastPathMetadata = null;
        Long lastPathId = 0L;

        if(segments.isEmpty()){
            // 寻找根目录
            OssFileMetadata root = metadataRepository.selectByPath(bucketName,"/",FileTypeEnum.FOLDER);
            if(root == null){
                mkdir(bucketName,"/","", currentUser);
            }
        }

        // 从深到浅检查路径
        for(int i=0; i<=segments.size(); i++){
            String parentPath = getParentPath(segments,i);
            OssFileMetadata parent = metadataRepository.selectByPath(bucketName,parentPath,FileTypeEnum.ALL);
            if(parent == null){
                parentToCreate.push(parentPath);
            }else{
                if(!parent.getIsDir()){
                    throw new ServiceException("待创建文件夹路径已存在文件: " + parentPath,ErrorEnum.DEST_FILE_TYPE_CONFLICT);
                }
                lastPathMetadata = parent;
                lastPathId = parent.getId();
                break;
            }
        }

        // 创建缺失的父目录
        while(!parentToCreate.isEmpty()){
            String pathToCreate = parentToCreate.pop();
            List<String> pathSegments = getPathSegments(pathToCreate);
            lastPathMetadata = mkdir(bucketName, getParentPath(pathSegments, 1), getRealName(pathSegments),currentUser);
            lastPathId = lastPathMetadata.getId();
        }
        return lastPathId;
    }

    @Transactional
    @Override
    public OssFileMetadataVO moveFile(String bucketName, String srcPath, String destPath) {
        try {
            // 规范化路径
            srcPath = normalizePath(srcPath);
            destPath = normalizePath(destPath);
            EditorUser currentUser = SecurityUtils.getCurrentUser();

            // 1. 校验源数据
            OssFileMetadata srcMeta = metadataRepository.selectByPath(bucketName, srcPath, FileTypeEnum.ALL);
            if (srcMeta == null) {
                throw new ServiceException("源文件不存在", ErrorEnum.FILE_METADATA_NOT_FOUND);
            }

            // 2. 处理目标路径
            OssFileMetadata destMetaIfExists = metadataRepository.selectByPath(bucketName, destPath, FileTypeEnum.ALL);
            if (destMetaIfExists != null) {
                // 目标存在且是文件时抛出异常
                if (!destMetaIfExists.getIsDir()) {
                    throw new ServiceException(destPath + " 不是目录", ErrorEnum.DEST_FILE_TYPE_CONFLICT);
                }
            } else {
                // 创建目标目录（包括父目录）
                mkdirWithParent(bucketName, destPath, currentUser);
            }

            // 3. 执行移动操作
            OssFileMetadataVO destMeta = new OssFileMetadataVO();
            if (srcMeta.getIsDir()) {
                destMeta = moveDirectory(bucketName, srcMeta, destPath, currentUser);
            } else {
                destMeta = moveSingleFile(bucketName, srcMeta, destPath, currentUser);
            }

            return destMeta;
        } catch (ObjectStorageException e) {
            throw new ServiceException(ErrorEnum.FILE_MOVE_FAIL);
        }
    }

    @Transactional
    protected OssFileMetadataVO moveDirectory(String bucketName, OssFileMetadata srcDirMeta,
                               String destPath, EditorUser editorUser) {
        // 生成新目录路径
        String basePath = normalizePath(destPath + "/" + srcDirMeta.getRealFileName());
        String newDirPath = generateUniquePath(bucketName, basePath,true);
        String newDirName = newDirPath.substring(newDirPath.lastIndexOf("/")+1);
        String newDirS3Key = getGeneratedS3KeyFromPath(bucketName, destPath, newDirName);

        // 如果不存在目标目录，则创建目标目录
        if (!metadataRepository.existsByPath(bucketName, newDirPath, FileTypeEnum.FOLDER)) {
            mkdir(bucketName, destPath, newDirName, editorUser);
        }
        Date currentTime = new Date();
        OssFileMetadata newDirMeta = OssFileMetadata.builder()
                .s3Bucket(bucketName)
                .s3Key(newDirS3Key)
                .path(newDirPath)
                .realFileName(newDirName)
                .isDir(true)
                .mimeType("application/x-directory")
                .parentPath(normalizePath(destPath))
                .createAt(currentTime) // 使用当前时间作为创建时间.createBy(userId)
                .createBy(editorUser.getId())
                .lastModifiedAt(currentTime).lastModifiedBy(editorUser.getId())
                .build();
        metadataRepository.insert(newDirMeta);

        // 广度优先遍历原目录
        Deque<Pair<String, String>> pathQueue = new ArrayDeque<>();
        pathQueue.push(Pair.of(srcDirMeta.getPath(), newDirPath));

        while (!pathQueue.isEmpty()) {
            Pair<String, String> paths = pathQueue.pop();
            String currentSrcPath = paths.getFirst();
            String currentDestPath = paths.getSecond();

            List<OssFileMetadata> children = metadataRepository.listByPath(bucketName, currentSrcPath, Sort.unsorted());

            // 处理文件
            children.stream()
                    .filter(m -> !m.getIsDir())
                    .forEach(file -> moveSingleFile(bucketName, file, currentDestPath, editorUser));

            // 处理子目录
            children.stream()
                    .filter(OssFileMetadata::getIsDir)
                    .forEach(dir -> {
                        String childDestPath = currentDestPath + "/" + dir.getRealFileName();
                        mkdir(bucketName, currentDestPath, dir.getRealFileName(),editorUser);
                        pathQueue.push(Pair.of(dir.getPath(), childDestPath));
                    });

            // 子目录全部处理完成后删除自身目录
            metadataRepository.deleteById(srcDirMeta.getId());
        }

        OssFileMetadataVO ossFileMetadataVO = BeanConvertUtil.convertToVo(OssFileMetadataVO.class, newDirMeta);
        Map<Long, String> userMap = getUserMap(Collections.singletonList(newDirMeta));
        fillVoUserName(userMap, ossFileMetadataVO);
        return ossFileMetadataVO;
    }

    @Transactional
    protected OssFileMetadataVO moveSingleFile(String bucketName, OssFileMetadata srcFileMeta,
                                String destDirPath, EditorUser createdBy) {
        String destFileName = srcFileMeta.getRealFileName();
        String destFilePath = normalizePath(destDirPath + "/" + destFileName);
        destFilePath = resolveDuplicatePath(bucketName, destFilePath);
        String destFileRealname = destFilePath.substring(destFilePath.lastIndexOf("/") + 1);

        String destS3Key = getGeneratedS3KeyFromPath(bucketName,destDirPath, destFileRealname);

        s3ObjectService.copyObject(bucketName, srcFileMeta.getS3Key(),
                bucketName, destFilePath);
        s3ObjectService.deleteObject(bucketName, srcFileMeta.getS3Key());

        OssFileMetadata destMeta = buildDestMetadata(bucketName, destFilePath, destS3Key, srcFileMeta, createdBy.getId());
        metadataRepository.insert(destMeta);

        metadataRepository.deleteById(srcFileMeta.getId());

        OssFileMetadataVO ossFileMetadataVO = BeanConvertUtil.convertToVo(OssFileMetadataVO.class, destMeta);
        Map<Long, String> userMap = getUserMap(Collections.singletonList(destMeta));
        fillVoUserName(userMap, ossFileMetadataVO);
        return ossFileMetadataVO;
    }

    @Transactional
    protected OssFileMetadataVO copySingleFile(String bucketName, OssFileMetadata srcFileMeta, String destBucketName,
                                             String destDirPath, EditorUser createdBy){
        String destFileName = srcFileMeta.getRealFileName();
        String destFilePath = normalizePath(destDirPath + "/" + destFileName);
        destFilePath = resolveDuplicatePath(bucketName, destFilePath);
        String destFileRealname = destFilePath.substring(destFilePath.lastIndexOf("/") + 1);

        String destS3Key = getGeneratedS3KeyFromPath(bucketName,destDirPath, destFileRealname);

        s3ObjectService.copyObject(bucketName, srcFileMeta.getS3Key(),
                destBucketName, destFilePath);

        OssFileMetadata destMeta = buildDestMetadata(destBucketName, destFilePath, destS3Key, srcFileMeta, createdBy.getId() );
        metadataRepository.insert(destMeta);

        OssFileMetadataVO ossFileMetadataVO = BeanConvertUtil.convertToVo(OssFileMetadataVO.class, destMeta);
        Map<Long, String> userMap = getUserMap(Collections.singletonList(destMeta));
        fillVoUserName(userMap, ossFileMetadataVO);
        return ossFileMetadataVO;
    }

    private OssFileMetadata buildDestMetadata(String bucketName, String destPath, String destS3Key,
                                              OssFileMetadata srcMeta, Long userId) {
        Date date = new Date();
        return OssFileMetadata.builder()
                .s3Bucket(bucketName)
                .s3Key(destS3Key)
                .path(destPath)
                .realFileName(getRealName(getPathSegments(destPath)))
                .fileSize(srcMeta.getFileSize())
                .mimeType(srcMeta.getMimeType())
                .isDir(srcMeta.getIsDir())
                .parentPath(getParentPath(getPathSegments(destPath),1))
                .createBy(userId)
                .lastModifiedBy(userId)
                .createAt(date)
                .lastModifiedAt(date)
                .build();
    }

    private String resolveDuplicatePath(String bucketName, String originalPath) {
        if (metadataRepository.selectByPath(bucketName, originalPath, FileTypeEnum.ALL) == null) {
            return originalPath;
        }
        return generateUniquePath(bucketName, originalPath, false);
    }

    @Override
    @Transactional
    public OssFileMetadataVO copyFile(String sourceBucket, String sourcePath,
                                      String destBucket, String destPath,
                                      boolean overwrite) {
        return new OssFileMetadataVO();
    }

    @Override
    @Transactional
    public void initBucket(String bucketName, EditorUser currentUser) {
        if(!s3BucketService.bucketExists(bucketName)) {
            try {
                s3BucketService.createBucket(bucketName);
                // 为数据库表添加根目录
                mkdir(bucketName,"/","", currentUser);
                if(!bucketName.equals("init-bucket")){

                }
            } catch (Exception e){
                if(s3BucketService.bucketExists(bucketName)) {
                    s3BucketService.deleteBucket(bucketName);
                }
                throw new ServiceException(e);
            }

        }else{
            throw new ServiceException(ErrorEnum.USER_BUCKET_ALREADY_INIT);

        }
    }

    @Override
    @Transactional
    public OssFileMetadataVO rename(String bucketName, String oldPath, String newName) {

        // 禁止重命名根目录
        if(oldPath.equals("/")){
            throw new ServiceException(ErrorEnum.RENAME_ROOT_NOT_ALLOWED);
        }

        // 规范化路径
        List<String> pathSegments = getPathSegments(oldPath);
        String parentPath = getParentPath(pathSegments,1);
        String newPath = normalizePath(parentPath + newName);

        // 检查路径是否相同
        if(oldPath.equals(newPath)){
            return new OssFileMetadataVO();
        }

        // 获取路径下的文件信息
        OssFileMetadata sourceMeta = metadataRepository.selectByPath(bucketName, oldPath, FileTypeEnum.ALL);

        // 校验源对象存在性
        if (sourceMeta == null) {
            throw new ServiceException(ErrorEnum.FILE_METADATA_NOT_FOUND);
        }

        // 检测目标路径是否存在
        boolean has = metadataRepository.existsByPath(bucketName, newPath, FileTypeEnum.ALL);
        if(has){
            throw new ServiceException(ErrorEnum.FILE_NAME_ALREADY_EXIST);
        }

        // 处理重命名逻辑
        try {
            OssFileMetadata meta = new OssFileMetadata();
            if(!sourceMeta.getIsDir()){
                meta = renameSingleFile(bucketName, oldPath, newName, sourceMeta, parentPath, newPath);
            }else {
                meta = renameFolder(bucketName, newName, sourceMeta);
            }
            OssFileMetadataVO ossFileMetadataVO = BeanConvertUtil.convertToVo(OssFileMetadataVO.class, meta);
            Map<Long, String> userMap = getUserMap(Collections.singletonList(meta));
            fillVoUserName(userMap,ossFileMetadataVO);
            return ossFileMetadataVO;
        }
        catch(ObjectStorageException e){
            throw new ServiceException(e);
        }
    }

    @Override
    public List<OssRecycleMetadataVO> listRecycleFiles(String bucketName, Sort sort) {
        List<OssRecycleMetadataVO> list = new ArrayList<>();

        // 检测桶是否存在
        if (!s3BucketService.bucketExists(bucketName)) {
            throw new ServiceException(ErrorEnum.USER_BUCKET_NOT_INIT);
        }

        List<OssFileMetadata> metadataList = metadataRepository.listSoftDeleteItems(bucketName, sort);

        Map<Long, String> userMap = getUserMap(metadataList);

        for (OssFileMetadata metadata : metadataList) {
            OssRecycleMetadataVO vo = BeanConvertUtil.convertToVo(OssRecycleMetadataVO.class,metadata);
            fillRecycleVoUserName(userMap, vo);
            list.add(vo);
        }

        return list;
    }

    @Override
    @Transactional
    public void deleteBucket(String bucketName) {
        metadataRepository.deleteBucket(bucketName);
        s3BucketService.deleteBucket(bucketName);
    }

    @Override
    @Transactional
    public void clearBucket(String bucketName) {
        metadataRepository.clearBucket(bucketName);
        s3BucketService.clearBucket(bucketName);
    }

    @Override
    public String generateLink(String bucketName, String filePath) {
        OssFileMetadata ossFileMetadata = metadataRepository.selectByPath(bucketName, filePath, FileTypeEnum.FILE);
        if(ossFileMetadata == null){
            throw new ServiceException(ErrorEnum.FILE_METADATA_NOT_FOUND);
        }
        Duration duration = Duration.ofHours(1);
        String s = s3ObjectService.generatePresignedUrl(bucketName, ossFileMetadata.getS3Key(), duration);
        return s;
    }

    private OssFileMetadata renameSingleFile(String bucket, String oldPath, String newName, OssFileMetadata sourceMeta, String parentPath, String newPath) {
        String oldS3Key = sourceMeta.getS3Key();
        String newS3Key = getGeneratedS3KeyFromPath(bucket, parentPath, newName);

        // 实现移动对象逻辑
        s3ObjectService.copyObject(bucket, oldS3Key, bucket, newS3Key);
        s3ObjectService.deleteObject(bucket, oldS3Key);

        // 更新数据库内容
        sourceMeta.setS3Key(newS3Key);
        sourceMeta.setPath(newPath);
        sourceMeta.setRealFileName(newName);
        metadataRepository.updateById(sourceMeta);

        return sourceMeta;
    }

    private OssFileMetadata renameFolder(String bucket, String newName, OssFileMetadata sourceMeta) {
        Deque<Pair<OssFileMetadata, String>> pathQueue = new ArrayDeque<>();

        // 计算初始目标路径（父路径 + 新名称）
        String parentPath = sourceMeta.getParentPath();
        String initialDestPath = normalizePath(parentPath + "/" + newName);
        sourceMeta.setRealFileName(newName);
        pathQueue.push(Pair.of(sourceMeta, initialDestPath));

        while (!pathQueue.isEmpty()) {
            Pair<OssFileMetadata, String> paths = pathQueue.pop();
            OssFileMetadata currentMeta = paths.getFirst();
            String currentDestPath = paths.getSecond();

            // 获取当前目录原始路径的子项
            List<OssFileMetadata> children = metadataRepository.listByPath(bucket, currentMeta.getPath(), Sort.unsorted());

            // 修复2：处理当前目录自身（直接使用目标路径，不需要拼接文件名）
            String newParent = getParentPath(currentDestPath);

            currentMeta.setParentPath(newParent);
            currentMeta.setPath(currentDestPath);
            currentMeta.setS3Key(getGeneratedS3KeyFromPath(bucket, newParent, currentMeta.getRealFileName()));
            metadataRepository.updateById(currentMeta);

            // 处理文件（修复3：使用正确的child变量引用）
            children.stream()
                    .filter(m -> !m.getIsDir())
                    .forEach(child -> {
                        String newFilePath = normalizePath(currentDestPath + "/" + child.getRealFileName());
                        String newKey = getGeneratedS3KeyFromPath(bucket, currentDestPath, child.getRealFileName());

                        s3ObjectService.copyObject(bucket, child.getS3Key(), bucket, newKey);
                        s3ObjectService.deleteObject(bucket, child.getS3Key());

                        child.setS3Key(newKey);
                        child.setPath(newFilePath);
                        child.setParentPath(currentDestPath);
                        metadataRepository.updateById(child);
                    });

            // 处理子目录（修复4：保持正确的目录层级）
            children.stream()
                    .filter(OssFileMetadata::getIsDir)
                    .forEach(child -> {
                        String newDirPath = normalizePath(currentDestPath + "/" + child.getRealFileName());
                        pathQueue.push(Pair.of(child, newDirPath));
                    });
        }
        return sourceMeta;

    }

    private String generateUniquePath(String bucket, String filePath, boolean isDir) {
        String currentPath = filePath;
        int attempt = 1;

        while (metadataRepository.existsByPath(bucket, currentPath, FileTypeEnum.ALL)) {
            String suffix = String.format("(%d)", attempt++);
            if(!isDir){
                int dotIndex = filePath.lastIndexOf('.');
                if(dotIndex != -1){
                    String base = filePath.substring(0, dotIndex);
                    String extension = filePath.substring(dotIndex);
                    currentPath = normalizePath(base + suffix + extension);
                }
            }else{
                currentPath = normalizePath(filePath) + suffix;
            }
        }
        return currentPath;
    }

    private String generateUniqueFileName(String bucket, String path, String fileName, Boolean isDir) {
        Pair<String, String> filePrefixAndExt = FileExtUtils.getFilePrefixAndExt(fileName);
        String filePrefix = filePrefixAndExt.getFirst();
        String fileExt =  "." + filePrefixAndExt.getSecond();

        int attempt = 1;
        while (metadataRepository.existsByPath(bucket, path + "/" + fileName, FileTypeEnum.ALL)) {
            String suffix = String.format("(%d)", attempt++);
            fileName = filePrefix + suffix + fileExt;
            if(!isDir){
                int dotIndex = fileName.lastIndexOf('.');
                if(dotIndex != -1){
                    String baseName = fileName.substring(0, dotIndex);
                    String extension = fileName.substring(dotIndex);
                    fileName = baseName + suffix + extension;
                }
            }
        }
        return fileName;
    }

    private void fillVoUserName(@NotNull Map<Long,String> userMap, @NotNull OssFileMetadataVO vo) {
        vo.setCreateByUsername(userMap.getOrDefault(vo.getCreateBy(),""));
        vo.setLastModifiedByUsername(userMap.getOrDefault(vo.getLastModifiedBy(),""));
    }

    private void fillRecycleVoUserName(@NotNull Map<Long,String> userMap, @NotNull OssRecycleMetadataVO vo) {
        vo.setCreateByUsername(userMap.getOrDefault(vo.getCreateBy(),""));
        vo.setLastModifiedByUsername(userMap.getOrDefault(vo.getLastModifiedBy(),""));
        vo.setDeletedByUsername(userMap.getOrDefault(vo.getDeletedBy(),""));
    }
}


