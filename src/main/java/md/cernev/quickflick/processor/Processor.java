package md.cernev.quickflick.processor;

public interface Processor {

  void getVideo(String url);

  void transcribeVideo(String video);

  void processVideo(String video);
}
