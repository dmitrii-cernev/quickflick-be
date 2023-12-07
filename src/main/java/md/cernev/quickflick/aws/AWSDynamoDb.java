package md.cernev.quickflick.aws;

import md.cernev.quickflick.entity.TranscriptionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class AWSDynamoDb {
  private static final Logger logger = LoggerFactory.getLogger(AWSDynamoDb.class);
  DynamoDbClient dynamoDbClient;

  @Value("${aws.dynamodb.tableName}")
  private String tableName;

  @Autowired
  public AWSDynamoDb(DynamoDbClient dynamoDbClient) {
    this.dynamoDbClient = dynamoDbClient;
  }

  public void putItem(TranscriptionEntity entity) {
    Map<String, AttributeValue> itemValues = new HashMap<>();
    itemValues.put("id", AttributeValue.builder().s(entity.getId()).build());
    itemValues.put("user_id", AttributeValue.builder().s(entity.getUserId()).build());
    itemValues.put("userIp", AttributeValue.builder().s(entity.getUserIp()).build());
    itemValues.put("videoUrl", AttributeValue.builder().s(entity.getVideoUrl()).build());
    itemValues.put("videoDownloadUrl", AttributeValue.builder().s(entity.getVideoDownloadUrl()).build());
    itemValues.put("platform", AttributeValue.builder().s(entity.getPlatform()).build());
    itemValues.put("title", AttributeValue.builder().s(entity.getTitle()).build());
    itemValues.put("description", AttributeValue.builder().s(entity.getDescription()).build());
    itemValues.put("transcription", AttributeValue.builder().s(entity.getTranscription()).build());
    PutItemRequest putItemRequest = PutItemRequest.builder()
        .item(itemValues)
        .tableName(tableName)
        .build();
    logger.info("Putting item in DynamoDB: {}", entity.getId());
    try {
      dynamoDbClient.putItem(putItemRequest);
    } catch (Exception e) {
      logger.error("Error while putting item in DynamoDB: {}", e.getMessage());
    }
  }

}
