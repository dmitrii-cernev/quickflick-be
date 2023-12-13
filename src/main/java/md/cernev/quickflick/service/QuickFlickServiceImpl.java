package md.cernev.quickflick.service;

import md.cernev.quickflick.ai.OpenAIProcessorImpl;
import md.cernev.quickflick.aws.AWSDynamoDb;
import md.cernev.quickflick.dto.TranscriptionDto;
import md.cernev.quickflick.entity.TranscriptionEntity;
import md.cernev.quickflick.scrapper.InstagramScrapper;
import md.cernev.quickflick.scrapper.ShortsScrapper;
import md.cernev.quickflick.scrapper.TikTokScrapper;
import md.cernev.quickflick.transcriber.Transcriber;
import md.cernev.quickflick.util.Format;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static md.cernev.quickflick.util.Format.*;

@Service
public class QuickFlickServiceImpl implements QuickFlickService {
  private final TikTokScrapper tikTokScrapper;
  private final InstagramScrapper instagramScrapper;
  private final ShortsScrapper shortsScrapper;
  private final Transcriber transcriber;
  private final OpenAIProcessorImpl openAIProcessor;
  private final AWSDynamoDb awsDynamoDb;
  private final TranscriptionEntity transcriptionEntity = new TranscriptionEntity();

  public QuickFlickServiceImpl(TikTokScrapper tikTokScrapper, InstagramScrapper instagramScrapper, ShortsScrapper shortsScrapper, Transcriber transcriber, OpenAIProcessorImpl openAIProcessor, AWSDynamoDb awsDynamoDb) {
    this.tikTokScrapper = tikTokScrapper;
    this.instagramScrapper = instagramScrapper;
    this.shortsScrapper = shortsScrapper;
    this.transcriber = transcriber;
    this.openAIProcessor = openAIProcessor;
    this.awsDynamoDb = awsDynamoDb;
  }


  @Override
  public String process(String videoUrl) {
    return process(videoUrl, "0.0.0.0");
  }

  @Override
  public String process(String videoUrl, String userIp) {
    transcriptionEntity.setUserIp(userIp);
    return processVideoURL(videoUrl);
  }

  @Override
  public String getTranscriptionsByUserIP(String userIp) {
    List<TranscriptionDto> items = awsDynamoDb.getTranscriptionsByUserIP(userIp);
    return new JSONArray(items).toString();
  }

  private String processVideoURL(String videoUrl) {
    Format format = getFormat(videoUrl);
    transcriptionEntity.setVideoUrl(videoUrl);
    transcriptionEntity.setPlatform(format.getFormat());
    return switch (format) {
      case TIKTOK -> processTikTok(videoUrl);
      case INSTAGRAM -> processInstagram(videoUrl);
      case SHORTS -> processShorts(videoUrl);
      default -> throw new RuntimeException("Unsupported video format.");
    };

  }

  private Format getFormat(String videoUrl) {
    if (videoUrl.contains("tiktok")) {
      return TIKTOK;
    } else if (videoUrl.contains("instagram")) {
      return INSTAGRAM;
    } else if (videoUrl.contains("youtube")) {
      return SHORTS;
    } else {
      return UNKNOWN;
    }
  }

  private String processShorts(String videoUrl) {
    String filePath = shortsScrapper.scrap(videoUrl);
    return processVideo(filePath);
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
