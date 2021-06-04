package com.lojfacens.pitchy.service.main;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.lojfacens.pitchy.entity.Shard;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.json.JSONException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.sharding.ShardManager;

@Slf4j
@Service
@RequiredArgsConstructor()
public class BotManager {

  private ShardManager shardManager;
  private int shardsTotal = 1;

  private final Map<Integer, Shard> shards = new ConcurrentHashMap<>();

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

  protected Map<Integer, Shard> getShards() {
    return shards;
  }

  public Collection<Shard> getUnmodifiableShards() {
    return Collections.unmodifiableCollection(shards.values());
  }

  public ShardManager getShardManager() {
    return shardManager;
  }

  protected void setShardManager(ShardManager shardManager) {
    this.shardManager = shardManager;
  }

  public int getShardsTotal() {
    return shardsTotal;
  }

}
