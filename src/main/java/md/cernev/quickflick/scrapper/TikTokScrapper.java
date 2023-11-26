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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static md.cernev.quickflick.configuration.AwsConfiguration.VIDEOS_FOLDER;

@Service
public class TikTokScrapper extends Scrapper {
  public static final String TIKTOK_DOWNLOAD_API_1 = "https://tiktok82.p.rapidapi.com/getDownloadVideo";
  public static final String TIKTOK_DOWNLOAD_API_2 = "https://tiktok-downloader-download-tiktok-videos-without-watermark.p.rapidapi.com/index";
  @Value("${rapidapi.key}")
  private String rapidApiKey;
  public static final String USER_REGEX = "@([^\\/]+)";
  public static final String VIDEO_ID_REGEX = "\\/video\\/(\\d+)";
  public static final String RAPID_API_TIKTOK_1 = "tiktok82.p.rapidapi.com";
  public static final String RAPID_API_TIKTOK_2 = "tiktok-downloader-download-tiktok-videos-without-watermark.p.rapidapi.com";
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
      AsyncHttpClient client = new DefaultAsyncHttpClient();
    return getUsingRapidAPI2(url, client);
  }

  private String getUsingRapiAPI1(String url, AsyncHttpClient client) throws InterruptedException, ExecutionException {
    String body = client
        .prepare("GET", TIKTOK_DOWNLOAD_API_1 + "?video_url=" + url)
        .setHeader("X-RapidAPI-Key", rapidApiKey)
        .setHeader("X-RapidAPI-Host", RAPID_API_TIKTOK_1)
          .execute()
          .toCompletableFuture()
        .get()
        .getResponseBody();
    return new JSONObject(body).getJSONArray("url_list").getString(0);
  }

  /**
   * Limit: 1000 requests per month. 2 requests per second.
   *
   * @param url
   * @param client
   * @return
   * @throws InterruptedException
   * @throws ExecutionException
   */
  private String getUsingRapidAPI2(String url, AsyncHttpClient client) throws InterruptedException, ExecutionException {
    String body = client
        .prepare("GET", TIKTOK_DOWNLOAD_API_2 + "?url=" + url)
        .setHeader("X-RapidAPI-Key", rapidApiKey)
        .setHeader("X-RapidAPI-Host", RAPID_API_TIKTOK_2)
        .execute()
        .toCompletableFuture()
        .get()
        .getResponseBody();
    return new JSONObject(body).getJSONArray("video").getString(0);
  }

  private String getFilename(String videoUrl) {
    Pattern userName = Pattern.compile(USER_REGEX);
    Pattern videoId = Pattern.compile(VIDEO_ID_REGEX);
    Matcher userNameMatcher = userName.matcher(videoUrl);
    Matcher videoIdMatcher = videoId.matcher(videoUrl);
    if (userNameMatcher.find() && videoIdMatcher.find()) {
      return userNameMatcher.group(1) + "_" + videoIdMatcher.group(1) + ".mp4";
    }
    return "unnamed_video.mp4";
  }
}
