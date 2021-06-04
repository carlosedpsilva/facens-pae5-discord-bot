package com.lojfacens.pitchy.module.utility.command;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;

import com.lojfacens.pitchy.service.command.meta.Command;
import com.lojfacens.pitchy.service.command.meta.CommandContext;

import net.dv8tion.jda.api.EmbedBuilder;

public class PingCommand extends Command {

  public final AtomicInteger GATEWAY_PING = new AtomicInteger();
  public final AtomicInteger REST_PING = new AtomicInteger();

  public PingCommand() {
    super("ping");
  }

  @Override
  public void onCommand(CommandContext context) {
    GATEWAY_PING.set((int) context.getJDA().getGatewayPing());
    context.getJDA().getRestPing().queue(s -> REST_PING.set(s.intValue()), f -> REST_PING.set(-1));

    var embed = new EmbedBuilder()
      .setColor(Color.decode("#e35f8d"))
      .setTitle("🏓 Pong!")
      .setDescription(String.format("**Gateway Ping**: %d ms%n**Rest Ping**: %d ms", GATEWAY_PING.intValue(), REST_PING.intValue()))
      .setFooter("🍑 Pitchy")
      .build();
    context.getMessage().reply(embed).queue(s -> {}, f -> context.getChannel().sendMessage(embed).queue());
  }

}
