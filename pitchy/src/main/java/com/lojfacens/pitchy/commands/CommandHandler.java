package com.lojfacens.pitchy.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import com.lojfacens.pitchy.config.BotConfig;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommandHandler {

  private final Map<String, CommandModule> commandModules = new ConcurrentHashMap<>();
  private final Map<String, Command>       commands       = new ConcurrentHashMap<>();

  public static boolean isCommand(String message, String prefix, String mention, String mentionAlias) {
    return message.startsWith(prefix) || message.startsWith(mention) || message.startsWith(mentionAlias);
  }

  @PostConstruct
  private void setupCommands() {
    for(Class<? extends CommandModule> obj
        : new Reflections("com.lojfacens.pitchy.commands")
        .getSubTypesOf(CommandModule.class)) {
      if(!Modifier.isAbstract(obj.getModifiers())) {
        CommandModule module;
        try {
          module = obj.getConstructor().newInstance();
          commandModules.put(module.getName(), module);
          commands.putAll(module.getCommands());
          log.info("Loaded all commands from module {}", module.getName());
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
          log.error("There was a problem reflecting modules/commands", e);
        }
      }
    }
    log.info("Loaded {} modules, containing {} commands", commandModules.size(), commands.size());
  }

  public void handle(CommandContext context) {
    if (context == null) return;

    var command = context.getCommand();
    if (command == null
      || !isEnabled(command, context)
      || !isValidAudio(command, context)
      || !hastPermission(command, context))
      return;

    log.trace("Invoking {}#onCommand", command.getClass().getSimpleName());
    command.onCommand(context);
  }

  private boolean isEnabled(Command command, CommandContext context) {
    if (!command.isEnabled()) {
      var embed = new EmbedBuilder()
          .setTitle("Ooops")
          .setDescription("This command is disabled.")
          .build();
      context.getMessage().reply(embed).queue(s -> {}, f -> context.getChannel().sendMessage(embed).queue());
      return false;
    }
    return true;
  }

  private boolean isValidAudio(Command command, CommandContext context) {
    if (!command.getModule().getName().equals("audio"))
      return true;

    if (!context.getMember().getVoiceState().inVoiceChannel()) {
      var embed = new EmbedBuilder()
          .setTitle("You are not connected to a voice channel")
          .build();
      context.getMessage().reply(embed).queue(s -> {}, f -> context.getChannel().sendMessage(embed).queue());
      return false;
    }

    if (context.getAudioManager().hasLink(context.getGuild())
        && !context.getAudioManager().isLinkConnected(context.getGuild())) {
      var embed = new EmbedBuilder()
          .setTitle("No audio connection")
          .build();
      context.getMessage().reply(embed).queue(s -> {}, f -> context.getChannel().sendMessage(embed).queue());
      return false;
    }

    if (context.getAudioManager().getLavalinkManager().getLavalink().getNodes().isEmpty()) {
      var embed = new EmbedBuilder()
          .setTitle("No lavalink")
          .build();
      context.getMessage().reply(embed).queue(s -> {}, f -> context.getChannel().sendMessage(embed).queue());
      return false;
    }

    return true;
  }

  private boolean hastPermission(Command command, CommandContext context) {
    if (context.getCommand().getPermissions() == null)
      return true;

    var bot = context.getSelfMember();
    var commander = context.getMember();
    var textChannel = context.getChannel();
    var voiceChannel = commander.getVoiceState().getChannel();

    for (GuildChannel channel : Stream.of(textChannel, voiceChannel).filter(Objects::nonNull).collect(Collectors.toList())) {
      for (Member member : Stream.of(bot, commander).filter(Objects::nonNull).collect(Collectors.toList())) {
        if (member.hasPermission(command.getPermissions()) && !member.hasPermission(channel, command.getPermissions())
            || !member.hasPermission(command.getPermissions()) && !member.hasPermission(channel, command.getPermissions())) {
          var embed = new EmbedBuilder()
              .setTitle("No lavalink")
              .build();
          context.getMessage().reply(embed).queue(s -> {}, f -> context.getChannel().sendMessage(embed).queue());
          return false;
        }
      }
    }
    return true;
  }

  public Map<String, Command> getCommands() {
    return commands;
  }

  public Map<String, CommandModule> getModules() {
    return commandModules;
  }

}
