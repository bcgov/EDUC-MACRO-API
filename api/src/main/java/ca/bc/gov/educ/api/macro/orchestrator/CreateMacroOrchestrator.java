package ca.bc.gov.educ.api.macro.orchestrator;

import ca.bc.gov.educ.api.macro.struct.v1.Event;
import ca.bc.gov.educ.api.macro.struct.v1.Macro;
import ca.bc.gov.educ.api.macro.messaging.MessagePublisher;
import ca.bc.gov.educ.api.macro.model.Saga;
import ca.bc.gov.educ.api.macro.properties.ApplicationProperties;
import ca.bc.gov.educ.api.macro.service.SagaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static ca.bc.gov.educ.api.macro.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.macro.constants.EventType.*;
import static ca.bc.gov.educ.api.macro.constants.SagaEnum.MACRO_CREATE_SAGA;
import static ca.bc.gov.educ.api.macro.constants.TopicsEnum.*;

/**
 * The type Create macro orchestrator
 */
@Component
@Slf4j
public class CreateMacroOrchestrator extends BaseMacroOrchestrator {

  /**
   * Instantiates a new orchestrator.
   *
   * @param sagaService      the saga service
   * @param messagePublisher the message publisher
   * @param properties       the application properties
   */
  public CreateMacroOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, final ApplicationProperties properties) {
    super(sagaService, messagePublisher, properties, MACRO_CREATE_SAGA.toString(), MACRO_CREATE_SAGA_TOPIC.toString());
  }

  /**
   * Populate steps to execute map.
   */
  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
        .begin(CREATE_MACRO, this::createMacro)
        .step(CREATE_MACRO, MACRO_CREATED, NOTIFY_MACRO_CREATE, this::sendMacroCreateEmail)
        .end(NOTIFY_MACRO_CREATE, NOTIFIED);
  }

  /**
   * create macro
   *
   * @param event                          the event
   * @param saga                           the saga
   * @param macroSagaData                  the macro saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void createMacro(final Event event, final ca.bc.gov.educ.api.macro.model.Saga saga, final Macro macroSagaData) throws JsonProcessingException {
    this.editMacro(event, saga, macroSagaData, CREATE_MACRO);
  }


  /**
   * send notification email
   *
   * @param event                          the event
   * @param saga                           the saga
   * @param macroSagaData                  the macro saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void sendMacroCreateEmail(final Event event, final Saga saga, final Macro macroSagaData) throws JsonProcessingException {
    this.sendMacroEditEmail(event, saga, macroSagaData, NOTIFY_MACRO_CREATE);
  }
}
