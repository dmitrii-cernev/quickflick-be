package md.cernev.quickflick.service;

import md.cernev.quickflick.ai.OpenAIProcessorImpl;
import md.cernev.quickflick.scrapper.TikTokScrapper;
import md.cernev.quickflick.transcriber.VideoTranscriber;
import org.springframework.stereotype.Service;

@Service
public class QuickFlickServiceImpl implements QuickFlickService {
  private final TikTokScrapper tikTokScrapper;
  private final VideoTranscriber transcriber;
  private final OpenAIProcessorImpl openAIProcessor;

  public QuickFlickServiceImpl(TikTokScrapper tikTokScrapper, VideoTranscriber transcriber, OpenAIProcessorImpl openAIProcessor) {
    this.tikTokScrapper = tikTokScrapper;
    this.transcriber = transcriber;
    this.openAIProcessor = openAIProcessor;
  }

  @Override
  public String process(String videoUrl) {
    String fileName = tikTokScrapper.scrap(videoUrl);
    String transcript = transcriber.transcribe(fileName);
    String summarize = openAIProcessor.summarize(transcript);
    return summarize;
  }
}
