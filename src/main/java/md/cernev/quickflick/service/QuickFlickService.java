package md.cernev.quickflick.service;

public interface QuickFlickService {
  String process(String videoUrl);

  String process(String videoUrl, String format);
}
