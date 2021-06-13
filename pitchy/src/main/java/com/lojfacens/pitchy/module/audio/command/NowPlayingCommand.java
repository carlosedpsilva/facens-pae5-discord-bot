package com.lojfacens.pitchy.module.audio.command;

import static com.lojfacens.pitchy.util.DisUtils.TITLE_EXCEPTION;
import static com.lojfacens.pitchy.util.DisUtils.responseEmbedBuilder;

import com.lojfacens.pitchy.service.command.meta.Command;
import com.lojfacens.pitchy.service.command.meta.CommandContext;
import com.lojfacens.pitchy.util.StringUtils;

public class NowPlayingCommand extends Command {

  public NowPlayingCommand() {
    super("nowplaying");
  }

  @Override
  public void onCommand(CommandContext context) {
    var manager = context.getAudioManager().getGuildAudioManager(context.getGuild());
    var track = manager.getPlayer().getPlayingTrack();

    var embedResponse = responseEmbedBuilder();

    if (track == null) {
      embedResponse.setTitle(TITLE_EXCEPTION).setDescription("There is no track playing");
      context.reply(embedResponse.build());
      return;
    }

    var trackContext = (CommandContext) track.getUserData();
    embedResponse.setTitle(track.getInfo().title, track.getInfo().uri)
        .addField("Duration", String.format(
            "%s/%s",
            StringUtils.getTimestamp(manager.getPlayer().getTrackPosition()),
            StringUtils.getTimestamp(track.getDuration())), true)
        .addField("Author", track.getInfo().author, true)
        .addField("Requested by", trackContext.getMember().getAsMention(), true);
    context.reply(embedResponse.build());

  }

}
