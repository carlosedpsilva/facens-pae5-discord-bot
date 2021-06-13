package com.lojfacens.pitchy.service.audio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.lojfacens.pitchy.service.audio.AudioLoadHandler.Playback;
import com.lojfacens.pitchy.service.command.meta.CommandContext;
import com.lojfacens.pitchy.util.DisUtils;
import com.lojfacens.pitchy.util.StringUtils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class TrackScheduler extends PlayerEventListenerAdapter {

  private final AudioManager audioManager;
  private final Guild guild;
  private final LavalinkPlayer player;
  private final ConcurrentLinkedDeque<AudioTrack> queue;

  @Getter @Setter private AudioTrack previousTrack;
  @Getter @Setter private AudioTrack currentTrack;
  @Getter @Setter private TextChannel requestedChannel;
  @Getter @Setter private boolean looping;
  @Getter @Setter private boolean pausedManually;

  public TrackScheduler(AudioManager audioManager, Guild guild, LavalinkPlayer player) {
    this.audioManager = audioManager;
    this.guild = guild;
    this.player = player;
    this.queue = new ConcurrentLinkedDeque<>();
  }

  public boolean hasNextTrack() {
    return queue.peek() != null;
  }

  public void queue(AudioTrack track, Playback playback) {
    if (player.getPlayingTrack() != null) {
      switch (playback) {
        case PLAY -> {
          queue.offer(track);
          return;
        }
        case PLAYNEXT -> {
          queue.addFirst(track);
          return;
        }
        case PLAYNOW -> {
          queue.addFirst(track);
          nextTrack();
          return;
        }
      }
    }
    player.playTrack(track);
  }

  public void queue(AudioTrack track) {
    queue(track, Playback.PLAY);
  }

  public void nextTrack() {
    AudioTrack track = queue.poll();
    if (track == null) { // If nothing is on queue, stop
      if (player.getPlayingTrack() != null)
        player.stopTrack();
      return;
    }
    // Play next
    player.playTrack(track);
  }

  public void stop() {
    // Stop track
    if (player.getPlayingTrack() != null) player.stopTrack();

    // Reset scheduler
    queue.clear();
    currentTrack = null;
    previousTrack = null;
    requestedChannel = null;
    pausedManually = false;

    // Disconnect audio player
    audioManager.destroyGuildAudioManager(guild);
  }

  @Override
  public void onTrackStart(IPlayer player, AudioTrack track) {
    this.currentTrack = track;
    var context = (CommandContext) track.getUserData();
    var embedResponse = DisUtils.responseEmbedBuilder()
      .setDescription(String.format(
        "Now playing [%s](%s) (%s) [%s]",
        track.getInfo().title,
        track.getInfo().uri,
        StringUtils.getTimestamp(track.getInfo().length),
        context.getMember().getAsMention()
      ));
    context.send(embedResponse.build());
  }

  @Override
  public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
    this.previousTrack = track;
    this.currentTrack = null;
    if (endReason.mayStartNext) {
      if (looping)
        queue.add(track);
      nextTrack();
    }
  }

  @Override
  public void onPlayerPause(IPlayer player) {
      super.onPlayerPause(player);
  }

  @Override
  public void onPlayerResume(IPlayer player) {
      super.onPlayerResume(player);
  }

  @Override
  public void onTrackException(IPlayer player, AudioTrack track, Exception exception) {
      super.onTrackException(player, track, exception);
  }

  @Override
  public void onTrackStuck(IPlayer player, AudioTrack track, long thresholdMs) {
    Queue<AudioTrack> temp = new LinkedList<>();
    temp.add(track.makeClone());
    temp.addAll(queue);
    audioManager.resetStuckGuildAudioManager(guild, temp);
  }

  public void shuffle() {
    var tempQueue = new ArrayList<>(queue);
    Collections.shuffle(tempQueue);
    queue.clear();
    queue.addAll(tempQueue);
  }

  public Guild getGuild() {
    return guild;
  }

  public Queue<AudioTrack> getQueue() {
    return queue;
  }

}
