package md.cernev.quickflick.scrapper;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TikTokScrapper implements Scrapper {

  public static final String TIKTOK_DOWNLOAD_API = "https://tiktok82.p.rapidapi.com/getDownloadVideo";
  public static final String RAPID_API_KEY = "e5d55f2ebdmsh1fdc26168bba541p18415cjsne46c1d57a3f9";
  public static final String USER_REGEX = "@([^\\/]+)";
  public static final String VIDEO_ID_REGEX = "\\/video\\/(\\d+)";
  public static final String RAPID_API_TIKTOK = "tiktok82.p.rapidapi.com";
  public static final String VIDEO_DIRECTORY = "videos/";

  /**
   * Scraps TikTok video from the given url and saves it to the local filesystem
   *
   * @param url - url of the TikTok video
   * @return saved file path
   */
  @Override
  public String scrap(String url) {
    return scrapUsingRapidApi(url);
  }

  private String scrapUsingRapidApi(String url) {
    String videoFileName = getVideoFileName(url);
    try {
      System.out.println("Started to save video...");
      AsyncHttpClient client = new DefaultAsyncHttpClient();
      client
          .prepare("GET", TIKTOK_DOWNLOAD_API + "?video_url=" + url)
          .setHeader("X-RapidAPI-Key", RAPID_API_KEY)
          .setHeader("X-RapidAPI-Host", RAPID_API_TIKTOK)
          .execute()
          .toCompletableFuture()
          .thenAccept(response -> {
            String body = response.getResponseBody();
            String videoUrl = new JSONObject(body).getJSONArray("url_list").getString(0);
            saveVideo(videoUrl, videoFileName);
          })
          .join();
      client.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return videoFileName;
  }

  private void saveVideo(String videoUrl, String videoFileName) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet(videoUrl);
    try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
      saveVideo(response, videoFileName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getVideoFileName(String videoUrl) {
    Pattern userName = Pattern.compile(USER_REGEX);
    Pattern videoId = Pattern.compile(VIDEO_ID_REGEX);
    Matcher userNameMatcher = userName.matcher(videoUrl);
    Matcher videoIdMatcher = videoId.matcher(videoUrl);
    if (userNameMatcher.find() && videoIdMatcher.find()) {
      return VIDEO_DIRECTORY + userNameMatcher.group(1) + "_" + videoIdMatcher.group(1) + ".mp4";
    }
    return VIDEO_DIRECTORY + "unnamed_video.mp4";
  }

  private void saveVideo(CloseableHttpResponse response, String filename) throws IOException {
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      InputStream content = entity.getContent();
      File outputFile = new File(filename);
      try {
        OutputStream outputStream = new FileOutputStream(outputFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = content.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
        content.close();
        outputStream.close();
        System.out.println("Video saved to file: " + outputFile.getAbsolutePath());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
