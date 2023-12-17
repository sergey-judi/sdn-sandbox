package kpi.iasa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HandlerServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(HandlerServiceApplication.class, args);
  }

}
