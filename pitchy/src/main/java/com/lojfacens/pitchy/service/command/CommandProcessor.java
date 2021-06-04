package com.lojfacens.pitchy.service.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.lojfacens.pitchy.service.command.meta.Command;
import com.lojfacens.pitchy.service.command.meta.CommandContext;
import com.lojfacens.pitchy.service.command.meta.CommandModule;

import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

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
    context.getCommand().onCommand(context);
  }

  public Map<String, Command> getCommands() {
    return commands;
  }

  public Map<String, CommandModule> getModules() {
    return commandModules;
  }

}
