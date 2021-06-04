package com.lojfacens.pitchy.service.audio.lavalink;

import com.lojfacens.pitchy.config.BotConfig;
import com.lojfacens.pitchy.service.main.BotManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lavalink.client.io.jda.JdaLavalink;
import lombok.Getter;

@Getter
@Component
public class LavalinkManager {

  private final JdaLavalink lavalink;

  @Autowired
  public LavalinkManager(BotConfig botConfig, BotManager botManager) {
    lavalink = new JdaLavalink(
      botConfig.getBotId(),
      botManager.getShardsTotal(),
      id -> botManager.getShardManager().getShardById(id)
    );
    lavalink.setAutoReconnect(true);
  }

}
