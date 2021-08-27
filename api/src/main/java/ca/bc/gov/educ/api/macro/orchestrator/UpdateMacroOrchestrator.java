package ca.bc.gov.educ.api.macro.orchestrator;

import ca.bc.gov.educ.api.macro.constants.EventOutcome;
import ca.bc.gov.educ.api.macro.constants.EventType;
import ca.bc.gov.educ.api.macro.constants.SagaEnum;
import ca.bc.gov.educ.api.macro.constants.TopicsEnum;
import ca.bc.gov.educ.api.macro.struct.v1.Event;
import ca.bc.gov.educ.api.macro.messaging.MessagePublisher;
import ca.bc.gov.educ.api.macro.model.Saga;
import ca.bc.gov.educ.api.macro.properties.ApplicationProperties;
import ca.bc.gov.educ.api.macro.service.SagaService;
import ca.bc.gov.educ.api.macro.struct.v1.Macro;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * The type Update macro orchestrator
 */
@Component
@Slf4j
public class UpdateMacroOrchestrator extends BaseMacroOrchestrator {

  /**
   * Instantiates a new orchestrator.
   *
   * @param sagaService      the saga service
   * @param messagePublisher the message publisher
   * @param properties       the application properties
   */
  public UpdateMacroOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, final ApplicationProperties properties) {
    super(sagaService, messagePublisher, properties, SagaEnum.MACRO_UPDATE_SAGA.toString(), TopicsEnum.MACRO_UPDATE_SAGA_TOPIC.toString());
  }

  /**
   * Populate steps to execute map.
   */
  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
        .begin(EventType.UPDATE_MACRO, this::updateMacro)
        .step(EventType.UPDATE_MACRO, EventOutcome.MACRO_UPDATED, EventType.NOTIFY_MACRO_UPDATE, this::sendMacroUpdateEmail)
        .end(EventType.NOTIFY_MACRO_UPDATE, EventOutcome.NOTIFIED);
  }

  /**
   * update macro
   *
   * @param event                          the event
   * @param saga                           the saga
   * @param macroSagaData                  the macro saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void updateMacro(final Event event, final Saga saga, final Macro macroSagaData) throws JsonProcessingException {
    this.editMacro(event, saga, macroSagaData, EventType.UPDATE_MACRO);
  }


  /**
   * send notification email
   *
   * @param event                          the event
   * @param saga                           the saga
   * @param macroSagaData                  the macro saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void sendMacroUpdateEmail(final Event event, final Saga saga, final Macro macroSagaData) throws JsonProcessingException {
    this.sendMacroEditEmail(event, saga, macroSagaData, EventType.NOTIFY_MACRO_UPDATE);
  }
}
