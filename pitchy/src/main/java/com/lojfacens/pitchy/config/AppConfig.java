package com.lojfacens.pitchy.config;

import com.lojfacens.pitchy.service.audio.AudioManager;
import com.lojfacens.pitchy.service.audio.GuildAudioManager;
import com.lojfacens.pitchy.service.audio.lavalink.LavalinkManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import net.dv8tion.jda.api.entities.Guild;

@Configuration
public class AppConfig {

  @Bean
  @Scope("prototype")
  public GuildAudioManager guildAudioManager(AudioManager audioManager, Guild guild,
      @Autowired LavalinkManager lavaLinkManager) {
    return new GuildAudioManager(audioManager, guild, lavaLinkManager);
  }

}
