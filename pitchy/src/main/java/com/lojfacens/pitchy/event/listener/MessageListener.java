package com.lojfacens.pitchy.event.listener;

import com.lojfacens.pitchy.event.handler.MessageHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageListener extends ListenerAdapter {

  private final MessageHandler messageHandler;

  @Override
  public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
    log.trace("Invoking handleMessage on onGuildMessageReceived");
    messageHandler.handleMessage(event);
  }

}
