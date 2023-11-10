package md.cernev.quickflick.controller;

import md.cernev.quickflick.scrapper.TikTokScrapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tiktok")
public class TikTokController {
  private TikTokScrapper tikTokScrapper;

  public TikTokController(TikTokScrapper tikTokScrapper) {
    this.tikTokScrapper = tikTokScrapper;
  }

  @GetMapping("/scrap")
  @ResponseBody
  public void scrap(@RequestParam String url) {
    tikTokScrapper.scrap(url);
  }
}
