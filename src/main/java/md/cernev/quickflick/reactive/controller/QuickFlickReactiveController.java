package md.cernev.quickflick.reactive.controller;

import md.cernev.quickflick.reactive.scrapper.TikTokReactiveScrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/reactive")
public class QuickFlickReactiveController {
  private final TikTokReactiveScrapper tikTokReactiveScrapper;

  public QuickFlickReactiveController(TikTokReactiveScrapper tikTokReactiveScrapper) {this.tikTokReactiveScrapper = tikTokReactiveScrapper;}

  @GetMapping("/scrap/tiktok")
  public Mono<String> scrapTikTok(@RequestParam String url) {
    return tikTokReactiveScrapper.scrap(url);
  }
}
