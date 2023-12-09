package md.cernev.quickflick.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TranscriptionEntity {
  private String id;
  private String userId;
  private String userIp;
  private String videoUrl;
  //todo: fix this
  private String videoDownloadUrl = "null";
  private String platform;
  private String title;
  private String description;
  private String transcription;
  //todo: add date
}
