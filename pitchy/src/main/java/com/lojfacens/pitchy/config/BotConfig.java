package com.lojfacens.pitchy.config;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "bot.config")
public class BotConfig {

  @NotNull(message = "Bot name may not be null")
  @Size(min = 2, max = 32, message = "Bot name must be between 2 and 32 characters long")
  private String botName;

  private String clientId;

  @NotNull(message = "Bot token may not be null")
  @Pattern(regexp = "^[A-Za-z\\d]{24}\\.[\\w-]{6}\\.[\\w-]{27}$", message = "The provided token is not valid")
  private String token;

  private String[] prefixes;

}
