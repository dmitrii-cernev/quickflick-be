package md.cernev.quickflick.transcriber;

public interface Transcriber {

  /**
   * @param filePath path to file that needs to be transcribed
   * @return transcription text
   */
  String transcribe(String filePath);
}
