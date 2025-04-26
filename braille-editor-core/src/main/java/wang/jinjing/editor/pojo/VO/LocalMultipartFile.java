package wang.jinjing.editor.pojo.VO;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import wang.jinjing.common.pojo.VO.BaseVO;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalMultipartFile extends BaseVO implements MultipartFile  {
    private final byte[] content;
    private final String name;
    private final String originalFilename;
    private final String contentType;

    public LocalMultipartFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        this.content = Files.readAllBytes(path);
        this.originalFilename = path.getFileName().toString();
        this.name = "file";
        this.contentType = Files.probeContentType(path); // 自动探测 Content-Type
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public Resource getResource() {
        return MultipartFile.super.getResource();
    }


    @Override
    public void transferTo(Path dest) throws IOException, IllegalStateException {
        MultipartFile.super.transferTo(dest);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        Files.write(dest.toPath(), content);
    }
}