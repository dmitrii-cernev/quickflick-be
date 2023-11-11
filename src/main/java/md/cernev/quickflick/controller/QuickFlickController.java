package md.cernev.quickflick.controller;

import md.cernev.quickflick.ai.OpenAIProcessorImpl;
import md.cernev.quickflick.scrapper.TikTokScrapper;
import md.cernev.quickflick.transcriber.VideoTranscriber;
import org.springframework.web.bind.annotation.*;

@RestController
public class QuickFlickController {
  private final TikTokScrapper tikTokScrapper;
  private final VideoTranscriber videoTranscriber;
  private final OpenAIProcessorImpl openAIProcessor;

  public QuickFlickController(TikTokScrapper tikTokScrapper, VideoTranscriber videoTranscriber, OpenAIProcessorImpl openAIProcessor) {
    this.tikTokScrapper = tikTokScrapper;
    this.videoTranscriber = videoTranscriber;
    this.openAIProcessor = openAIProcessor;
  }

  @GetMapping("/scrap/tiktok")
  @ResponseBody
  public void scrapTikTok(@RequestParam String url) {
    tikTokScrapper.scrap(url);
  }

  @GetMapping("/transcribe")
  @ResponseBody
  public String transcribe(@RequestParam String filename) {
    return videoTranscriber.transcribe(filename);
  }

  @PostMapping("/summarize")
  @ResponseBody
  public String summarize(@RequestBody String text) {
    return openAIProcessor.summarize(text);
  }
}
