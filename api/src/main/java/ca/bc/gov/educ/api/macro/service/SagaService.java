package ca.bc.gov.educ.api.macro.service;

import ca.bc.gov.educ.api.macro.constants.EventType;
import ca.bc.gov.educ.api.macro.model.SagaEventStates;
import ca.bc.gov.educ.api.macro.repository.SagaEventRepository;
import ca.bc.gov.educ.api.macro.model.Saga;
import ca.bc.gov.educ.api.macro.repository.SagaRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.api.macro.constants.SagaStatusEnum.STARTED;
import static lombok.AccessLevel.PRIVATE;

/**
 * The type Saga service.
 */
@Service
@Slf4j
public class SagaService {
  /**
   * The Saga repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final SagaRepository sagaRepository;
  /**
   * The Saga event repository.
   */
  @Getter(PRIVATE)
  private final SagaEventRepository sagaEventRepository;

  /**
   * Instantiates a new Saga service.
   *
   * @param sagaRepository      the saga repository
   * @param sagaEventRepository the saga event repository
   */
  @Autowired
  public SagaService(final SagaRepository sagaRepository, final SagaEventRepository sagaEventRepository) {
    this.sagaRepository = sagaRepository;
    this.sagaEventRepository = sagaEventRepository;
  }


  /**
   * Create saga record saga.
   *
   * @param saga the saga
   * @return the saga
   */
  public Saga createSagaRecord(final Saga saga) {
    return this.getSagaRepository().save(saga);
  }

  /**
   * no need to do a get here as it is an attached entity
   * first find the child record, if exist do not add. this scenario may occur in replay process,
   * so dont remove this check. removing this check will lead to duplicate records in the child table.
   *
   * @param saga            the saga object.
   * @param sagaEventStates the saga event
   */
  @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(multiplier = 2, delay = 2000))
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateAttachedSagaWithEvents(final Saga saga, final SagaEventStates sagaEventStates) {
    saga.setUpdateDate(LocalDateTime.now());
    this.getSagaRepository().save(saga);
    val result = this.getSagaEventRepository()
        .findBySagaAndSagaEventOutcomeAndSagaEventStateAndSagaStepNumber(saga, sagaEventStates.getSagaEventOutcome(), sagaEventStates.getSagaEventState(), sagaEventStates.getSagaStepNumber() - 1); //check if the previous step was same and had same outcome, and it is due to replay.
    if (result.isEmpty()) {
      this.getSagaEventRepository().save(sagaEventStates);
    }
  }

  /**
   * Find saga by id optional.
   *
   * @param sagaId the saga id
   * @return the optional
   */
  public Optional<Saga> findSagaById(final UUID sagaId) {
    return this.getSagaRepository().findById(sagaId);
  }

  /**
   * Find all saga states list.
   *
   * @param saga the saga
   * @return the list
   */
  public List<SagaEventStates> findAllSagaStates(final Saga saga) {
    return this.getSagaEventRepository().findBySaga(saga);
  }


  /**
   * Update saga record.
   *
   * @param saga the saga
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public void updateSagaRecord(final Saga saga) { // saga here MUST be an attached entity
    this.getSagaRepository().save(saga);
  }

  /**
   * Find by macro id optional.
   *
   * @param macroId the macro id
   * @param sagaName  the saga name
   * @return the list
   */
  public Optional<Saga> findByStudentIDAndSagaName(final UUID macroId, final String sagaName) {
    return this.getSagaRepository().findByMacroIdAndSagaName(macroId, sagaName);
  }

  /**
   * Find all by macro id and status in list.
   *
   * @param macroId the macro id
   * @param sagaName  the saga name
   * @param statuses  the statuses
   * @return the list
   */
  public List<Saga> findAllByMacroIdAndStatusIn(final UUID macroId, final String sagaName, final List<String> statuses) {
    return this.getSagaRepository().findAllByMacroIdAndSagaNameAndStatusIn(macroId, sagaName, statuses);
  }

  /**
   * Update attached entity during saga process.
   *
   * @param saga the saga
   */
  @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(multiplier = 2, delay = 2000))
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateAttachedEntityDuringSagaProcess(final Saga saga) {
    this.getSagaRepository().save(saga);
  }

  /**
   * Create saga record in db saga.
   *
   * @param sagaName  the saga name
   * @param userName  the user name
   * @param payload   the payload
   * @param macroId   the macro id
   * @return the saga
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Saga createSagaRecordInDB(final String sagaName, final String userName, final String payload, final UUID macroId) {
    final var saga = Saga
        .builder()
        .payload(payload)
        .macroId(macroId)
        .sagaName(sagaName)
        .status(STARTED.toString())
        .sagaState(EventType.INITIATED.toString())
        .createDate(LocalDateTime.now())
        .createUser(userName)
        .updateUser(userName)
        .updateDate(LocalDateTime.now())
        .build();
    return this.createSagaRecord(saga);
  }
}
