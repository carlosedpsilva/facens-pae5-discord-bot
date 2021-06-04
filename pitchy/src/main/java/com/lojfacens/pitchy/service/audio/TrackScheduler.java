package com.lojfacens.pitchy.service.audio;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;

import com.lojfacens.pitchy.scheduler.ScheduleHandler;
import com.lojfacens.pitchy.scheduler.jobs.VoiceTimeoutJob;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;

@Slf4j
public class TrackScheduler extends PlayerEventListenerAdapter {

  private final AudioManager audioManager;
  private final Guild guild;
  private AudioTrack background = null;
  private AudioTrack lastTrack  = null;
  private boolean looping = false;
  private final LavalinkPlayer player;
  private final Queue<AudioTrack> queue;
  private ScheduledFuture<?> timeout;

  public TrackScheduler(AudioManager audioManager, Guild guild, LavalinkPlayer player) {
    this.audioManager = audioManager;
    this.guild = guild;
    this.player = player;
    this.queue = new LinkedList<>();
  }

  public boolean hasNextTrack() {
    return queue.peek() != null;
  }

  public void queue(AudioTrack track) {
    if (player.getPlayingTrack() == null || (background != null && player.getPlayingTrack() == background)) {
      queue.add(track);
      nextTrack();
      return;
    }
    queue.offer(track);
  }

  private void nextTrack() {
    try {
      AudioTrack track = queue.poll();
      if (track == null) {
        if (background != null) {
          background = background.makeClone();
          player.playTrack(background);
          return;
        }
        timeout = ScheduleHandler.registerUniqueJob(new VoiceTimeoutJob(guild));
        // If skip is used and nothing else is in the queue, stop
        if (player.getPlayingTrack() != null) {
          player.stopTrack();
        }
        return;
      }
      player.playTrack(track);
    } catch (Exception e) {
      log.warn("TrackScheduler issue detected", e);
    }
  }

  @Override
  public void onTrackStart(IPlayer player, AudioTrack track) {
    try {
      // Cancel timeout onStart if it is currently set
      if (timeout != null) {
        timeout.cancel(true);
        timeout = null;
      }
      // TODO: Finalize (create messagEvent entity)
    } catch (Exception e) {
      log.debug(e.getMessage());
    }
  }

  @Override
  public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
    this.lastTrack = track;
    if (endReason.mayStartNext) {
      if (looping)
        queue.add(track);
      nextTrack();
    }
  }

  @Override
  public void onTrackStuck(IPlayer player, AudioTrack track, long thresholdMs) {
    Queue<AudioTrack> temp = new LinkedList<>();
    temp.add(track.makeClone());
    temp.addAll(queue);
    audioManager.resetStuckGuildAudioManager(guild, temp);
  }

  public void shuffle() {
    Collections.shuffle((List<?>) queue);
  }

  public AudioTrack getLastTrack() {
    return lastTrack;
  }

  public boolean isLooping() {
    return looping;
  }

  public void setBackground(AudioTrack track) {
    background = track;
  }

  public void setLooping(boolean looping) {
    this.looping = looping;
  }

  public Queue<AudioTrack> getQueue() {
    return queue;
  }

}
