package md.cernev.quickflick.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

//@RestController
public class HealthCheckController {

  @GetMapping("/ping")
  @ResponseBody
  public String ping() {
    return "pong";
  }
}
