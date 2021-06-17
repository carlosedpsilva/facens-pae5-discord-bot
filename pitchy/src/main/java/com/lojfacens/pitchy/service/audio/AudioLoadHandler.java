package com.lojfacens.pitchy.service.audio;

import static com.lojfacens.pitchy.util.DisUtils.TITLE_EXCEPTION;
import static com.lojfacens.pitchy.util.DisUtils.responseEmbedBuilder;

import java.util.ArrayList;

import com.lojfacens.pitchy.service.command.meta.CommandContext;
import com.lojfacens.pitchy.util.StringUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AudioLoadHandler {

  private AudioLoadHandler() { }

  public enum Playback {
    PLAY, PLAYNEXT, PLAYNOW
  }

  public static void loadAndPlay(GuildAudioManager manager, CommandContext context, String trackUrl, Playback type) {
    context.getAudioManager().getPlayerManager().loadItemOrdered(manager, trackUrl, new AudioLoadResultHandler() {

      @Override
      public void trackLoaded(AudioTrack track) {
        try {
          track.setUserData(context);
          var embedResponse = responseEmbedBuilder()
              .setDescription(String.format(
                  "Queued track [%s](%s) (%s) [%s]",
                  track.getInfo().title,
                  track.getInfo().uri,
                  StringUtils.getTimestamp(track.getInfo().length),
                  context.getMember().getAsMention()
              ));
          context.reply(embedResponse.build());
          switch (type) {
            case PLAY       -> manager.getScheduler().queue(track);
            case PLAYNEXT   -> manager.getScheduler().queue(track, Playback.PLAYNEXT);
            case PLAYNOW    -> manager.getScheduler().queue(track, Playback.PLAYNOW);
          }
        } catch (Exception e) {
          log.error("An error ocurred while running the {} class, message:", this, e.getMessage(), e);
        }
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        var embedResponse = responseEmbedBuilder()
            .setDescription(String.format("Queued playlist [%s](%s) [%s].",
            playlist.getName(),
            trackUrl,
        context.getMember().getAsMention()));
        context.reply(embedResponse.build());
        switch (type) {
          case PLAY -> {
            var tracks = playlist.getTracks();
            for (var track : tracks) {
              track.setUserData(context);
              manager.getScheduler().queue(track);
            }
          }
          case PLAYNEXT -> {
            var tempQueue = new ArrayList<>(manager.getScheduler().getQueue());
            manager.getScheduler().getQueue().clear();
            var tracks = playlist.getTracks();
            for (var track : tracks) {
              track.setUserData(context);
              manager.getScheduler().queue(track);
            }
            manager.getScheduler().getQueue().addAll(tempQueue);
          }
          case PLAYNOW -> {
            var tempQueue = new ArrayList<>(manager.getScheduler().getQueue());
            manager.getScheduler().getQueue().clear();
            var tracks = playlist.getTracks();
            for (var track : tracks) {
              track.setUserData(context);
              manager.getScheduler().queue(track);
            }
            manager.getScheduler().getQueue().addAll(tempQueue);
            manager.getScheduler().nextTrack();
          }
        }
      }

      @Override
      public void noMatches() {
        var embedResponse = responseEmbedBuilder()
            .setTitle(TITLE_EXCEPTION)
            .setDescription("No matches");
        context.reply(embedResponse.build());
      }

      @Override
      public void loadFailed(FriendlyException e) {
        var embedResponse = responseEmbedBuilder()
            .setTitle(TITLE_EXCEPTION)
            .setDescription(String.format("Failed to load track, message: %s", e.getMessage()));
        context.reply(embedResponse.build());
      }

    });
  }

}
