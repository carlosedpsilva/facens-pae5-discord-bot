package com.lojfacens.pitchy.module.audio.command;

import static com.lojfacens.pitchy.util.DisUtils.TITLE_EXCEPTION;
import static com.lojfacens.pitchy.util.DisUtils.responseEmbedBuilder;
import static java.util.Map.entry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.lojfacens.pitchy.service.command.meta.Command;
import com.lojfacens.pitchy.service.command.meta.CommandContext;
import com.lojfacens.pitchy.util.StringUtils;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class QueueCommand extends Command {

  private final Map<String, List<String>> optionAliases = Map.ofEntries(
    entry("page", List.of("page", "p", "n"))
  );

  public QueueCommand() {
    super("queue");
  }

  @Override
  public void onCommand(CommandContext context) {
    var manager = context.getAudioManager().getGuildAudioManager(context.getGuild());
    synchronized (manager.getScheduler().getQueue()) {
      var embedResponse = responseEmbedBuilder();

      var currentTrack = manager.getPlayer().getPlayingTrack();
      if (currentTrack == null) {
        embedResponse.setTitle(TITLE_EXCEPTION).setDescription("There is no track playing");
        context.reply(embedResponse.build());
        return;
      }

      var queueSize = manager.getScheduler().getQueue().size();
      var page = -1;
      var tracksPerPage = 10;
      var totalPages = (queueSize / tracksPerPage) + 1;

      // If queue is too bigger to fit in an embed, enable page option
      if (context.hasArguments() && queueSize > 10) {
        var usedOptions = context.getOptionalArguments();
        for (var usedOptionEntry : usedOptions.entrySet()) {
          if (page != -1) break;
          var usedOptionKey = usedOptionEntry.getKey();
          for (var aliasEntry : optionAliases.entrySet()) {
            if (aliasEntry.getValue().contains(usedOptionKey) && aliasEntry.getKey().equals("page")) {
              var optionArgs = usedOptions.get(usedOptionKey);
              var arg = StringUtils.normalizeArray(optionArgs, 1)[0];
              try { page = Integer.parseInt(arg); } catch (NumberFormatException e) { continue; }
              if (page > 0) break;
            }
          }
        }
      }

      page = (page == -1) ? 1 : page;
      var start = (page - 1) * tracksPerPage;
      var end = start + tracksPerPage;

      var queue = new StringBuilder();
      var nextDuration = new AtomicLong();
      var totalDuration = new AtomicLong();
      var totalTrackCount = new AtomicInteger();
      var embedTrackCount = new AtomicInteger();

      queue.append(String.format(
          "%s%n`▶` [%s](%s) · (%s)%n%s%n",
          "⬐ current track ⬎",
          currentTrack.getInfo().title,
          currentTrack.getInfo().uri,
          StringUtils.getTimestamp(currentTrack.getInfo().length),
          "⬑ current track ⬏"
      ));

      manager.getScheduler().getQueue().stream().forEach(audioTrack -> {
        totalTrackCount.getAndIncrement();
        if (totalTrackCount.get() > start && totalTrackCount.get() <= end) {
          embedTrackCount.getAndIncrement();
          queue.append(String.format(
              "`%d)` [%s](%s) · (%s)%n",
              totalTrackCount.get(),
              audioTrack.getInfo().title,
              audioTrack.getInfo().uri,
              StringUtils.getTimestamp(audioTrack.getInfo().length)
          ));
        }
        nextDuration.getAndAdd(audioTrack.getDuration());
        totalDuration.getAndAdd(audioTrack.getDuration());
      });

      if (embedTrackCount.get() >= tracksPerPage)
        queue.append(String.format("...and %d more. ", totalTrackCount.get() - end));
      queue.append(String.format("Page %d of %d.", page, totalPages));

      totalDuration.getAndAdd(currentTrack.getDuration());
      embedResponse.setDescription(queue.toString())
          .addField("Queue length", manager.getScheduler().getQueue().size() + "", true)
          .addField("Queue duration", StringUtils.getTimestamp(nextDuration.get()), true)
          .addField("Total duration", StringUtils.getTimestamp(totalDuration.get()), true);
      context.reply(embedResponse.build());
    }
  }

}
