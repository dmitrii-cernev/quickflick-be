package md.cernev.quickflick.scrapper;

import lombok.SneakyThrows;
import md.cernev.quickflick.storage.StorageService;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static md.cernev.quickflick.configuration.AwsConfiguration.VIDEOS_FOLDER;

@Service
public class ShortsScrapper extends Scrapper {
  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ShortsScrapper.class);
  @Value("${rapidapi.key}")
  private String rapidApiKey;

  protected ShortsScrapper(StorageService storageService) {
    super(storageService);
  }

  @Override
  public String scrap(String url) {
    String downloadUrl = getDownloadURL(url);
    String filename = getFilename(url);
    byte[] videoData = getVideoData(downloadUrl);
    return storageService.save(videoData, filename, VIDEOS_FOLDER);
  }

  @SneakyThrows
  private String getDownloadURL(String url) {
    logger.info("Getting Youtube Shorts video url...");
    AsyncHttpClient client = new DefaultAsyncHttpClient();
    JSONObject requestBody = new JSONObject();
    requestBody.put("url", url);
    String body = client.prepare("POST", "https://youtube86.p.rapidapi.com/api/youtube/links")
        .setHeader("content-type", "application/json")
        .setHeader("X-RapidAPI-Key", rapidApiKey)
        .setHeader("X-Forwarded-For", "70.41.3.18")
        .setHeader("X-RapidAPI-Host", "youtube86.p.rapidapi.com")
        .setBody(requestBody.toString())
        .execute()
        .toCompletableFuture()
        .get()
        .getResponseBody();
    return new JSONArray(body).getJSONObject(0).getJSONArray("urls").getJSONObject(2).getString("url");
  }

  private String getFilename(String url) {
    return url.hashCode() + ".mp4";
  }
}
