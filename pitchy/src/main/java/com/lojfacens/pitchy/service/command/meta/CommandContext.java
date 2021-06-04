package com.lojfacens.pitchy.service.command.meta;

import com.lojfacens.pitchy.service.main.BotManager;

import lombok.Builder;
import lombok.Data;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@Data
@Builder
public class CommandContext {

  private final BotManager botManager;
  private final GuildMessageReceivedEvent event;
  private final Command command;
  private final String content;
  private final boolean isMentionPrefix;

  public JDA getJDA() { return event.getJDA(); }

  public Member getMember() { return event.getMember(); }

  public User getUser() { return event.getAuthor(); }

  public User getAuthor() { return event.getAuthor(); }

  public Guild getGuild() { return event.getGuild(); }

  public TextChannel getChannel() { return event.getChannel(); }

  public Message getMessage() { return event.getMessage(); }

  public SelfUser getSelfUser() { return getJDA().getSelfUser(); }

  public Member getSelfMember() { return getGuild().getSelfMember(); }

}
