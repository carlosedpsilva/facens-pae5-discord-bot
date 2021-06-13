package com.lojfacens.pitchy.service.command.meta;

import java.util.List;
import java.util.Map;

import com.lojfacens.pitchy.service.audio.AudioManager;
import com.lojfacens.pitchy.service.main.BotManager;
import com.lojfacens.pitchy.util.StringUtils;

import lombok.Builder;
import lombok.Data;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@Data
@Builder
public class CommandContext {

  private final BotManager botManager;
  private final AudioManager audioManager;
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

  public boolean hasArguments() { return content != null; }

  public String[] getArguments() { return getArguments(-1); }

  public String[] getArguments(int expectedArgs) { return StringUtils.advancedSplitArgs(content, expectedArgs); }

  public Map<String, List<String>> getOptionalArguments() { return StringUtils.parseArguments(getArguments()); }

  public void send(Message message) { getChannel().sendMessage(message); }

  public void send(String message) { getChannel().sendMessage(message); }

  public void send(MessageEmbed embed) { getChannel().sendMessage(embed).queue(s -> {}, Throwable::printStackTrace); }

  public void reply(Message message) { getMessage().reply(message).queue(s -> {}, f -> send(message)); }

  public void reply(String message) { getMessage().reply(message).queue(s -> {}, f -> send(message)); }

  public void reply(MessageEmbed embed) { getMessage().reply(embed).queue(s -> {}, f -> send(embed)); }

  public void sendFormat(String message, Object... format) {
    getChannel().sendMessage(
        String.format(message, format)
    ).queue();
  }

}
