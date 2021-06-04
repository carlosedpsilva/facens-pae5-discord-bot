package com.lojfacens.pitchy.event.handler;

import com.lojfacens.pitchy.config.BotConfig;
import com.lojfacens.pitchy.service.command.CommandProcessor;
import com.lojfacens.pitchy.service.command.meta.Command;
import com.lojfacens.pitchy.service.command.meta.CommandContext;
import com.lojfacens.pitchy.service.main.BotManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageHandler {

  private final BotConfig botConfig;
  private final BotManager botManager;
  private final CommandProcessor commandProcessor;

  public void handleMessage(GuildMessageReceivedEvent event) {
    var isSelf = event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong();
    if (event.getAuthor().isBot() || event.isWebhookMessage() || isSelf) return;

    var prefixes = botConfig.getPrefixes();
    var rawMessage = event.getMessage().getContentRaw();
    var lowerRawMessage = rawMessage.toLowerCase();

    String[] mentionPrefixes = {
      String.format("<@%s> ", botConfig.getClientId()),
      String.format("<@!%s> ", botConfig.getClientId())
    };

    // Check for mention prefixes
    var isMention = false;
    String usedPrefix = null;
    for (String mention : mentionPrefixes)
      if (lowerRawMessage.startsWith(mention)) {
        usedPrefix = mention;
        isMention = true;
      }

    // Check for default prefixes
    if (usedPrefix == null)
      for (String prefix : prefixes)
        if (lowerRawMessage.startsWith(prefix))
          usedPrefix = prefix;

    if (usedPrefix != null) {
      rawMessage = rawMessage.substring(usedPrefix.length()); // Remove prefix from raw message

      final CommandContext commandContext;
      if ((commandContext = buildCommandContext(event, rawMessage, isMention)) != null) {
        commandProcessor.process(commandContext);
      }
    }
  }

  private CommandContext buildCommandContext(GuildMessageReceivedEvent event, String rawMessage, boolean isMention) {
    String[] args = rawMessage.split("\\s+", 2);
    var commandName = args[0];
    var content = args.length > 1 ? args[1] : null;


    final Command command;
    if ((command = commandProcessor.getCommands().get(commandName.toLowerCase())) == null)
        return null;

    return CommandContext.builder()
        .botManager(botManager)
        .event(event)
        .command(command)
        .content(content)
        .isMentionPrefix(isMention)
        .build();
  }

}
