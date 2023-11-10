package md.cernev.quickflick.controller;

import md.cernev.quickflick.scrapper.TikTokScrapper;
import md.cernev.quickflick.transcriber.TikTokTranscriber;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tiktok")
public class TikTokController {
  private final TikTokScrapper tikTokScrapper;
  private final TikTokTranscriber tikTokTranscriber;

  public TikTokController(TikTokScrapper tikTokScrapper, TikTokTranscriber tikTokTranscriber) {
    this.tikTokScrapper = tikTokScrapper;
    this.tikTokTranscriber = tikTokTranscriber;
  }

  @GetMapping("/scrap")
  @ResponseBody
  public void scrap(@RequestParam String url) {
    tikTokScrapper.scrap(url);
  }

  @GetMapping("/transcribe")
  @ResponseBody
  public String transcribe(@RequestParam String url) {
    return tikTokTranscriber.transcribe(url);
  }
}
