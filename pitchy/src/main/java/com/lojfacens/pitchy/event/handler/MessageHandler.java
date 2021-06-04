package com.lojfacens.pitchy.event.handler;

import com.lojfacens.pitchy.commands.Command;
import com.lojfacens.pitchy.commands.CommandContext;
import com.lojfacens.pitchy.commands.CommandHandler;
import com.lojfacens.pitchy.config.BotConfig;
import com.lojfacens.pitchy.service.audio.AudioManager;
import com.lojfacens.pitchy.service.main.BotManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageHandler {

  private final BotConfig botConfig;
  private final BotManager botManager;
  private final AudioManager audioManager;

  private final CommandHandler commandHandler;

  public void handleMessage(GuildMessageReceivedEvent event) {
    var isSelf = event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong();
    if (event.getAuthor().isBot() || event.isWebhookMessage() || isSelf) return;

    var prefix = botConfig.getPrefix();
    var message = event.getMessage().getContentRaw();
    var botId = event.getJDA().getSelfUser().getId();
    var mention =  "<@" + botId + ">";
    var mentionAlias = "<@!" + botId + ">";

    if (CommandHandler.isCommand(message, prefix, mention, mentionAlias)) {
      var cmd = message.substring(prefix.length()).split("\\s+", 2)[0];
      final Command command;

      if((command = commandHandler.getCommands().get(cmd.toLowerCase())) == null)
          return;

      var commandContext = CommandContext.builder()
          .botManager(botManager)
          .audioManager(audioManager)
          .event(event)
          .command(command)
          .content(event.getMessage().getContentRaw())
          .isMentionPrefix(false)
          .build();

      log.trace("Invoking handle on {} command", command.getClass().getSimpleName());
      commandHandler.handle(commandContext);
    }
  }

}
