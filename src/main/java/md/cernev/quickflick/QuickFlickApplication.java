package md.cernev.quickflick;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.web.reactive.config.EnableWebFlux;

//todo: definetly an issue
@SpringBootApplication(exclude = WebMvcAutoConfiguration.class)
@EnableWebFlux
public class QuickFlickApplication {

  public static void main(String[] args) {
    SpringApplication.run(QuickFlickApplication.class, args);
  }

}
