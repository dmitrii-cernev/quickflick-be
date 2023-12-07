package md.cernev.quickflick.reactive.transcriber;

import md.cernev.quickflick.configuration.AwsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.annotations.NotNull;
import software.amazon.awssdk.services.eventbridge.EventBridgeAsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.transcribe.TranscribeAsyncClient;
import software.amazon.awssdk.services.transcribe.model.MediaFormat;
import software.amazon.awssdk.services.transcribe.model.StartTranscriptionJobRequest;

import static md.cernev.quickflick.aws.AWSTranscribe.TRANSCRIPTIONS_FOLDER;

@Service
public class AWSReactiveTranscriber {
  private static final Logger logger = LoggerFactory.getLogger(AWSReactiveTranscriber.class);
  private final TranscribeAsyncClient transcribeAsyncClient;
  private final S3AsyncClient s3AsyncClient;
  private final EventBridgeAsyncClient eventBridgeAsyncClient;

  public AWSReactiveTranscriber(TranscribeAsyncClient transcribeAsyncClient, S3AsyncClient s3AsyncClient) {
    this.transcribeAsyncClient = transcribeAsyncClient;
    this.s3AsyncClient = s3AsyncClient;
    eventBridgeAsyncClient = EventBridgeAsyncClient.create();
  }

  public Mono<String> transcribe(String mediaFileUri) {
    //todo
    return Mono.fromCallable(() -> startTranscriptionJob(mediaFileUri))
        .flatMap(this::waitForTranscriptionJobCompletion);
  }

  private Mono<String> waitForTranscriptionJobCompletion(String s) {
    //todo
    return Mono.just(s);
  }

  //todo: currently it is blocking
  private String startTranscriptionJob(String mediaFileUri) {
    String transcriptionJobName = getTranscriptionJobName(mediaFileUri);
    StartTranscriptionJobRequest startTranscriptionJobRequest = StartTranscriptionJobRequest.builder()
        .identifyLanguage(true)
        .transcriptionJobName(transcriptionJobName)
        .outputBucketName(AwsConfiguration.BUCKET_NAME)
        .outputKey(TRANSCRIPTIONS_FOLDER)
        .media(builder -> builder.mediaFileUri(mediaFileUri).build()).mediaFormat(MediaFormat.MP4)
        .build();
    logger.info("Starting transcription job {} for file {}", transcriptionJobName, mediaFileUri);
    transcribeAsyncClient.startTranscriptionJob(startTranscriptionJobRequest);
    return transcriptionJobName;
  }

  @NotNull
  private String getTranscriptionJobFileKey(String transcriptionJobName) {
    return TRANSCRIPTIONS_FOLDER + transcriptionJobName + ".json";
  }

  @NotNull
  private String getTranscriptionJobName(String mediaFileUri) {
    return String.valueOf(mediaFileUri.hashCode()) + System.currentTimeMillis();
  }
}
