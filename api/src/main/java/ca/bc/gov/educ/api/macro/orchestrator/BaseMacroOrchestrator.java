package ca.bc.gov.educ.api.macro.orchestrator;

import ca.bc.gov.educ.api.macro.constants.BusinessUseTypeCodes;
import ca.bc.gov.educ.api.macro.constants.EventType;
import ca.bc.gov.educ.api.macro.constants.TopicsEnum;
import ca.bc.gov.educ.api.macro.messaging.MessagePublisher;
import ca.bc.gov.educ.api.macro.model.SagaEventStates;
import ca.bc.gov.educ.api.macro.struct.v1.Event;
import ca.bc.gov.educ.api.macro.struct.v1.MacroEditNotificationEvent;
import ca.bc.gov.educ.api.macro.model.Saga;
import ca.bc.gov.educ.api.macro.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.macro.properties.ApplicationProperties;
import ca.bc.gov.educ.api.macro.service.SagaService;
import ca.bc.gov.educ.api.macro.struct.v1.Macro;
import ca.bc.gov.educ.api.macro.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static ca.bc.gov.educ.api.macro.constants.SagaStatusEnum.IN_PROGRESS;
import static lombok.AccessLevel.PROTECTED;

/**
 * The type Base macro orchestrator
 */
@Slf4j
public abstract class BaseMacroOrchestrator extends BaseOrchestrator<Macro> {

  @Getter(PROTECTED)
  private final ApplicationProperties properties;

  /**
   * Instantiates a new orchestrator.
   *
   * @param sagaService      the saga service
   * @param messagePublisher the message publisher
   * @param properties       the application properties
   * @param sagaName         the saga name
   * @param topicToSubscribe the topic to subscribe
   */
  public BaseMacroOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, final ApplicationProperties properties,
                               final String sagaName, final String topicToSubscribe) {
    super(sagaService, messagePublisher, Macro.class, sagaName, topicToSubscribe);
    this.properties = properties;
  }

  /**
   * edit macro
   *
   * @param event                          the event
   * @param saga                           the saga
   * @param macroSagaData                  the macro saga data
   * @param editEventType                  the event type of editing macro
   * @throws JsonProcessingException the json processing exception
   */
  protected void editMacro(final Event event, final Saga saga, final Macro macroSagaData, final EventType editEventType) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setStatus(IN_PROGRESS.toString());
    saga.setSagaState(editEventType.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(editEventType)
      .replyTo(this.getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(macroSagaData))
      .build();
    this.postMessageToTopic(TopicsEnum.MACRO_API_TOPIC.toString(), nextEvent);
    log.info("message sent to MACRO_API_TOPIC for " + editEventType.toString() + " Event.");
  }


  /**
   * send notification email
   *
   * @param event                          the event
   * @param saga                           the saga
   * @param macroSagaData                  the macro saga data
   * @param notifyEventType                the event type of notification
   * @throws JsonProcessingException the json processing exception
   */
  protected void sendMacroEditEmail(final Event event, final Saga saga, final Macro macroSagaData, final EventType notifyEventType) throws JsonProcessingException {
    final SagaEventStates eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(notifyEventType.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final var businessUseTypeCode = BusinessUseTypeCodes.valueOf(macroSagaData.getBusinessUseTypeCode());
    final var macroEditNotificationEvent = MacroEditNotificationEvent.builder()
      .fromEmail(this.properties.getFromEmail())
      .toEmail(this.properties.getToEmail())
      .appName(businessUseTypeCode.getApp())
      .businessUseTypeName(businessUseTypeCode.getName())
      .macroTypeCode(macroSagaData.getMacroTypeCode())
      .macroCode(macroSagaData.getMacroCode())
      .macroText(macroSagaData.getMacroText())
      .build();

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(notifyEventType)
      .replyTo(this.getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(macroEditNotificationEvent))
      .build();
    this.postMessageToTopic(TopicsEnum.PROFILE_REQUEST_EMAIL_API_TOPIC.toString(), nextEvent);
    log.info("message sent to PROFILE_REQUEST_EMAIL_API_TOPIC for " + notifyEventType.toString() + " Event.");
  }
}
