package md.cernev.quickflick.reactive.storage;

import md.cernev.quickflick.configuration.AwsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.ByteBuffer;

@Component
public class AWSReactiveStorageService {
  private static final Logger logger = LoggerFactory.getLogger(AWSReactiveStorageService.class);
  private final S3AsyncClient s3AsyncClient;

  public AWSReactiveStorageService(S3AsyncClient s3AsyncClient) {this.s3AsyncClient = s3AsyncClient;}

  /**
   * Note: theoretically may produce memory issues.
   * @param fileData
   * @param filePath "<directory to save>/<filename>"*
   * @return S3 location of saved file, for example: "S3://DIR_TO_SAVE/saved_file.txt
   */
  public Mono<String> save(Flux<ByteBuffer> fileData, String filePath, long contentLength) {
    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(AwsConfiguration.BUCKET_NAME)
        .contentLength(contentLength)
        .key(filePath)
        .build();
    return Mono.fromFuture(() -> s3AsyncClient.putObject(request, AsyncRequestBody.fromPublisher(fileData)))
        .map(response -> {
          String s3Location = "s3://" + AwsConfiguration.BUCKET_NAME + "/" + filePath;
          logger.info("File uploaded successfully. Location: {}", s3Location);
          return s3Location;
        });
  }
}
