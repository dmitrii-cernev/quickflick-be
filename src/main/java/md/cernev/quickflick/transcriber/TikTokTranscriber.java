package md.cernev.quickflick.transcriber;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

import java.io.*;

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
//    String s3Path = uploadToS3(filename);
//    String transcriptionJobName = startTranscriptionJob(s3Path, filename);
    String transcriptionJobName = "weevelanguages_7296481599242243361job";
    String transcriptionJobFileKey = getTranscriptionJobFileKey(transcriptionJobName);
    String trasncribtionFileName = downloadFromS3(transcriptionJobFileKey, transcriptionJobFileKey);
    return getTranscript(trasncribtionFileName);
  }

  private String getTranscript(String trasncribtionFileName) {
    String transcript;
    try {
      FileReader transcriptFile = new FileReader(trasncribtionFileName);
      JSONTokener jsonTokener = new JSONTokener(transcriptFile);
      JSONObject jsonObject = new JSONObject(jsonTokener);
      transcript = jsonObject.getJSONObject("results")
          .getJSONArray("transcripts")
          .getJSONObject(0)
          .getString("transcript");
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    return transcript;
  }

  private String uploadToS3(String filename) {
    File file = new File(filename);
    PutObjectRequest request = PutObjectRequest.builder().bucket(BUCKET_NAME).key(file.getName())
        .build();
    PutObjectResponse response = s3Client.putObject(request, file.toPath());
    String s3Location = "s3://" + BUCKET_NAME + "/" + filename;

    System.out.println("File uploaded successfully. ETag: " + response.eTag());
    System.out.println("S3 Location: " + s3Location);
    return s3Location;
  }

  private String downloadFromS3(String fileFromS3, String filenameToSave) {
    GetObjectRequest request = GetObjectRequest.builder().bucket(BUCKET_NAME).key(fileFromS3).build();
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

  private String startTranscriptionJob(String mediaFileUri, String filename) {
    String transcriptionJobName = filename.replace(".mp4", "-job");

    StartTranscriptionJobRequest startTranscriptionJobRequest = StartTranscriptionJobRequest.builder()
        .identifyLanguage(true).transcriptionJobName(transcriptionJobName)
        .media(builder -> builder.mediaFileUri(mediaFileUri).build()).mediaFormat(MediaFormat.MP4)
        .outputBucketName(BUCKET_NAME)
        .build();

    StartTranscriptionJobResponse startTranscriptionJobResponse = transcribeClient.startTranscriptionJob(startTranscriptionJobRequest);
    System.out.println(startTranscriptionJobResponse.toString());
    return transcriptionJobName;
  }

  private String getTranscriptionJobFileKey(String transcriptionJobName) {
    GetTranscriptionJobRequest request = GetTranscriptionJobRequest.builder()
        .transcriptionJobName(transcriptionJobName)
        .build();

    TranscriptionJobStatus transcriptionJobStatus = TranscriptionJobStatus.IN_PROGRESS;
    String transcriptFileUri = "";

    while (transcriptionJobStatus != TranscriptionJobStatus.COMPLETED && transcriptionJobStatus != TranscriptionJobStatus.FAILED) {
      GetTranscriptionJobResponse response = transcribeClient.getTranscriptionJob(request);
      transcriptionJobStatus = response.transcriptionJob().transcriptionJobStatus();
      transcriptFileUri = response.transcriptionJob().transcript().transcriptFileUri();
      System.out.println(transcriptFileUri);
    }

    return transcriptionJobName + ".json";
  }
}
