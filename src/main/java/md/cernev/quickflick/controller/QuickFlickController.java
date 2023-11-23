package md.cernev.quickflick.controller;

import md.cernev.quickflick.ai.OpenAIProcessorImpl;
import md.cernev.quickflick.scrapper.InstagramScrapper;
import md.cernev.quickflick.scrapper.TikTokScrapper;
import md.cernev.quickflick.service.QuickFlickServiceImpl;
import md.cernev.quickflick.transcriber.LocalTranscriber;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class QuickFlickController {
  private final TikTokScrapper tikTokScrapper;
  private final InstagramScrapper instagramScrapper;
  private final LocalTranscriber localTranscriber;
  private final OpenAIProcessorImpl openAIProcessor;
  private final QuickFlickServiceImpl quickFlickService;

  public QuickFlickController(TikTokScrapper tikTokScrapper, InstagramScrapper instagramScrapper, LocalTranscriber localTranscriber, OpenAIProcessorImpl openAIProcessor, QuickFlickServiceImpl quickFlickService) {
    this.tikTokScrapper = tikTokScrapper;
    this.instagramScrapper = instagramScrapper;
    this.localTranscriber = localTranscriber;
    this.openAIProcessor = openAIProcessor;
    this.quickFlickService = quickFlickService;
  }

  @GetMapping(value = "/process", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public String process(@RequestParam String url) {
    return quickFlickService.process(url);
  }

  @GetMapping("/scrap/tiktok")
  @ResponseBody
  public void scrapTikTok(@RequestParam String url) {
    tikTokScrapper.scrap(url);
  }

  @GetMapping("/scrap/instagram")
  @ResponseBody
  public void scrapInstagram(@RequestParam String url) {
    instagramScrapper.scrap(url);
  }

  @GetMapping("/transcribe")
  @ResponseBody
  public String transcribe(@RequestParam String filename) {
    return localTranscriber.transcribe(filename);
  }

  @PostMapping("/summarize")
  @ResponseBody
  public String summarize(@RequestBody String text) {
    return openAIProcessor.summarize(text);
  }
}
