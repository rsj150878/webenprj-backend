package at.fhtw.webenprjbackend.filestorage.minio;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioClientBackendConfig {

    @Bean
    public MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint("http://127.0.0.1:9000")
                .credentials("minioadmin", "minioadmin")
                .build();
    }

}
