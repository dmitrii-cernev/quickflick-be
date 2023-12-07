package md.cernev.quickflick.service;

import md.cernev.quickflick.ai.OpenAIProcessorImpl;
import md.cernev.quickflick.aws.AWSDynamoDb;
import md.cernev.quickflick.entity.TranscriptionEntity;
import md.cernev.quickflick.scrapper.InstagramScrapper;
import md.cernev.quickflick.scrapper.TikTokScrapper;
import md.cernev.quickflick.transcriber.Transcriber;
import md.cernev.quickflick.util.Format;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static md.cernev.quickflick.util.Format.INSTAGRAM;
import static md.cernev.quickflick.util.Format.TIKTOK;

@Service
public class QuickFlickServiceImpl implements QuickFlickService {
  private final TikTokScrapper tikTokScrapper;
  private final InstagramScrapper instagramScrapper;
  private final Transcriber transcriber;
  private final OpenAIProcessorImpl openAIProcessor;
  private final AWSDynamoDb awsDynamoDb;
  private final TranscriptionEntity transcriptionEntity = new TranscriptionEntity();

  public QuickFlickServiceImpl(TikTokScrapper tikTokScrapper, InstagramScrapper instagramScrapper, @Qualifier("AWSTranscriber") Transcriber transcriber, OpenAIProcessorImpl openAIProcessor, AWSDynamoDb awsDynamoDb) {
    this.tikTokScrapper = tikTokScrapper;
    this.instagramScrapper = instagramScrapper;
    this.transcriber = transcriber;
    this.openAIProcessor = openAIProcessor;
    this.awsDynamoDb = awsDynamoDb;
  }


  @Override
  public String process(String videoUrl) {
    Format format = videoUrl.contains(INSTAGRAM.getFormat()) ? INSTAGRAM : TIKTOK;
    transcriptionEntity.setUserIp("0.0.0.0");
    return process(videoUrl, format);
  }

  @Override
  public String process(String videoUrl, String userIp) {
    Format format = videoUrl.contains(INSTAGRAM.getFormat()) ? INSTAGRAM : TIKTOK;
    transcriptionEntity.setUserIp(userIp);
    return process(videoUrl, format);
  }

  private String process(String videoUrl, Format format) {
    transcriptionEntity.setVideoUrl(videoUrl);
    transcriptionEntity.setPlatform(format.getFormat());
    return switch (format) {
      case TIKTOK -> processTikTok(videoUrl);
      case INSTAGRAM -> processInstagram(videoUrl);
      default -> throw new RuntimeException("Unsupported video format.");
    };

  }

  private String processTikTok(String videoUrl) {
    String filePath = tikTokScrapper.scrap(videoUrl);
    return processVideo(filePath);
  }

  private String processInstagram(String videoUrl) {
    String filePath = instagramScrapper.scrap(videoUrl);
    return processVideo(filePath);
  }

  private String processVideo(String filePath) {
    String transcript = transcriber.transcribe(filePath);
    String summarize = openAIProcessor.summarize(transcript);
    var json = new JSONObject(summarize);
    json = json.put("transcription", transcript);
    transcriptionEntity.setId(UUID.randomUUID().toString());
    transcriptionEntity.setUserId(String.valueOf(transcriptionEntity.getUserIp().hashCode()));
    transcriptionEntity.setTranscription(transcript);
    transcriptionEntity.setTitle(json.getString("title"));
    transcriptionEntity.setDescription(json.getString("summary"));
    awsDynamoDb.putItem(transcriptionEntity);
    return json.toString();
  }
}
