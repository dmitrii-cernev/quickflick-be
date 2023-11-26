package md.cernev.quickflick.configuration;

import md.cernev.quickflick.storage.AWSStorageService;
import md.cernev.quickflick.storage.StorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class StorageConfiguration {

  @Bean
  StorageService storageService(S3Client s3Client) {
//    return new LocalStorageService();
    return new AWSStorageService(s3Client);
  }
}
