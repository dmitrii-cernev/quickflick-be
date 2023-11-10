package md.cernev.quickflick.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.transcribe.TranscribeClient;

@Configuration
public class AwsConfiguration {
  private static final Region REGION = Region.US_EAST_1;

  @Bean
  public TranscribeClient getTranscribeClient() {
    return TranscribeClient.builder()
        .credentialsProvider(getCredentials())
        .region(REGION)
        .build();
  }

  @Bean
  public AwsCredentialsProvider getCredentials() {
    return DefaultCredentialsProvider.create();
  }
}
