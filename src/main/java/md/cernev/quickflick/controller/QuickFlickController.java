package md.cernev.quickflick.controller;

import md.cernev.quickflick.ai.OpenAIProcessorImpl;
import md.cernev.quickflick.scrapper.TikTokScrapper;
import md.cernev.quickflick.service.QuickFlickServiceImpl;
import md.cernev.quickflick.transcriber.VideoTranscriber;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
public class QuickFlickController {
  private final TikTokScrapper tikTokScrapper;
  private final VideoTranscriber videoTranscriber;
  private final OpenAIProcessorImpl openAIProcessor;
  private final QuickFlickServiceImpl quickFlickService;

  public QuickFlickController(TikTokScrapper tikTokScrapper, VideoTranscriber videoTranscriber, OpenAIProcessorImpl openAIProcessor, QuickFlickServiceImpl quickFlickService) {
    this.tikTokScrapper = tikTokScrapper;
    this.videoTranscriber = videoTranscriber;
    this.openAIProcessor = openAIProcessor;
    this.quickFlickService = quickFlickService;
  }


  @GetMapping(value = "/process/tiktok", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public String processTikTok(@RequestParam String url) {
    return quickFlickService.process(url);
  }

  @GetMapping("/scrap/tiktok")
  @ResponseBody
  public void scrapTikTok(@RequestParam String url) {
    tikTokScrapper.scrap(url);
  }

  @GetMapping("/transcribe")
  @ResponseBody
  public String transcribe(@RequestParam String filename) {
    File file = new File(filename);
    return videoTranscriber.transcribe(file);
  }

  @PostMapping("/summarize")
  @ResponseBody
  public String summarize(@RequestBody String text) {
    return openAIProcessor.summarize(text);
  }
}
