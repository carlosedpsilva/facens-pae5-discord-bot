package com.lojfacens.pitchy.service.audio;

import java.util.HashMap;
import java.util.Queue;

import javax.annotation.PostConstruct;

import com.lojfacens.pitchy.service.audio.lavalink.LavalinkManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lavalink.client.io.Link;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AudioManager {

  private final LavalinkManager lavalinkManager;
  private final BeanFactory beanFactory;

  private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
  private final HashMap<Guild, GuildAudioManager> guildManagers = new HashMap<>();

  @PostConstruct
  public void registerSourcemanagers() {
    playerManager.registerSourceManager(new YoutubeAudioSourceManager(true));
    playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
    log.info("Finished loading Audio Manager");
  }

  public GuildAudioManager getGuildAudioManager(Guild guild) {
    var manager = guildManagers.get(guild);
    if (manager == null) {
      synchronized (getGuildAudioManagers()) {
        manager = beanFactory.getBean(GuildAudioManager.class, this, guild);
        addGuildAudioManager(guild, manager);
      }
    }
    return manager;
  }

  private HashMap<Guild, GuildAudioManager> getGuildAudioManagers() {
    return guildManagers;
  }

  private void addGuildAudioManager(Guild guild, GuildAudioManager manager) {
    guildManagers.put(guild, manager);
  }

  public void resetStuckGuildAudioManager(Guild guild, Queue<AudioTrack> queue) {
    destroyGuildAudioManager(guild);
    getGuildAudioManager(guild).getScheduler().getQueue().addAll(queue);
  }

  private void destroyGuildAudioManager(Guild guild) {
    if (guildManagers.containsKey(guild)) {
      guildManagers.get(guild).destroyConnection();
      guildManagers.remove(guild);
    }
  }

  public LavalinkManager getLavalinkManager() {
    return lavalinkManager;
  }

  public AudioPlayerManager getPlayerManager() {
    return playerManager;
  }

  public boolean hasLink(Guild guild) {
    return lavalinkManager.getLavalink().getExistingLink(guild) != null;
  }

  public Link getExistingLink(Guild guild) {
    return lavalinkManager.getLavalink().getExistingLink(guild);
  }

  public boolean isLinkConnected(Guild guild) {
    return getExistingLink(guild).getState() == Link.State.CONNECTED;
  }

}
