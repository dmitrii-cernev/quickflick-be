package md.cernev.quickflick.scrapper;

import lombok.SneakyThrows;
import md.cernev.quickflick.storage.StorageService;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static md.cernev.quickflick.configuration.AwsConfiguration.VIDEOS_FOLDER;

@Service
public class InstagramScrapper extends Scrapper {

  private static final String INSTAGRAM_URL = "https://www.instagram.com/p/{video_id}/?__a=1&__d=dis";
  private final Logger logger = LoggerFactory.getLogger(InstagramScrapper.class);
  @Value("${rapidapi.key}")
  private String rapidApiKey;

  protected InstagramScrapper(StorageService storageService) {
    super(storageService);
  }

  @Override
  public String scrap(String url) {
    String downloadUrl = getDownloadURLRapidAPI(url);
    String filename = getVideoFileName(url);
    byte[] videoData = getVideoData(downloadUrl);
    return storageService.save(videoData, filename, VIDEOS_FOLDER);
  }

  /**
   * Limit: 100 requests per day. Note: seems like sometimes can not work.
   *
   * @param url
   * @return
   */
  @SneakyThrows
  private String getDownloadURLRapidAPI(String url) {
    logger.info("Getting Instagram video url...");
    AsyncHttpClient client = new DefaultAsyncHttpClient();
    String body = client
        .prepare("GET", "https://instagram-downloader-download-instagram-videos-stories1.p.rapidapi.com/?url=" + url)
        .setHeader("X-RapidAPI-Key", rapidApiKey)
        .setHeader("X-RapidAPI-Host", "instagram-downloader-download-instagram-videos-stories1.p.rapidapi.com")
        .execute()
        .get()
        .getResponseBody();
    String downloadUrl = new JSONArray(body).getString(0);
    client.close();
    return downloadUrl;
  }

  @SneakyThrows
  private String getDownloadURLManually(String url) {
    String videoId = getVideoId(url);
    String videoInfoUrl = INSTAGRAM_URL.replace("{video_id}", videoId);
    logger.info("Getting Instagram video url...");
    AsyncHttpClient client = new DefaultAsyncHttpClient();
    String body = client
        .prepare("GET", videoInfoUrl)
        .execute()
        .toCompletableFuture()
        .get()
        .getResponseBody();
    //for some reason there are different types of response
    JSONObject jsonObject = new JSONObject(body);
    if (jsonObject.has("status") && jsonObject.getString("status").equals("fail")) {
      logger.warn("Error with getting Instagram URL.");
      throw new RuntimeException(jsonObject.getString("message"));
    }
    return jsonObject.has("graphql") ? getStringWithGraphQLResponse(body) : getRegularResponse(body);
  }

  private String getRegularResponse(String body) {
    return new JSONObject(body)
        .getJSONArray("items")
        .getJSONObject(0)
        .getJSONArray("video_versions")
        .getJSONObject(0)
        .getString("url");
  }

  private String getStringWithGraphQLResponse(String body) {
    return new JSONObject(body)
        .getJSONObject("graphql")
        .getJSONObject("shortcode_media")
        .getString("video_url");
  }

  private String getVideoId(String url) {
    Pattern pattern = Pattern.compile("/reel/([^/?]+)");
    Matcher matcher = pattern.matcher(url);
    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return "";
    }
  }

  String getVideoFileName(String url) {
    String videoId = getVideoId(url);
    return videoId + ".mp4";
  }
}
