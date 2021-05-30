package com.lojfacens.pitchy.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;

import com.lojfacens.pitchy.config.BotConfig;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BotManager {

  private final BotConfig config;

  private ShardManager shardManager;

  @PostConstruct
  private void init() {
    log.info("Starting up {}", config.getBotName());
    var token = config.getToken();

    try {
      var shardsTotal = getRecommendedShards(token);
      var shardStartListener = new ShardStartListener(new CountDownLatch(shardsTotal));

      var shardManagerBuilder = DefaultShardManagerBuilder.createDefault(token)
      .addEventListeners(shardStartListener)
      .setShardsTotal(shardsTotal);

      log.info("Initializing {} shards...", shardsTotal);
      var start = System.currentTimeMillis();
      this.shardManager = shardManagerBuilder.build();
      countDownShards(start, shardStartListener);
    } catch (LoginException e) {
      log.error(e.getMessage());
      throw new IllegalStateException(e);
    }
  }

  private int getRecommendedShards(String token) {
    HttpResponse<JsonNode> request = null;
    try {
      request = Unirest.get("https://discordapp.com/api/gateway/bot")
          .header("Authorization", "Bot " + token)
          .header("Content-type", "application/json")
          .asJson();
      return Integer.parseInt(request.getBody().getObject().get("shards").toString());
    } catch (UnirestException e) { log.error("Failed to request shard count", e);
    } catch (JSONException ignored) { /* invalid token */ }
    return 1;
  }

  @Async
  private void countDownShards(Long start, ShardStartListener shardStartListener) {
    try {
      shardStartListener.latch.await();
      var elapsed = System.currentTimeMillis() - start;
      log.info("All shards ready! Took {} seconds", TimeUnit.MILLISECONDS.toSeconds(elapsed));
      this.shardManager.removeEventListener(shardStartListener);
    } catch (InterruptedException e) {
      log.error("InterruptedException: ", e);
      Thread.currentThread().interrupt();
    }
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
