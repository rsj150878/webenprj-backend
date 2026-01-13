package at.fhtw.webenprjbackend.filestorage.minio;

import java.io.InputStream;

import io.minio.RemoveObjectArgs;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import at.fhtw.webenprjbackend.filestorage.FileStorage;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Profile("!docker-free & !test") // Exclude from docker-free and test profiles - use MockFileStorage instead
public class MinioStorage implements FileStorage {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public String upload(MultipartFile file) {
        try {
            String objectName = java.util.UUID.randomUUID().toString();

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    @Override
    public InputStream load(String id) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(id)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to load file from MinIO", e);
        }
    }

    @Override
    public void delete(String id) {
        try {
           minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(id)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from MinIO", e);
        }

    }
}

