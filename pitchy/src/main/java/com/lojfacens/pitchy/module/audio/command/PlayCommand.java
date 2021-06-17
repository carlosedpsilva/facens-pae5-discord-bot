package com.lojfacens.pitchy.module.audio.command;

import static com.lojfacens.pitchy.util.DisUtils.TITLE_EXCEPTION;
import static com.lojfacens.pitchy.util.DisUtils.responseEmbedBuilder;
import static com.lojfacens.pitchy.util.StringUtils.URL_PATTERN;
import static java.util.Map.entry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.lojfacens.pitchy.service.audio.AudioLoadHandler;
import com.lojfacens.pitchy.service.audio.AudioLoadHandler.Playback;
import com.lojfacens.pitchy.service.audio.GuildAudioManager;
import com.lojfacens.pitchy.service.command.meta.Command;
import com.lojfacens.pitchy.service.command.meta.CommandContext;
import com.lojfacens.pitchy.util.StringUtils;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=true)
public class PlayCommand extends Command {

  private final Map<String, List<String>> optionAliases = Map.ofEntries(
    entry("play",     List.of("p")),
    entry("playnext", List.of("playnext", "play-next", "next")),
    entry("playnow",  List.of("playnow", "play-now", "now", "forceplay", "force", "f"))
  );

  public PlayCommand() {
    super("play");
  }

  @Override
  public void onCommand(CommandContext context) {
    var manager = context.getAudioManager().getGuildAudioManager(context.getGuild());
    var botVoiceChannel = context.getSelfMember().getVoiceState().getChannel();

    var embedResponse = responseEmbedBuilder();

    // Bot is already in a voice channel
    if (botVoiceChannel != null && !botVoiceChannel.equals(context.getMember().getVoiceState().getChannel())
        && !botVoiceChannel.getMembers().stream().allMatch(m -> m.getUser().isBot())) {
      embedResponse.setTitle("❌ Oops").setDescription("Already in a voice channel");
      context.reply(embedResponse.build());
      return;
    }

    // No argument is provided
    if (!context.hasArguments()) {
      if (manager.getPlayer().getPlayingTrack() == null) {
        embedResponse.setTitle(TITLE_EXCEPTION).setDescription("There is no track playing");
        context.reply(embedResponse.build());
      } else if (manager.getPlayer().isPaused() && manager.getScheduler().isPausedManually()) {
        manager.getPlayer().setPaused(false);
        manager.getScheduler().setPausedManually(false);
        embedResponse.setDescription(String.format("Resumed by %s", context.getMember().getAsMention()));
        context.reply(embedResponse.build());
      } else {
        embedResponse.setTitle(TITLE_EXCEPTION).setDescription("Player not paused");
        context.reply(embedResponse.build());
      }
      return;
    }

    String track = null;
    var urlProvided = false;
    var usedOptions = context.getOptionalArguments();

    var defaultArgsKey = "default";
    List<String> defaultArgs = null;

    // Get link or search term from default args
    if (usedOptions.containsKey(defaultArgsKey)) {
      defaultArgs = usedOptions.get(defaultArgsKey);
      // Get track based on regex pattern
      track = StringUtils.getFirstMatch(defaultArgs, URL_PATTERN.pattern());
      if (track != null) { // If url was found, remove potential angle brackets
        track = track.startsWith("<") && track.endsWith(">")
            ? track.substring(1, track.length() - 1)
            : track;
        urlProvided = true;
      // If url was not found, then join default args into a search term
      } else track = StringUtils.normalizeArray(defaultArgs, 0)[0];
    }

    // Get options
    String lastOptionUsed = null;
    Playback chosenPlaybackType = Playback.PLAY;
    List<String> playbackTypes = Arrays.stream(Playback.values()).map(p -> p.toString().toLowerCase()).collect(Collectors.toList());
    for (var usedOption : usedOptions.keySet()) {
      for (var aliasEntry : optionAliases.entrySet()) {
        if (aliasEntry.getValue().contains(usedOption)) {
          var optionKey = aliasEntry.getKey(); // Get option key
          if (playbackTypes.contains(optionKey)) // Just in case
            chosenPlaybackType = Playback.valueOf(optionKey.toUpperCase());
        }
      }
      lastOptionUsed = usedOption; // Get used option for later use (may be an alias)
    }

    if (urlProvided) {
      openConnection(manager, context);
      AudioLoadHandler.loadAndPlay(manager, context, track, chosenPlaybackType);
      return;
    }

    // Get url from last option args
    var lastOptionUsedArgs = usedOptions.get(lastOptionUsed);
    track = StringUtils.getFirstMatch(lastOptionUsedArgs, URL_PATTERN.pattern());
    if (track != null) { // If url was found, remove potential angle brackets
      track = track.startsWith("<") && track.endsWith(">")
          ? track.substring(1, track.length() - 1)
          : track;
      urlProvided = true;
    // If url was not found, then join default args into a search term
    } else track = StringUtils.normalizeArray(lastOptionUsedArgs, 0)[0];

    if (urlProvided) {
      openConnection(manager, context);
      AudioLoadHandler.loadAndPlay(manager, context, track, chosenPlaybackType);
      return;
    }

    if (track.isBlank()) {
      embedResponse.setTitle(TITLE_EXCEPTION).setDescription("Incorrect command usage");
      context.reply(embedResponse.build());
    }
  }

  private void openConnection(GuildAudioManager manager, CommandContext context) {
    manager.openConnection(context.getMember().getVoiceState().getChannel());
    manager.getScheduler().setRequestedChannel(context.getChannel());
  }

}
