package md.cernev.quickflick.transcriber;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

@Service
public class TikTokTranscriber implements Transcriber {
  public static final String MP4 = "mp4";
  private static final Region region = Region.US_EAST_1;
  private final TranscribeClient transcribeClient;

  public TikTokTranscriber() {
    transcribeClient = TranscribeClient.builder()
        .credentialsProvider(getCredentials())
        .region(region)
        .build();
  }

  @Override
  public String transcribe(Object o) {
    String transcriptionJobName = startTranscriptionJob();
    return getTranscriptionJobFileUri(transcriptionJobName);
  }

  private AwsCredentialsProvider getCredentials() {
    return DefaultCredentialsProvider.create();
  }

  private String startTranscriptionJob() {
    String transcriptionJobName = "my-first-transcription-job";
    String mediaType = MP4;
    Media myMedia = Media.builder()
        .mediaFileUri("s3://quickflick-buckeet/Download.mp4")
        .build();
    String outputBucketName = "quickflick-buckeet";
    StartTranscriptionJobRequest startTranscriptionJobRequest = StartTranscriptionJobRequest.builder()
        .identifyLanguage(true)
        .transcriptionJobName(transcriptionJobName)
        .media(myMedia)
        .mediaFormat(mediaType)
        .outputBucketName(outputBucketName)
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
    System.out.println(getTranscriptionJobResponse.transcriptionJob());

    return getTranscriptionJobResponse.transcriptionJob().transcript().transcriptFileUri();
  }
}
