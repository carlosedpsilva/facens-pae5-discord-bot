package com.lojfacens.pitchy.module.audio.command;

import static com.lojfacens.pitchy.util.DisUtils.TITLE_EXCEPTION;
import static com.lojfacens.pitchy.util.DisUtils.responseEmbedBuilder;

import com.lojfacens.pitchy.service.command.meta.Command;
import com.lojfacens.pitchy.service.command.meta.CommandContext;

public class PauseCommand extends Command {

  public PauseCommand() {
    super("pause");
  }

  @Override
  public void onCommand(CommandContext context) {
    var manager = context.getAudioManager().getGuildAudioManager(context.getGuild());

    var embedResponse = responseEmbedBuilder();

    if (manager.getPlayer().isPaused()) {
      embedResponse.setTitle(TITLE_EXCEPTION).setDescription("Already paused");
      context.reply(embedResponse.build());
      return;
    }

    manager.getPlayer().setPaused(true);
    manager.getScheduler().setPausedManually(true);

    embedResponse.setDescription(String.format("Paused by %s", context.getMember().getAsMention()));
    context.reply(embedResponse.build());
  }

}
