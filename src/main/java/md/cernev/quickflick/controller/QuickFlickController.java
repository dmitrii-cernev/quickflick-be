package md.cernev.quickflick.controller;

import md.cernev.quickflick.ai.OpenAIProcessorImpl;
import md.cernev.quickflick.scrapper.InstagramScrapper;
import md.cernev.quickflick.scrapper.ShortsScrapper;
import md.cernev.quickflick.scrapper.TikTokScrapper;
import md.cernev.quickflick.service.QuickFlickServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
//todo: should not be global
@CrossOrigin
public class QuickFlickController {
  private final TikTokScrapper tikTokScrapper;
  private final InstagramScrapper instagramScrapper;
  private final ShortsScrapper shortsScrapper;
  private final OpenAIProcessorImpl openAIProcessor;
  private final QuickFlickServiceImpl quickFlickService;

  public QuickFlickController(TikTokScrapper tikTokScrapper, InstagramScrapper instagramScrapper, ShortsScrapper shortsScrapper, OpenAIProcessorImpl openAIProcessor, QuickFlickServiceImpl quickFlickService) {
    this.tikTokScrapper = tikTokScrapper;
    this.instagramScrapper = instagramScrapper;
    this.shortsScrapper = shortsScrapper;
    this.openAIProcessor = openAIProcessor;
    this.quickFlickService = quickFlickService;
  }

  @GetMapping(value = "/process", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public String process(@RequestParam String url, @RequestParam String userIp) {
    return quickFlickService.process(url, userIp);
  }

  @GetMapping(value = "/get/{userIp}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public String getByUserIP(@PathVariable String userIp) {
    return quickFlickService.getTranscriptionsByUserIP(userIp);
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

  @GetMapping("/scrap/shorts")
  @ResponseBody
  public void scrapShorts(@RequestParam String url) {
    shortsScrapper.scrap(url);
  }

  @PostMapping("/summarize")
  @ResponseBody
  public String summarize(@RequestBody String text) {
    return openAIProcessor.summarize(text);
  }
}
