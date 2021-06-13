package com.lojfacens.pitchy.module.utility.command;

import static com.lojfacens.pitchy.util.DisUtils.responseEmbedBuilder;

import java.util.concurrent.atomic.AtomicInteger;

import com.lojfacens.pitchy.service.command.meta.Command;
import com.lojfacens.pitchy.service.command.meta.CommandContext;

public class PingCommand extends Command {

  public PingCommand() {
    super("ping");
  }

  @Override
  public void onCommand(CommandContext context) {
    var gatewayPing = new AtomicInteger();
    var restPing = new AtomicInteger();

    gatewayPing.set((int) context.getJDA().getGatewayPing());
    context.getJDA().getRestPing().queue(s -> restPing.set(s.intValue()), f -> restPing.set(-1));

    var embedResponse = responseEmbedBuilder()
      .setTitle("🏓 Pong!")
      .setDescription(String.format(
          "**Gateway Ping**: %d ms%n**Rest Ping**: %d ms", gatewayPing.intValue(), restPing.intValue()));
    context.reply(embedResponse.build());
  }

}
