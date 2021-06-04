package com.lojfacens.pitchy.scheduler;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@AllArgsConstructor
public abstract class Job extends TimerTask {

  private final long delay;
  private final long jobPeriod;
  private final TimeUnit unit;

  protected void handleTask(Task... tasks) {
    for (var task : tasks) {
      try {
        log.trace("Invoking {}#handle()", task.getClass().getName());
        task.run();
      } catch (Exception e) {
        log.error("An error occurred while running the {} class", task.getClass().getSimpleName(), e);
      }
    }
  }

}
