package md.cernev.quickflick.aws;

import md.cernev.quickflick.configuration.AwsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Component
public class AWSS3 {

  final S3Client s3Client;

  @Autowired
  public AWSS3(S3Client s3Client) {this.s3Client = s3Client;}

  public String uploadToS3(String filename) {
    File file = new File(filename);
    System.out.println("Uploading file to server...");
    PutObjectRequest request = PutObjectRequest.builder().bucket(AwsConfiguration.BUCKET_NAME).key(file.getName())
        .build();
    PutObjectResponse response = s3Client.putObject(request, file.toPath());
    String s3Location = "s3://" + AwsConfiguration.BUCKET_NAME + "/" + filename;

    System.out.println("File uploaded successfully. ETag: " + response.eTag());
    return s3Location;
  }

  public String downloadFromS3(String fileFromS3, String filenameToSave) {
    GetObjectRequest request = GetObjectRequest.builder().bucket(AwsConfiguration.BUCKET_NAME).key(fileFromS3).build();
    ResponseBytes<GetObjectResponse> responseResponseBytes = s3Client.getObjectAsBytes(request);

    byte[] data = responseResponseBytes.asByteArray();

    File myFile = new File(filenameToSave);
    OutputStream os = null;
    try {
      os = new FileOutputStream(myFile);
      os.write(data);
      System.out.println("Successfully obtained bytes from an S3 object");
      os.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return filenameToSave;
  }
}
