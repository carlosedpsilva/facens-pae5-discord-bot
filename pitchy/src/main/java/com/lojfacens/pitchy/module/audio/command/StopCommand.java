package com.lojfacens.pitchy.module.audio.command;

import static com.lojfacens.pitchy.util.DisUtils.responseEmbedBuilder;

import com.lojfacens.pitchy.service.command.meta.Command;
import com.lojfacens.pitchy.service.command.meta.CommandContext;

public class StopCommand extends Command {

  public StopCommand() {
    super("stop");
  }

  @Override
  public void onCommand(CommandContext context) {
    context.getAudioManager().getGuildAudioManager(context.getGuild()).getScheduler().stop();
    var embedResponse = responseEmbedBuilder().setDescription("Stopped");
    context.reply(embedResponse.build());
  }

}
