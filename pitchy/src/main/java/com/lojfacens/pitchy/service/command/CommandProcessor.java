package com.lojfacens.pitchy.service.command;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@Slf4j
@Component
public class CommandProcessor {

  public void process(GuildMessageReceivedEvent event) {
    log.trace("Invoking command");
    if (event.getMessage().getContentRaw().startsWith("Hi"))
      event.getMessage().reply("Hello").queue();
  }

}
