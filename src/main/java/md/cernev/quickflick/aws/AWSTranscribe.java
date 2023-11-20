package md.cernev.quickflick.aws;

import md.cernev.quickflick.configuration.AwsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

@Component
public class AWSTranscribe {

  private final Logger logger = LoggerFactory.getLogger(AWSTranscribe.class);

  public static final String TRANSCRIPTIONS_FOLDER = "transcriptions/";
  final TranscribeClient transcribeClient;

  @Autowired
  public AWSTranscribe(TranscribeClient transcribeClient) {this.transcribeClient = transcribeClient;}


  public String startTranscriptionJob(String mediaFileUri, String filename) {
    String transcriptionJobName = filename.replace(".mp4", "-job");

    StartTranscriptionJobRequest startTranscriptionJobRequest = StartTranscriptionJobRequest.builder()
        .identifyLanguage(true)
        .transcriptionJobName(transcriptionJobName)
        .outputBucketName(AwsConfiguration.BUCKET_NAME)
        .outputKey(TRANSCRIPTIONS_FOLDER)
        .media(builder -> builder.mediaFileUri(mediaFileUri).build()).mediaFormat(MediaFormat.MP4)
        .build();
    logger.info("Starting transcription job {}", transcriptionJobName);
    transcribeClient.startTranscriptionJob(startTranscriptionJobRequest);
    return transcriptionJobName;
  }

  public String getTranscriptionJobFileKey(String transcriptionJobName) {
    GetTranscriptionJobRequest request = GetTranscriptionJobRequest.builder()
        .transcriptionJobName(transcriptionJobName)
        .build();

    TranscriptionJobStatus transcriptionJobStatus = TranscriptionJobStatus.IN_PROGRESS;

    logger.info("Waiting for job to complete...");
    while (transcriptionJobStatus != TranscriptionJobStatus.COMPLETED && transcriptionJobStatus != TranscriptionJobStatus.FAILED) {
      GetTranscriptionJobResponse response = transcribeClient.getTranscriptionJob(request);
      transcriptionJobStatus = response.transcriptionJob().transcriptionJobStatus();
    }
    logger.info("Job succeed.");
    return TRANSCRIPTIONS_FOLDER + transcriptionJobName + ".json";
  }
}
