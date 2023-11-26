package md.cernev.quickflick.transcriber;

import md.cernev.quickflick.aws.AWSTranscribe;
import md.cernev.quickflick.configuration.AwsConfiguration;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service
public class AWSTranscriber implements Transcriber {
  private final AWSTranscribe awsTranscribe;
  private final S3Client s3Client;

  public AWSTranscriber(AWSTranscribe awsTranscribe, S3Client s3Client) {
    this.awsTranscribe = awsTranscribe;
    this.s3Client = s3Client;
  }

  @Override
  public String transcribe(String filePath) {
    String transcriptionJobName = awsTranscribe.startTranscriptionJob(filePath);
    String transcriptionJobFileS3Path = awsTranscribe.getTranscriptionJobFileKey(transcriptionJobName);
    byte[] bytesTranscription = readBytesFromS3(transcriptionJobFileS3Path);
    return getTranscription(bytesTranscription);
  }

  //todo: also can be moved
  private String getTranscription(byte[] bytesTranscription) {
    JSONTokener jsonTokener = new JSONTokener(new String(bytesTranscription));
    JSONObject jsonObject = new JSONObject(jsonTokener);
    return jsonObject.getJSONObject("results")
        .getJSONArray("transcripts")
        .getJSONObject(0)
        .getString("transcript");
  }

  //todo: move somewhere
  private byte[] readBytesFromS3(String transcriptionJobFileS3Path) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(AwsConfiguration.BUCKET_NAME)
        .key(transcriptionJobFileS3Path)
        .build();

    ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(getObjectRequest);
    return responseBytes.asByteArray();
  }
}
