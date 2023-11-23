package md.cernev.quickflick.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LocalStorageService implements StorageService {
  public static final Logger logger = LoggerFactory.getLogger(LocalStorageService.class);
  public static final String VIDEO_DIRECTORY = "videos/";

  @Override
  public String save(byte[] fileData, String filename) {
    String filePath = VIDEO_DIRECTORY + filename;

    File outputFile = new File(filePath);
    try {
      OutputStream outputStream = new FileOutputStream(outputFile);
      outputStream.write(fileData);
      outputStream.close();
      logger.info("Video saved to file: {}", outputFile.getAbsolutePath());
    } catch (IOException e) {
      logger.error("Could not save video {}", filePath, e);
    }

    return filePath;
  }
}

