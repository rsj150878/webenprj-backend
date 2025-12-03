package at.fhtw.webenprjbackend.filestorage.minio;

import java.io.InputStream;

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
@Profile("!docker-free") // Exclude from docker-free profile - use MockFileStorage instead
public class MinioStorage implements FileStorage {

    private final MinioClient minioClient;

    @Override
    public String upload(MultipartFile file) {
        try {
            String objectName = java.util.UUID.randomUUID().toString();
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket("uploads")
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
                    .bucket("uploads")
                    .object(id)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to load file from MinIO", e);
        }
    }
}

