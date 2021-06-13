package com.lojfacens.pitchy.module.audio.command;

import static com.lojfacens.pitchy.util.DisUtils.TITLE_EXCEPTION;
import static com.lojfacens.pitchy.util.DisUtils.responseEmbedBuilder;

import com.lojfacens.pitchy.service.command.meta.Command;
import com.lojfacens.pitchy.service.command.meta.CommandContext;

public class ClearCommand extends Command {

  public ClearCommand() {
    super("clear");
  }

  @Override
  public void onCommand(CommandContext context) {
    var manager = context.getAudioManager().getGuildAudioManager(context.getGuild());
    var embedResponse = responseEmbedBuilder();

    if (manager.getScheduler().getQueue().isEmpty()) {
      embedResponse.setTitle(TITLE_EXCEPTION).setDescription("There is no track queued");
      context.reply(embedResponse.build());
    }

    manager.getScheduler().getQueue().clear();
    embedResponse.setDescription("Cleared queue");
    context.reply(embedResponse.build());

  }

}
