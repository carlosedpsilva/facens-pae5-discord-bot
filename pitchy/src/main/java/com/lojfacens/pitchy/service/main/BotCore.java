package com.lojfacens.pitchy.service.main;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;

import com.lojfacens.pitchy.config.BotConfig;
import com.lojfacens.pitchy.entity.Shard;
import com.lojfacens.pitchy.event.listener.MessageListener;
import com.lojfacens.pitchy.service.command.CommandProcessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BotCore {

  private final BotConfig botConfig;
  private final BotManager botManager;

  private final CommandProcessor commandProcessor;

  private final MessageListener messageListener;

  @PostConstruct
  private void startUp() {

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (botManager.getShardManager() != null) botManager.getShardManager().shutdown();
    }));

    var token = botConfig.getToken();
    var shardsTotal = botManager.getRecommendedShards(token);
    var shardStartListener = new ShardStartListener(new CountDownLatch(shardsTotal));

    GatewayIntent[] intentsToEnable = {
      GatewayIntent.GUILD_MESSAGES,          // Receive guild messages
      GatewayIntent.GUILD_MESSAGE_REACTIONS, // Receive message reactions
      GatewayIntent.GUILD_MEMBERS,           // Receive member events
      GatewayIntent.GUILD_VOICE_STATES,      // Receive voice states
    };

    CacheFlag[] flagsToDisable = {
      CacheFlag.ACTIVITY,
      CacheFlag.EMOTE,
      CacheFlag.CLIENT_STATUS,
      CacheFlag.ONLINE_STATUS
    };

    try {
      var shardManagerBuilder = DefaultShardManagerBuilder.create(token, Arrays.asList(intentsToEnable))
      .disableCache(Arrays.asList(flagsToDisable))
      .setShardsTotal(shardsTotal)
      .addEventListeners(
          shardStartListener,
          messageListener
      )
      .addEventListenerProvider(id -> getShard(id).getListener());

      log.info("Initializing {} shards...", shardsTotal);
      var start = System.currentTimeMillis();
      botManager.setShardManager(shardManagerBuilder.build());

      new Thread(() -> countdownShards(start, shardStartListener)).start();
      commandProcessor.setupCommands();

    } catch (LoginException e) {
      log.error("Login operation failed");
      throw new IllegalStateException(e);
    }
  }

  private void countdownShards(Long start, ShardStartListener shardStartListener) {
    try {
      shardStartListener.latch.await();
      var elapsed = System.currentTimeMillis() - start;
      log.info("All shards ready! Took {} seconds", TimeUnit.MILLISECONDS.toSeconds(elapsed));
      botManager.getShardManager().removeEventListener(shardStartListener);

    } catch (InterruptedException e) {
      log.error("Shard Countdown Interrupted", e);
      Thread.currentThread().interrupt();
    }
  }

  private Shard getShard(int id) {
    return botManager.getShards().computeIfAbsent(id, Shard::new);
  }

  @AllArgsConstructor
  private static class ShardStartListener implements EventListener {

    private CountDownLatch latch;

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
      if (event instanceof ReadyEvent) {
        var sm = event.getJDA().getShardManager();
        if (sm == null) throw new AssertionError();
        latch.countDown();
      }
    }

  }

}
