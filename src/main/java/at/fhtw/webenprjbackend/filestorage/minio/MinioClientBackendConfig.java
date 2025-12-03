package at.fhtw.webenprjbackend.filestorage.minio;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.minio.MinioClient;

@Configuration
@Profile("!docker-free") // Exclude from docker-free profile
public class MinioClientBackendConfig {

    @Bean
    public MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint("http://127.0.0.1:9000")
                .credentials("minioadmin", "minioadmin")
                .build();
    }

}
