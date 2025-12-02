package at.fhtw.webenprjbackend.filestorage.minio;

import at.fhtw.webenprjbackend.filestorage.FileException;
import at.fhtw.webenprjbackend.filestorage.FileStorage;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import okhttp3.internal.concurrent.TaskLoggerKt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioStorage implements FileStorage {

    private final MinioProperties minioProperties;

    private final MinioClient minioClient;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public String upload(MultipartFile file) {
        String uuid = UUID.randomUUID().toString();

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("uploads")
                            .object(uuid)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {

            logger.error("Error while uploading file", e);
            throw new FileException("Upload file failed for file with id=" + uuid, e);
        }

        return uuid;
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
            throw new FileException("Load file failed for file with external id=" + id, e);
        }
    }
}

