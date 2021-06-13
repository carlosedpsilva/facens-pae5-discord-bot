package com.lojfacens.pitchy.module.audio.command;

import static com.lojfacens.pitchy.util.DisUtils.TITLE_EXCEPTION;
import static com.lojfacens.pitchy.util.DisUtils.responseEmbedBuilder;

import com.lojfacens.pitchy.service.command.meta.Command;
import com.lojfacens.pitchy.service.command.meta.CommandContext;
import com.lojfacens.pitchy.util.StringUtils;

public class SkipCommand extends Command {

  public SkipCommand() {
    super("skip");
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

    var track = manager.getPlayer().getPlayingTrack();
    var trackContext = (CommandContext) track.getUserData();
    embedResponse.setDescription(String.format(
      "Skipped [%s](%s) (%s) [%s]",
      track.getInfo().title,
      track.getInfo().uri,
      StringUtils.getTimestamp(track.getInfo().length),
      trackContext.getMember().getAsMention()
    ));
    context.reply(embedResponse.build());
    manager.getScheduler().nextTrack();
  }

}
