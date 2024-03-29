package ca.bc.gov.educ.api.macro.schedulers;

import ca.bc.gov.educ.api.macro.repository.MacroEventRepository;
import ca.bc.gov.educ.api.macro.repository.SagaEventRepository;
import ca.bc.gov.educ.api.macro.repository.SagaRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
public class PurgeOldSagaRecordsScheduler {
  @Getter(PRIVATE)
  private final SagaRepository sagaRepository;

  @Getter(PRIVATE)
  private final SagaEventRepository sagaEventRepository;

  @Getter(PRIVATE)
  private final MacroEventRepository macroEventRepository;

  @Value("${purge.records.saga.after.days}")
  @Setter
  @Getter
  Integer sagaRecordStaleInDays;

  public PurgeOldSagaRecordsScheduler(final SagaRepository sagaRepository, final SagaEventRepository sagaEventRepository, final MacroEventRepository macroEventRepository) {
    this.sagaRepository = sagaRepository;
    this.sagaEventRepository = sagaEventRepository;
    this.macroEventRepository = macroEventRepository;
  }


  /**
   * run the job based on configured scheduler(a cron expression) and purge old records from DB.
   */
  @Scheduled(cron = "${scheduled.jobs.purge.old.saga.records.cron}")
  @SchedulerLock(name = "PurgeOldSagaRecordsLock",
      lockAtLeastFor = "PT1H", lockAtMostFor = "PT1H") //midnight job so lock for an hour
  @Transactional
  public void purgeOldRecords() {
    LockAssert.assertLocked();
    final LocalDateTime createDateToCompare = this.calculateCreateDateBasedOnStaleSagaRecordInDays();
    this.sagaEventRepository.deleteBySagaCreateDateBefore(createDateToCompare);
    this.macroEventRepository.deleteByCreateDateBefore(createDateToCompare);
    this.sagaRepository.deleteByCreateDateBefore(createDateToCompare);
    log.info("Purged old saga and event records");
  }

  private LocalDateTime calculateCreateDateBasedOnStaleSagaRecordInDays() {
    final LocalDateTime currentTime = LocalDateTime.now();
    return currentTime.minusDays(this.getSagaRecordStaleInDays());
  }
}
