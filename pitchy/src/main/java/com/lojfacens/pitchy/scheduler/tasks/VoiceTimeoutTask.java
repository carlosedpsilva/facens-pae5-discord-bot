package com.lojfacens.pitchy.scheduler.tasks;

import com.lojfacens.pitchy.scheduler.Task;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class VoiceTimeoutTask implements Task {

  private final Guild guild;
  private final GuildVoiceState voiceState;

  public VoiceTimeoutTask(Guild guild) {
    this.guild = guild;
    this.voiceState = guild.getSelfMember().getVoiceState();
  }

  @Override
  public void run() {
    if (voiceState != null && voiceState.inVoiceChannel()) {
      // TODO: new StopCommand().onCommand(guild);
    }
  }

}
