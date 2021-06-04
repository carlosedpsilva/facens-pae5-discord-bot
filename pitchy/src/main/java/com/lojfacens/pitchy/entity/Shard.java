package com.lojfacens.pitchy.entity;

import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;

@Getter(onMethod = @__(@NonNull))
public class Shard {

  private final int id;
  private final EventListener listener;
  private JDA jda;

  public Shard(int id) {
    this.id = id;
    this.listener = event -> {
      if (event instanceof ReadyEvent)
        synchronized(this) {
          this.jda = event.getJDA();
        }
    };
  }

}
