package md.cernev.quickflick.configuration;

import md.cernev.quickflick.storage.LocalStorageService;
import md.cernev.quickflick.storage.StorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfiguration {

  @Bean
  StorageService storageService() {
    return new LocalStorageService();
  }
}
