package at.fhtw.webenprjbackend.filestorage.minio;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("minio")

public class MinioProperties {

    private String url;
    private int port;
    private String user;
    private String password;


}
