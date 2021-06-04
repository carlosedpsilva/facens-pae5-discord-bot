package com.lojfacens.pitchy.event.handler;

import com.lojfacens.pitchy.service.command.CommandProcessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageHandler {

  private final CommandProcessor commandProcessor;

  public void handleMessage(GuildMessageReceivedEvent event) {
    var isSelf = event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong();
    if (event.getAuthor().isBot() || event.isWebhookMessage() || isSelf) return;

    log.trace("Invoking CommandHandler#handleMessage");
    commandProcessor.process(event);
  }

}
