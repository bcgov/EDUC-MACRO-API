package ca.bc.gov.educ.api.macro.config;

import ca.bc.gov.educ.api.macro.util.ThreadFactoryBuilder;
import lombok.val;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * The type Async configuration.
 */
@Configuration
@EnableAsync
@Profile("!test")
public class AsyncConfiguration {
  /**
   * Thread pool task executor executor.
   *
   * @return the executor
   */
  @Bean(name = "subscriberExecutor")
  public Executor threadPoolTaskExecutor() {
    return new EnhancedQueueExecutor.Builder().setThreadFactory(new ThreadFactoryBuilder().withNameFormat("message-subscriber-%d").get())
        .setCorePoolSize(10)
        .setMaximumPoolSize(10)
        .setKeepAliveTime(Duration.ofSeconds(60))
        .build();
  }

  /**
   * Controller task executor executor.
   *
   * @return the executor
   */
  @Bean(name = "taskExecutor")
  public Executor controllerTaskExecutor() {
    return new EnhancedQueueExecutor.Builder().setThreadFactory(new ThreadFactoryBuilder().withNameFormat("async-executor-%d").get())
      .setCorePoolSize(10)
      .setMaximumPoolSize(10)
      .setKeepAliveTime(Duration.ofSeconds(60))
      .build();
  }

  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    val threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(5);
    threadPoolTaskScheduler.setThreadFactory(new ThreadFactoryBuilder().withNameFormat("scheduled-task-%d").get());
    return threadPoolTaskScheduler;
  }

}
