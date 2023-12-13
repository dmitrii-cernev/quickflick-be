package md.cernev.quickflick.scrapper;

import lombok.SneakyThrows;
import md.cernev.quickflick.storage.StorageService;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

import static md.cernev.quickflick.configuration.AwsConfiguration.VIDEOS_FOLDER;

@Service
public class TikTokScrapper extends Scrapper {
  public static final String TIKTOK_DOWNLOAD_API = "https://tiktok-downloader-download-tiktok-videos-without-watermark.p.rapidapi.com/index";
  @Value("${rapidapi.key}")
  private String rapidApiKey;
  public static final String RAPID_API_TIKTOK = "tiktok-downloader-download-tiktok-videos-without-watermark.p.rapidapi.com";
  private final Logger logger = LoggerFactory.getLogger(TikTokScrapper.class);

  protected TikTokScrapper(StorageService storageService) {
    super(storageService);
  }

  /**
   * Scraps TikTok video from the given url and saves it to the local filesystem
   *
   * @param url - url of the TikTok video
   * @return saved file path
   */
  @Override
  public String scrap(String url) {
    String downloadUrl = getDownloadURL(url);
    String filename = getFilename(url);
    byte[] videoData = getVideoData(downloadUrl);
    return storageService.save(videoData, filename, VIDEOS_FOLDER);
  }

  @SneakyThrows
  private String getDownloadURL(String url) {
    logger.info("Getting TikTok video url...");
    return getUsingRapidAPI(url);
  }

  /**
   * Limit: 1000 requests per month. 2 requests per second.
   *
   * @param url
   * @return
   * @throws InterruptedException
   * @throws ExecutionException
   */
  private String getUsingRapidAPI(String url) throws InterruptedException, ExecutionException {
    AsyncHttpClient client = new DefaultAsyncHttpClient();
    String body = client
        .prepare("GET", TIKTOK_DOWNLOAD_API + "?url=" + url)
        .setHeader("X-RapidAPI-Key", rapidApiKey)
        .setHeader("X-RapidAPI-Host", RAPID_API_TIKTOK)
        .execute()
        .toCompletableFuture()
        .get()
        .getResponseBody();
    return new JSONObject(body).getJSONArray("video").getString(0);
  }

  private String getFilename(String videoUrl) {
    return videoUrl.hashCode() + ".mp4";
  }
}
