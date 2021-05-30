package com.lojfacens.pitchy;

import com.lojfacens.pitchy.config.BotConfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BotConfig.class)
public class PitchyApplication {

  public static void main(String[] args) {
    SpringApplication.run(PitchyApplication.class, args);
  }

}
