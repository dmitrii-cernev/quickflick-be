package md.cernev.quickflick.storage;

import md.cernev.quickflick.configuration.AwsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class AWSStorageService implements StorageService {
  private static final Logger logger = LoggerFactory.getLogger(AWSStorageService.class);
  private final S3Client s3Client;

  public AWSStorageService(S3Client s3Client) {this.s3Client = s3Client;}

  @Override
  public String save(byte[] fileData, String filename) {
    logger.info("Uploading file to server...");
    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(AwsConfiguration.BUCKET_NAME)
        .key(filename)
        .build();
    PutObjectResponse response = s3Client.putObject(request, RequestBody.fromBytes(fileData));
    String s3Location = "s3://" + AwsConfiguration.BUCKET_NAME + "/" + filename;

    logger.info("File uploaded successfully. ETag: {}", response.eTag());
    return s3Location;
  }
}
