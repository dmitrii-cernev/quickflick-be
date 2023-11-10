package md.cernev.quickflick.transcriber;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

@Service
public class TikTokTranscriber implements Transcriber {
  public static final String MP4 = "mp4";
  public static final String TRANSCRIPTION_JOB_NAME = "my-first-transcription-job";
  private final TranscribeClient transcribeClient;

  @Autowired
  public TikTokTranscriber(TranscribeClient transcribeClient) {
    this.transcribeClient = transcribeClient;
  }

  @Override
  public String transcribe(String filename) {
    String transcriptionJobName = startTranscriptionJob();
    return getTranscriptionJobFileUri(transcriptionJobName);
  }

  private String startTranscriptionJob() {
    String transcriptionJobName = TRANSCRIPTION_JOB_NAME;
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
    String transcriptFileUri = getTranscriptionJobResponse.transcriptionJob().transcript().transcriptFileUri();
    System.out.println(transcriptFileUri);

    return transcriptFileUri;
  }
}
