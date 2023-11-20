package md.cernev.quickflick.scrapper;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public abstract class Scrapper {
  public static final String VIDEO_DIRECTORY = "videos/";
  private final Logger logger = LoggerFactory.getLogger(Scrapper.class);

  public abstract String scrap(String url);

  private String saveVideo(CloseableHttpResponse response, String filename) throws IOException {
    String filePath = VIDEO_DIRECTORY + filename;
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      InputStream content = entity.getContent();
      File outputFile = new File(filePath);
      try {
        OutputStream outputStream = new FileOutputStream(outputFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = content.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
        content.close();
        outputStream.close();
        logger.info("Video saved to file: {}", outputFile.getAbsolutePath());
      } catch (IOException e) {
        logger.error("Could not save video {}", filePath, e);
      }
    }
    return filePath;
  }

  String saveVideo(String videoUrl, String videoFileName) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet(videoUrl);
    try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
      return saveVideo(response, videoFileName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
