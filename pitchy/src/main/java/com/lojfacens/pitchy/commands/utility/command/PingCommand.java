package com.lojfacens.pitchy.commands.utility.command;

import java.time.Instant;

import com.lojfacens.pitchy.commands.Command;
import com.lojfacens.pitchy.commands.CommandContext;

import net.dv8tion.jda.api.EmbedBuilder;

public class PingCommand extends Command {

  public PingCommand() {
    super("ping");
  }

  @Override
  public void onCommand(CommandContext context) {
    var embed = new EmbedBuilder()
      .setTitle("Pong!")
      .setDescription("Como se calcula o ping msm?")
      .setTimestamp(Instant.now())
      .setFooter("Pitchy")
      .build();
    context.getMessage().reply(embed).queue(s -> {}, f -> context.getChannel().sendMessage(embed).queue());
  }

}
