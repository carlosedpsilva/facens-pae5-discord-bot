package com.lojfacens.pitchy.module.audio.command;

import static com.lojfacens.pitchy.util.DisUtils.TITLE_EXCEPTION;
import static com.lojfacens.pitchy.util.DisUtils.responseEmbedBuilder;

import com.lojfacens.pitchy.service.command.meta.Command;
import com.lojfacens.pitchy.service.command.meta.CommandContext;

public class LoopCommand extends Command {

  public LoopCommand() {
    super("loop");
  }

  @Override
  public void onCommand(CommandContext context) {
    var manager = context.getAudioManager().getGuildAudioManager(context.getGuild());

    var embedResponse = responseEmbedBuilder();

    if (manager.getPlayer().getPlayingTrack() == null) {
      embedResponse.setTitle(TITLE_EXCEPTION).setDescription("There is no track playing");
      context.reply(embedResponse.build());
      return;
    }
    var looping = manager.getScheduler().isLooping();
    manager.getScheduler().setLooping(!looping);

    embedResponse.setDescription(looping ? "Stopped looping queue" : "Now looping queue");
    context.reply(embedResponse.build());
  }

}
