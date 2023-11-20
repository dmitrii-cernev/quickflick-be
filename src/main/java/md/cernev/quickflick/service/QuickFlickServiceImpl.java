package md.cernev.quickflick.service;

import md.cernev.quickflick.ai.OpenAIProcessorImpl;
import md.cernev.quickflick.scrapper.InstagramScrapper;
import md.cernev.quickflick.scrapper.TikTokScrapper;
import md.cernev.quickflick.transcriber.VideoTranscriber;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class QuickFlickServiceImpl implements QuickFlickService {
  private final TikTokScrapper tikTokScrapper;
  private final InstagramScrapper instagramScrapper;
  private final VideoTranscriber transcriber;
  private final OpenAIProcessorImpl openAIProcessor;

  public QuickFlickServiceImpl(TikTokScrapper tikTokScrapper, InstagramScrapper instagramScrapper, VideoTranscriber transcriber, OpenAIProcessorImpl openAIProcessor) {
    this.tikTokScrapper = tikTokScrapper;
    this.instagramScrapper = instagramScrapper;
    this.transcriber = transcriber;
    this.openAIProcessor = openAIProcessor;
  }

  @Override
  public String process(String videoUrl) {
    return processTikTok(videoUrl);
  }

  @Override
  public String process(String videoUrl, String format) {
    return switch (format) {
      case "tiktok" -> processTikTok(videoUrl);
      case "instagram" -> processInstagram(videoUrl);
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
    File file = new File(filePath);
    String transcript = transcriber.transcribe(file);
    String summarize = openAIProcessor.summarize(transcript);
    var json = new JSONObject(summarize);
    return json.put("transcription", transcript).toString();
  }
}
