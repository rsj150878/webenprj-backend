package at.fhtw.webenprjbackend.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;

import at.fhtw.webenprjbackend.filestorage.FileStorage;

/**
 * Mock file storage configuration for docker-free development and testing
 */
@Configuration
@Profile({"docker-free", "test"})
public class MockFileStorageConfig {

    /**
     * Mock FileStorage implementation that stores files in memory
     */
    @Bean
    @Primary
    public FileStorage mockFileStorage() {
        return new MockFileStorage();
    }

    /**
     * In-memory mock file storage implementation
     */
    public static class MockFileStorage implements FileStorage {
        
        private final Map<String, MockFile> fileStore = new ConcurrentHashMap<>();
        private final Logger log = LoggerFactory.getLogger(MockFileStorage.class);

        @Override
        public String upload(MultipartFile file) {
            String fileId = UUID.randomUUID().toString();
            
            try {
                MockFile mockFile = new MockFile(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
                );
                
                fileStore.put(fileId, mockFile);
                
                log.info("✅ Mock file upload: {} -> {}", 
                    file.getOriginalFilename(), fileId);
                
                return fileId;
                
            } catch (java.io.IOException e) {
                log.error("❌ Mock file upload failed: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("Mock file upload failed", e);
            }
        }

        @Override
        public InputStream load(String id) {
            MockFile mockFile = fileStore.get(id);
            
            if (mockFile == null) {
                log.warn("⚠️ Mock file not found: {}", id);
                return new ByteArrayInputStream(new byte[0]);
            }
            
            log.info("✅ Mock file loaded: {}", mockFile.originalName);
            return new ByteArrayInputStream(mockFile.content);
        }

        @Override
        public void delete(String id) {

        }
    }

    /**
     * Container for mock file data
     */
    private static class MockFile {
        final String originalName;
        final byte[] content;

        MockFile(String originalName, String contentType, byte[] content) {
            this.originalName = originalName;
            this.content = content;
        }
    }
}
