package md.cernev.quickflick.service;

import md.cernev.quickflick.ai.OpenAIProcessorImpl;
import md.cernev.quickflick.scrapper.InstagramScrapper;
import md.cernev.quickflick.scrapper.TikTokScrapper;
import md.cernev.quickflick.transcriber.Transcriber;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class QuickFlickServiceImpl implements QuickFlickService {
  public static final String INSTAGRAM = "instagram";
  public static final String TIKTOK = "tiktok";
  private final TikTokScrapper tikTokScrapper;
  private final InstagramScrapper instagramScrapper;
  private final Transcriber transcriber;
  private final OpenAIProcessorImpl openAIProcessor;

  public QuickFlickServiceImpl(TikTokScrapper tikTokScrapper, InstagramScrapper instagramScrapper, @Qualifier("AWSTranscriber") Transcriber transcriber, OpenAIProcessorImpl openAIProcessor) {
    this.tikTokScrapper = tikTokScrapper;
    this.instagramScrapper = instagramScrapper;
    this.transcriber = transcriber;
    this.openAIProcessor = openAIProcessor;
  }

  @Override
  public String process(String videoUrl) {
    String format = videoUrl.contains(INSTAGRAM) ? INSTAGRAM : TIKTOK;
    return process(videoUrl, format);
  }

  private String process(String videoUrl, String format) {
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
    return json.put("transcription", transcript).toString();
  }
}
