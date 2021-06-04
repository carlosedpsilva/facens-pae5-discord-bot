package com.lojfacens.pitchy.service.command.meta;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.reflections.Reflections;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class CommandModule {

  private final String name;
  private final Map<String, Command> commands = new HashMap<>();

  protected CommandModule(String name) {
    this.name = name;
    new Reflections("com.lojfacens.pitchy.module." + name)
        .getSubTypesOf(Command.class)
        .forEach(clazz -> {
          try {
            Command command = clazz.getConstructor().newInstance();
            command.setModule(this);
            commands.putIfAbsent(command.getName(), command);
          } catch (InstantiationException | IllegalAccessException |
              InvocationTargetException | NoSuchMethodException e) {
            log.error("Something went wrong during the reflection process", e);
          }
        });
  }

}
