package com.lojfacens.pitchy.scheduler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class ScheduleHandler {

  private static final Set<ScheduledFuture<?>> tasks = new HashSet<>();
  private static final ScheduledExecutorService schedulerService = Executors.newScheduledThreadPool(1);

  private ScheduleHandler() { }

  public static void registerJob(Job job) {
    tasks.add(schedulerService.scheduleAtFixedRate(job, job.getDelay(), job.getJobPeriod(), job.getUnit()));
  }

  public static ScheduledFuture<?> registerUniqueJob(Job job) {
    return schedulerService.schedule(job, job.getDelay(), job.getUnit());
  }

}
