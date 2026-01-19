package at.fhtw.webenprjbackend.filestorage.minio;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.minio.MinioClient;

@Configuration
@Profile("!docker-free & !test") // Exclude from docker-free and test profiles
@RequiredArgsConstructor
public class MinioClientBackendConfig {

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient getMinioClient() {
        String endpoint = minioProperties.getUrl();

        if (!endpoint.matches(".*:\\d+$")) {
            endpoint = endpoint + ":" + minioProperties.getPort();
        }

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(minioProperties.getUser(), minioProperties.getPassword())
                .build();
    }

}
