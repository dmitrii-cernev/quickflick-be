package md.cernev.quickflick.reactive.scrapper;

import md.cernev.quickflick.reactive.storage.AWSReactiveStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TikTokReactiveScrapper {
  public static final String TIKTOK_DOWNLOAD_API = "https://tiktok-downloader-download-tiktok-videos-without-watermark.p.rapidapi.com/index";
  public static final String RAPID_API_TIKTOK = "tiktok-downloader-download-tiktok-videos-without-watermark.p.rapidapi.com";
  private static final String VIDEOS_FOLDER = "videos/";
  private final Logger logger = LoggerFactory.getLogger(TikTokReactiveScrapper.class);
  private final AWSReactiveStorageService storageService;
  private final String rapidApiKey;

  @Autowired
  public TikTokReactiveScrapper(AWSReactiveStorageService storageService, @Value("${rapidapi.key}") String rapidApiKey) {
    this.storageService = storageService;
    this.rapidApiKey = rapidApiKey;
  }

  public Mono<String> scrap(String url) {
    return Mono.defer(() -> {
      String downloadUrl = getDownloadURL(url);
      String filepath = getFilepath(url);

      return WebClient.create()
          .get()
          .uri(downloadUrl)
          .header("X-RapidAPI-Key", rapidApiKey)
          .header("X-RapidAPI-Host", RAPID_API_TIKTOK)
          .retrieve()
          .bodyToMono(byte[].class)
          .flatMap(videoData -> storageService.save(videoData, filepath));
    });
  }

  private String getDownloadURL(String url) {
    logger.info("Getting TikTok video URL...");
    return TIKTOK_DOWNLOAD_API + "?url=" + url;
  }

  private String getFilepath(String videoUrl) {
    return VIDEOS_FOLDER + videoUrl.hashCode() + ".mp4";
  }
}
