package md.cernev.quickflick.transcriber;

import md.cernev.quickflick.aws.AWSS3;
import md.cernev.quickflick.aws.AWSTranscribe;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;

@Service
public class VideoTranscriber implements Transcriber {

  private final AWSTranscribe awsTranscribe;
  private final AWSS3 awss3;

  @Autowired
  public VideoTranscriber(AWSTranscribe awsTranscribe, AWSS3 awss3) {
    this.awsTranscribe = awsTranscribe;
    this.awss3 = awss3;
  }

  @Override
  public String transcribe(String filename) {
    return transcriptAwsBatch(filename);
  }

  private String transcriptAwsBatch(String filename) {
    String s3Path = awss3.uploadToS3(filename);
    String transcriptionJobName = awsTranscribe.startTranscriptionJob(s3Path, filename);
    String transcriptionJobFileKey = awsTranscribe.getTranscriptionJobFileKey(transcriptionJobName);
    String transcriptionFileName = awss3.downloadFromS3(transcriptionJobFileKey, transcriptionJobFileKey);
    return getTranscript(transcriptionFileName);
  }

  private String getTranscript(String transcriptionFileName) {
    String transcript;
    try {
      FileReader transcriptFile = new FileReader(transcriptionFileName);
      JSONTokener jsonTokener = new JSONTokener(transcriptFile);
      JSONObject jsonObject = new JSONObject(jsonTokener);
      transcript = jsonObject.getJSONObject("results")
          .getJSONArray("transcripts")
          .getJSONObject(0)
          .getString("transcript");
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    System.out.println("Successfully got the transcription.");
    return transcript;
  }
}
