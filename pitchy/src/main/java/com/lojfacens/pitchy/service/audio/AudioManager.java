package com.lojfacens.pitchy.service.audio;

import java.util.HashMap;
import java.util.Queue;

import javax.annotation.PostConstruct;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lavalink.client.io.Link;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AudioManager {

  private final LavalinkManager lavalinkManager;

  private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
  private final HashMap<Guild, GuildAudioManager> guildAudioManagers = new HashMap<>();

  @PostConstruct
  public void registerSourceManagers() {
    playerManager.registerSourceManager(new YoutubeAudioSourceManager(true));
    playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
    playerManager.registerSourceManager(new BandcampAudioSourceManager());
    playerManager.registerSourceManager(new VimeoAudioSourceManager());
    playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
    playerManager.registerSourceManager(new BeamAudioSourceManager());
    log.info("Finished loading Audio Manager");
  }

  public GuildAudioManager getGuildAudioManager(Guild guild) {
    var manager = guildAudioManagers.get(guild);
    if (manager == null) {
      synchronized (getGuildAudioManagers()) {
        manager = new GuildAudioManager(this, guild, lavalinkManager);
        addGuildAudioManager(guild, manager);
      }
    }
    return manager;
  }

  public void resetStuckGuildAudioManager(Guild guild, Queue<AudioTrack> queue) {
    destroyGuildAudioManager(guild);
    getGuildAudioManager(guild).getScheduler().getQueue().addAll(queue);
  }

  public void addGuildAudioManager(Guild guild, GuildAudioManager manager) {
    guildAudioManagers.put(guild, manager);
  }

  public void destroyGuildAudioManager(Guild guild) {
    if (guildAudioManagers.containsKey(guild)) {
      guildAudioManagers.get(guild).destroyConnection();
      guildAudioManagers.remove(guild);
    }
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
