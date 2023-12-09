package md.cernev.quickflick.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TranscriptionDto {
  private String videoUrl;
  private String title;
  private String description;
  private String transcription;
  private String platform;
}
