package wang.jinjing.editor.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class OssMinioConfig {

    @Value("${oss.minio.endpoint}")
    private String endpoint;

    @Value("${oss.minio.accessKey}")
    private String accessKey;

    @Value("${oss.minio.secretKey}")
    private String secretKey;

    @Bean
    MinioClient getMinioClient(){
        return MinioClient
                .builder().endpoint(endpoint)
                .credentials(accessKey, secretKey).build();
    }
}
