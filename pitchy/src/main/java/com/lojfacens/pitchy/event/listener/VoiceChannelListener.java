package com.lojfacens.pitchy.event.listener;

import static com.lojfacens.pitchy.util.DisUtils.responseEmbedBuilder;

import com.lojfacens.pitchy.service.audio.AudioManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class VoiceChannelListener extends ListenerAdapter {

  private final AudioManager audioManager;

  @Override
  public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
    var guildAudioManager = audioManager.getGuildAudioManager(event.getGuild());
    if (guildAudioManager == null) return;

    if (event.getMember().getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong())
      guildAudioManager.destroyConnection();

    var vs = event.getVoiceState();
    if (validate(vs)) return;

    if (isAlone(event.getChannelLeft())) {
      guildAudioManager.destroyConnection();
    }

  }

  @Override
  public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {
    if (event.getMember().getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong())
      return;

    var vs = event.getVoiceState();
    if (validate(vs)) return;

    var guildAudioManager = audioManager.getGuildAudioManager(event.getGuild());
    if (guildAudioManager == null) return;

    var scheduler = guildAudioManager.getScheduler();
    var player = guildAudioManager.getPlayer();
    if (event.getVoiceState().isMuted()) {
      if (scheduler.getCurrentTrack() != null && scheduler.getRequestedChannel() != null) {
        var embedResponse = responseEmbedBuilder().setDescription("Paused because I was muted");
        scheduler.getRequestedChannel().sendMessage(embedResponse.build()).queue();
        player.setPaused(true);
      }
    } else {
      if (vs.getChannel() == null) return;
      if (!isAlone(vs.getChannel()) && scheduler.getCurrentTrack() != null && !scheduler.isPausedManually()) {
        var embedResponse = responseEmbedBuilder().setDescription("Resumed because I was unmuted");
        scheduler.getRequestedChannel().sendMessage(embedResponse.build()).queue();
        player.setPaused(false);
      }
    }

  }

  private static boolean validate(GuildVoiceState state) {
    return state == null || !state.inVoiceChannel();
  }

  private static boolean isAlone(VoiceChannel vc) {
    return vc.getMembers().stream().allMatch(m -> m.getUser().isBot());
  }

}
