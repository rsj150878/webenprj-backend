package at.fhtw.webenprjbackend.filestorage.minio;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties("minio")
@Getter
@Setter
public class MinioProperties {

    private String url;
    private int port;
    private String user;
    private String password;
    private String bucketName = "uploads";  // Default value

}
