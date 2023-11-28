package md.cernev.quickflick.storage;

public interface StorageService {
  /**
   * @param fileData Bytes of file data
   * @param filename Name of file to save
   * @return Path to saved file
   */

  String save(byte[] fileData, String filename, String dir);
}
