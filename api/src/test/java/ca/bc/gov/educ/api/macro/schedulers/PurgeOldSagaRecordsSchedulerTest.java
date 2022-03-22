package ca.bc.gov.educ.api.macro.schedulers;

import ca.bc.gov.educ.api.macro.MacroApiResourceApplication;
import ca.bc.gov.educ.api.macro.constants.EventStatus;
import ca.bc.gov.educ.api.macro.model.MacroEvent;
import ca.bc.gov.educ.api.macro.model.Saga;
import ca.bc.gov.educ.api.macro.model.SagaEvent;
import ca.bc.gov.educ.api.macro.repository.MacroEventRepository;
import ca.bc.gov.educ.api.macro.repository.SagaEventRepository;
import ca.bc.gov.educ.api.macro.repository.SagaRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.macro.constants.SagaStatusEnum.COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {MacroApiResourceApplication.class})
@AutoConfigureMockMvc
@Slf4j
public class PurgeOldSagaRecordsSchedulerTest {

  @Autowired
  SagaRepository repository;

  @Autowired
  SagaEventRepository sagaEventRepository;

  @Autowired
  MacroEventRepository macroEventRepository;

  @Autowired
  PurgeOldSagaRecordsScheduler purgeOldSagaRecordsScheduler;


  @Test
  public void pollSagaTableAndPurgeOldRecords_givenOldRecordsPresent_shouldBeDeleted() {
    final String macroId = "7f000101-7151-1d84-8171-5187006c0000";
    final var payload = " {\n" +
        "    \"createUser\": \"test\",\n" +
        "    \"updateUser\": \"test\",\n" +
        "    \"macroId\": \"" + macroId + "\",\n" +
        "  }";
    final var saga_today = this.getSaga(payload, LocalDateTime.now());
    final var yesterday = LocalDateTime.now().minusDays(1);
    final var saga_yesterday = this.getSaga(payload, yesterday);

    this.repository.save(saga_today);
    this.sagaEventRepository.save(this.getSagaEvent(saga_today, payload));
    this.macroEventRepository.save(this.getMacroEvent(saga_today, payload, LocalDateTime.now()));

    this.repository.save(saga_yesterday);
    this.sagaEventRepository.save(this.getSagaEvent(saga_yesterday, payload));
    this.macroEventRepository.save(this.getMacroEvent(saga_yesterday, payload, yesterday));

    this.purgeOldSagaRecordsScheduler.setSagaRecordStaleInDays(1);
    this.purgeOldSagaRecordsScheduler.purgeOldRecords();
    final var sagas = this.repository.findAll();
    assertThat(sagas).hasSize(1);

    final var sagaEvents = this.sagaEventRepository.findAll();
    assertThat(sagaEvents).hasSize(1);

    final var servicesEvents = this.macroEventRepository.findAll();
    assertThat(servicesEvents).hasSize(1);
  }


  private Saga getSaga(final String payload, final LocalDateTime createDateTime) {
    return Saga
        .builder()
        .payload(payload)
        .sagaName("MACRO_CREATE_SAGA")
        .status(COMPLETED.toString())
        .sagaState(COMPLETED.toString())
        .createDate(createDateTime)
        .createUser("MACRO_API")
        .updateUser("MACRO_API")
        .updateDate(createDateTime)
        .build();
  }

  private SagaEvent getSagaEvent(final Saga saga, final String payload) {
    return SagaEvent
        .builder()
        .sagaEventResponse(payload)
        .saga(saga)
        .sagaEventState("CREATE_MACRO")
        .sagaStepNumber(1)
        .sagaEventOutcome("MACRO_CREATED")
        .createDate(LocalDateTime.now())
        .createUser("MACRO_API")
        .updateUser("MACRO_API")
        .updateDate(LocalDateTime.now())
        .build();
  }

  private MacroEvent getMacroEvent(final Saga saga, final String payload, final LocalDateTime createDateTime) {
    return MacroEvent
      .builder()
      .eventPayloadBytes(payload.getBytes())
      .eventStatus(EventStatus.MESSAGE_PUBLISHED.toString())
      .eventType("CREATE_MACRO")
      .sagaId(saga.getSagaId())
      .eventOutcome("MACRO_CREATED")
      .replyChannel("TEST_CHANNEL")
      .createDate(createDateTime)
      .createUser("MACRO_API")
      .updateUser("MACRO_API")
      .updateDate(createDateTime)
      .build();
  }
}
