package md.cernev.quickflick.ai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class OpenAIProcessorImpl implements AIProcessor {
  public static final String GPT_MODEL = "gpt-3.5-turbo";
  private static final String TOKEN = "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";
  private static final String SYSTEM_TASK_MESSAGE = "You are an API Server that responds in a JSON format." +
      "Don't say anything else. Respond ONLY with JSON." +
      "The user will send you a transcription of a short video from TikTok. It can be any type of video. Note, that videos can be in different languages, not only English." +
      "Your goal is to create a title for that video based on provided text and make a short summary. In summary you should say about what this text was." +
      "Respond in JSON format. It should contain two elements: 'title' and 'summary', both are texts." +
      "Don't add anything else in the end of your respond after JSON";
  private final Logger logger = LoggerFactory.getLogger(OpenAIProcessorImpl.class);

  @Override
  public String summarize(String text) {
    OpenAiService service = new OpenAiService(TOKEN, Duration.ofSeconds(60));

    List<ChatMessage> messages = new ArrayList<>();
    messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), SYSTEM_TASK_MESSAGE));
    messages.add(new ChatMessage(ChatMessageRole.USER.value(), text));

    ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
        .model(GPT_MODEL)
        .temperature(0.8)
        .messages(messages)
        .build();

    StringBuilder stringBuilder = new StringBuilder();
    logger.info("Asking GPT...");
    service.createChatCompletion(chatCompletionRequest).getChoices()
        .forEach(choice -> stringBuilder.append(choice.getMessage().getContent()));
    logger.info("GPT responded.");
    return stringBuilder.toString();
  }
}
