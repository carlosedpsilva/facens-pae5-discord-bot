package com.lojfacens.pitchy.service.command;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lojfacens.pitchy.service.command.meta.Command;
import com.lojfacens.pitchy.service.command.meta.CommandContext;
import com.lojfacens.pitchy.service.command.meta.CommandModule;

import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;

@Slf4j
@Service
public class CommandProcessor {

  private final Map<String, CommandModule> commandModules = new ConcurrentHashMap<>();
  private final Map<String, Command>       commands       = new ConcurrentHashMap<>();

  public void setupCommands() {
    for(Class<? extends CommandModule> obj
        : new Reflections("com.lojfacens.pitchy.module")
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

  public void process(CommandContext context) {
    if (context == null) return;
    var command = context.getCommand();
    if (command == null
        || !isEnabled(command, context)
        || !hasPermission(command, context))
      return;
    command.onCommand(context);
  }

  private boolean isEnabled(Command command, CommandContext context) {
    if (!command.isEnabled()) {
      var embed = new EmbedBuilder()
          .setColor(Color.decode("#e35f8d"))
          .setTitle("❌ Oops")
          .setDescription("This command is disabled.")
          .setFooter("🍑 Pitchy")
          .build();
      context.getMessage().reply(embed).queue(s -> {}, f -> context.getChannel().sendMessage(embed).queue());
      return false;
    }
    return true;
  }

  private boolean hasPermission(Command command, CommandContext context) {
    if (context.getCommand().getPermissions() == null)
      return true;

    var bot = context.getSelfMember();
    var commander = context.getMember();
    var textChannel = context.getChannel();
    var voiceChannel = commander.getVoiceState().getChannel();

    for (GuildChannel channel : Stream.of(textChannel, voiceChannel).filter(Objects::nonNull).collect(Collectors.toList())) {
      for (Member member : Stream.of(commander, bot).filter(Objects::nonNull).collect(Collectors.toList())) {
        if (member.hasPermission(command.getPermissions()) && !member.hasPermission(channel, command.getPermissions())
            || !member.hasPermission(command.getPermissions()) && !member.hasPermission(channel, command.getPermissions())) {
          var memberMention = member.getUser().getAsMention() + (member.equals(commander) ? ", you lack" : " lacks");
          var embed = new EmbedBuilder()
              .setColor(Color.decode("#e35f8d"))
              .setTitle("❌ Oops")
              .setDescription(memberMention + " the following permissions to use this command:\n\n"
                  + command.getPermissions().stream().map(p -> "`" + p.getName() + "`").collect(Collectors.joining(", ")))
              .setFooter("🍑 Pitchy")
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
