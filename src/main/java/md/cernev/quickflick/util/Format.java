package md.cernev.quickflick.util;

public enum Format {
  TIKTOK("tiktok"),
  INSTAGRAM("instagram");

  private final String format;

  Format(String format) {
    this.format = format;
  }

  public String getFormat() {
    return format;
  }
}