package md.cernev.quickflick.transcriber;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

import java.io.File;

@Service
public class TikTokTranscriber implements Transcriber {
  public static final String BUCKET_NAME = "quickflick-buckeet";
  private final TranscribeClient transcribeClient;
  private final S3Client s3Client;

  @Autowired
  public TikTokTranscriber(TranscribeClient transcribeClient, S3Client s3Client) {
    this.transcribeClient = transcribeClient;
    this.s3Client = s3Client;
  }

  @Override
  public String transcribe(String filename) {
    String s3Path = uploadToS3(filename);
    String transcriptionJobName = startTranscriptionJob(s3Path, filename);
    return getTranscriptionJobFileUri(transcriptionJobName);
  }

  private String uploadToS3(String filename) {
    File file = new File(filename);
    PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(BUCKET_NAME).key(file.getName())
        .build();
    PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, file.toPath());
    String s3Location = "s3://" + BUCKET_NAME + "/" + filename;

    System.out.println("File uploaded successfully. ETag: " + putObjectResponse.eTag());
    System.out.println("S3 Location: " + s3Location);
    return s3Location;
  }

  private String startTranscriptionJob(String mediaFileUri, String filename) {
    String transcriptionJobName = filename.replace(".mp4", "job");
    StartTranscriptionJobRequest startTranscriptionJobRequest = StartTranscriptionJobRequest.builder()
        .identifyLanguage(true).transcriptionJobName(transcriptionJobName)
        .media(builder -> builder.mediaFileUri(mediaFileUri).build()).mediaFormat(MediaFormat.MP4)
        .outputBucketName(BUCKET_NAME)
        .build();

    StartTranscriptionJobResponse startTranscriptionJobResponse = transcribeClient.startTranscriptionJob(startTranscriptionJobRequest);
    System.out.println(startTranscriptionJobResponse.toString());
    return transcriptionJobName;
  }

  private String getTranscriptionJobFileUri(String transcriptionJobName) {
    GetTranscriptionJobRequest getTranscriptionJobRequest = GetTranscriptionJobRequest.builder()
        .transcriptionJobName(transcriptionJobName)
        .build();

    GetTranscriptionJobResponse getTranscriptionJobResponse = transcribeClient.getTranscriptionJob(getTranscriptionJobRequest);
    String transcriptFileUri = getTranscriptionJobResponse.transcriptionJob().transcript().transcriptFileUri();
    System.out.println(transcriptFileUri);

    return transcriptFileUri;
  }
}
