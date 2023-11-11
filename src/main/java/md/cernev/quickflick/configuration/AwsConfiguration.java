package md.cernev.quickflick.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.transcribe.TranscribeClient;

@Configuration
public class AwsConfiguration {
  public static final Region REGION = Region.US_EAST_1;
  public static final String BUCKET_NAME = "quickflick-buckeet";

  @Bean
  public TranscribeClient getTranscribeClient() {
    return TranscribeClient.builder()
        .credentialsProvider(getCredentials())
        .region(REGION)
        .build();
  }

  @Bean
  public S3Client getS3Client() {
    return S3Client.builder()
        .credentialsProvider(getCredentials())
        .region(REGION)
        .build();
  }

  @Bean
  public AwsCredentialsProvider getCredentials() {
    return DefaultCredentialsProvider.create();
  }
}
