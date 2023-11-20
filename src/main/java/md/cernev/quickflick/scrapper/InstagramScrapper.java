package md.cernev.quickflick.scrapper;

import lombok.SneakyThrows;
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

  @Override
  public String scrap(String url) {
    return scrapVideo(url);
  }

  @SneakyThrows
  private String scrapVideo(String url) {
    String videoId = getVideoId(url);
    String videoFileName = getVideoFileName(videoId);
    String videoInfoUrl = INSTAGRAM_URL.replace("{video_id}", videoId);
    logger.info("Started to save Instagram video...");
    AsyncHttpClient client = new DefaultAsyncHttpClient();
    String body = client
        .prepare("GET", videoInfoUrl)
        .execute()
        .toCompletableFuture()
        .get()
        .getResponseBody();
    String videoUrl = new JSONObject(body)
        .getJSONObject("graphql")
        .getJSONObject("shortcode_media")
        .getString("video_url");
    client.close();
    return saveVideo(videoUrl, videoFileName);
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

  String getVideoFileName(String videoId) {
    return videoId + ".mp4";
  }
}
