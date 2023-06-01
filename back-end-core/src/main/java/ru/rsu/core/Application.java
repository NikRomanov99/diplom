package ru.rsu.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("ru.rsu.core")
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
