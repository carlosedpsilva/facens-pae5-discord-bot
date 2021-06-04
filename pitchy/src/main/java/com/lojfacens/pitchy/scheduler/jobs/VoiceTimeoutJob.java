package com.lojfacens.pitchy.scheduler.jobs;

import java.util.concurrent.TimeUnit;

import com.lojfacens.pitchy.scheduler.Job;
import com.lojfacens.pitchy.scheduler.tasks.VoiceTimeoutTask;

import net.dv8tion.jda.api.entities.Guild;

public class VoiceTimeoutJob extends Job {

  private final VoiceTimeoutTask voiceTimeoutTask;

  public VoiceTimeoutJob(Guild guild) {
    super(5, 0, TimeUnit.MINUTES);
    voiceTimeoutTask = new VoiceTimeoutTask(guild);
  }

  @Override
  public void run() {
    handleTask(voiceTimeoutTask);
  }

}
