package md.cernev.quickflick.reactive.scrapper;

import md.cernev.quickflick.reactive.storage.AWSReactiveStorageService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@Component
public class TikTokReactiveScrapper {
  public static final String TIKTOK_DOWNLOAD_API = "https://tiktok-downloader-download-tiktok-videos-without-watermark.p.rapidapi.com/index";
  public static final String RAPID_API_TIKTOK = "tiktok-downloader-download-tiktok-videos-without-watermark.p.rapidapi.com";
  private static final String VIDEOS_FOLDER = "videos/";
  private final Logger logger = LoggerFactory.getLogger(TikTokReactiveScrapper.class);
  private final AWSReactiveStorageService storageService;
  private final String rapidApiKey;
  private final WebClient webClient = WebClient.create();

  @Autowired
  public TikTokReactiveScrapper(AWSReactiveStorageService storageService, @Value("${rapidapi.key}") String rapidApiKey) {
    this.storageService = storageService;
    this.rapidApiKey = rapidApiKey;
  }

  public Mono<String> scrap(String url) {
    return Mono.defer(() -> {
      String filepath = getFilepath(url);
      return webClient
          .get().uri(TIKTOK_DOWNLOAD_API + "?url=" + url)
          .header("X-RapidAPI-Key", rapidApiKey)
          .header("X-RapidAPI-Host", RAPID_API_TIKTOK)
          .retrieve().bodyToMono(String.class)
          .map(body -> new JSONObject(body).getJSONArray("video").getString(0)) // get download url
          .flatMap(downloadUrl -> webClient.get().uri(downloadUrl).exchangeToMono(clientResponse -> {
            logger.info("Getting video content...");
            long contentLength = clientResponse.headers().asHttpHeaders().getContentLength();
            return storageService.save(clientResponse.bodyToFlux(ByteBuffer.class), filepath, contentLength);
          }));
    });
  }

  private String getFilepath(String videoUrl) {
    return VIDEOS_FOLDER + videoUrl.hashCode() + ".mp4";
  }
}
