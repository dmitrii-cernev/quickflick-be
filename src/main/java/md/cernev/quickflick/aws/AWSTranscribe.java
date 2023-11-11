package md.cernev.quickflick.aws;

import md.cernev.quickflick.configuration.AwsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

@Component
public class AWSTranscribe {
  final TranscribeClient transcribeClient;

  @Autowired
  public AWSTranscribe(TranscribeClient transcribeClient) {this.transcribeClient = transcribeClient;}


  public String startTranscriptionJob(String mediaFileUri, String filename) {
    String transcriptionJobName = filename.replace(".mp4", "-job");

    StartTranscriptionJobRequest startTranscriptionJobRequest = StartTranscriptionJobRequest.builder()
        .identifyLanguage(true).transcriptionJobName(transcriptionJobName)
        .media(builder -> builder.mediaFileUri(mediaFileUri).build()).mediaFormat(MediaFormat.MP4)
        .outputBucketName(AwsConfiguration.BUCKET_NAME)
        .build();
    System.out.println("Starting transcription job " + transcriptionJobName);
    transcribeClient.startTranscriptionJob(startTranscriptionJobRequest);
    return transcriptionJobName;
  }

  public String getTranscriptionJobFileKey(String transcriptionJobName) {
    GetTranscriptionJobRequest request = GetTranscriptionJobRequest.builder()
        .transcriptionJobName(transcriptionJobName)
        .build();

    TranscriptionJobStatus transcriptionJobStatus = TranscriptionJobStatus.IN_PROGRESS;
    String transcriptFileUri = "";

    System.out.println("Waiting for job to complete...");
    while (transcriptionJobStatus != TranscriptionJobStatus.COMPLETED && transcriptionJobStatus != TranscriptionJobStatus.FAILED) {
      GetTranscriptionJobResponse response = transcribeClient.getTranscriptionJob(request);
      transcriptionJobStatus = response.transcriptionJob().transcriptionJobStatus();
      transcriptFileUri = response.transcriptionJob().transcript().transcriptFileUri();
    }
    System.out.println(transcriptFileUri);
    return transcriptionJobName + ".json";
  }
}
