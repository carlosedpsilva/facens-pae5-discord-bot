package com.lojfacens.pitchy.service.main;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.lojfacens.pitchy.commands.Command;
import com.lojfacens.pitchy.commands.CommandModule;
import com.lojfacens.pitchy.entity.Shard;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.json.JSONException;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.sharding.ShardManager;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BotManager {

  private final Map<Integer, Shard> shards = new ConcurrentHashMap<>();

  private ShardManager shardManager;
  private int shardsTotal = 1;

  public int getRecommendedShards(String token) {
    HttpResponse<JsonNode> request = null;
    try {
      request = Unirest.get("https://discordapp.com/api/gateway/bot")
          .header("Authorization", "Bot " + token)
          .header("Content-type", "application/json")
          .asJson();
      shardsTotal = Integer.parseInt(request.getBody().getObject().get("shards").toString());
    } catch (UnirestException e) { log.error("Failed to request shard count", e);
    } catch (JSONException ignored) { /* invalid token */ }
    return shardsTotal;
  }

  public int getShardsTotal() {
    return shardsTotal;
  }

  public Collection<Shard> getShards() {
    return Collections.unmodifiableCollection(shards.values());
  }

  protected Map<Integer, Shard> getShardsMap() {
    return shards;
  }

  public ShardManager getShardManager() {
    return shardManager;
  }

  protected void setShardManager(ShardManager shardManager) {
    this.shardManager = shardManager;
  }

}
