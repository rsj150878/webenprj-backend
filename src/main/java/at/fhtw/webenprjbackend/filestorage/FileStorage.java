package at.fhtw.webenprjbackend.filestorage;


import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Component
public interface FileStorage {
    String upload(MultipartFile file);

    InputStream load(String id);

    void delete(String id);
}
