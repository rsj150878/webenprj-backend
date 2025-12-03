package at.fhtw.webenprjbackend.filestorage.minio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;

@Component
@Profile("!docker-free") // Exclude from docker-free profile
public class MinioInitializer {

    Logger log = LoggerFactory.getLogger(MinioInitializer.class);
    private final MinioClient minioClient;

    public MinioInitializer(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void initBuckets() throws Exception {
        createBucketIfNotExists("uploads");
    }

    private void createBucketIfNotExists(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        );
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
            );
            log.info("Bucket {} created", bucketName);
        } else
          log.info("Bucket {} already exists", bucketName);
    }
}


