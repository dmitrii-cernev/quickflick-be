package md.cernev.quickflick.scrapper;

import md.cernev.quickflick.storage.StorageService;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class Scrapper {
  protected final StorageService storageService;
  private final Logger logger = LoggerFactory.getLogger(Scrapper.class);

  protected Scrapper(StorageService storageService) {this.storageService = storageService;}

  public abstract String scrap(String url);

  byte[] getVideoData(String downloadUrl) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet(downloadUrl);
    logger.info("Getting video content...");
    try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        return entity.getContent().readAllBytes();
      } else {
        logger.warn("Video content is null! downloadUrl: {}", downloadUrl);
        return new byte[]{};
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
