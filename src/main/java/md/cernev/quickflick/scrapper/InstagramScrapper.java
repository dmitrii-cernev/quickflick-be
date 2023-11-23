package md.cernev.quickflick.scrapper;

import lombok.SneakyThrows;
import md.cernev.quickflick.storage.StorageService;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InstagramScrapper extends Scrapper {

  private static final String INSTAGRAM_URL = "https://www.instagram.com/p/{video_id}/?__a=1&__d=dis";
  private final Logger logger = LoggerFactory.getLogger(InstagramScrapper.class);

  protected InstagramScrapper(StorageService storageService) {
    super(storageService);
  }

  @Override
  public String scrap(String url) {
    String downloadUrl = getDownloadURL(url);
    String filename = getVideoFileName(url);
    byte[] videoData = getVideoData(downloadUrl);
    return storageService.save(videoData, filename);
  }

  @SneakyThrows
  private String getDownloadURL(String url) {
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
