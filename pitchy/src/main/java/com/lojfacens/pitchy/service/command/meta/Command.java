package com.lojfacens.pitchy.service.command.meta;

import java.util.HashMap;
import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;

@Data
@RequiredArgsConstructor
public abstract class Command {

  private CommandModule module;
  private boolean enabled = true;

  private final String name;
  private final List<String> usage;
  private final List<Permission> permissions;
  private final int parameters;
  private final long cooldownDuration;
  private final HashMap<String, Long> cooldownsList = new HashMap<>();

  protected Command(String name) {
    this(name, null, null, 0, -1L);
  }

  protected Command(String name, List<String> usage) {
    this(name, usage, null, 0, -1L);
  }

  protected Command(String name, List<String> usage, int parameters) {
    this(name, usage, null, parameters, -1L);
  }

  protected Command(String name, List<String> usage, List<Permission> permissions, int parameters) {
    this(name, usage, permissions, parameters, -1L);
  }

  public abstract void onCommand(CommandContext context);

}
