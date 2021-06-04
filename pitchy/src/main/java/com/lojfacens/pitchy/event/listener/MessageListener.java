package com.lojfacens.pitchy.event.listener;

import com.lojfacens.pitchy.event.handler.MessageHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageListener extends ListenerAdapter {

  private final MessageHandler messageHandler;

  @Override
  public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
    messageHandler.handleMessage(event);
  }

}
