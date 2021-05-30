package com.lojfacens.pitchy.service;

import javax.annotation.PostConstruct;

import com.lojfacens.pitchy.config.BotConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BotManager {

  private BotConfig config;

  @PostConstruct
  private void init() {
    log.info("Starting up {}", config.getBotName());
  }

}
