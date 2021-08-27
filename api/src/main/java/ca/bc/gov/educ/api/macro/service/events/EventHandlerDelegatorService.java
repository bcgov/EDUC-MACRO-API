package ca.bc.gov.educ.api.macro.service.events;

import ca.bc.gov.educ.api.macro.struct.v1.Event;
import ca.bc.gov.educ.api.macro.messaging.MessagePublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.nats.client.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static lombok.AccessLevel.PRIVATE;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
public class EventHandlerDelegatorService {
  /**
   * The constant PAYLOAD_LOG.
   */
  public static final String PAYLOAD_LOG = "Payload is :: {}";
  /**
   * The Event handler service.
   */
  @Getter
  private final EventHandlerService eventHandlerService;

  /**
   * The Message publisher.
   */
  @Getter(PRIVATE)
  private final MessagePublisher messagePublisher;


  /**
   * Instantiates a new Event handler delegator service.
   *
   * @param eventHandlerService the event handler service
   * @param messagePublisher    the message publisher
   */
  @Autowired
  public EventHandlerDelegatorService(final EventHandlerService eventHandlerService, final MessagePublisher messagePublisher) {
    this.eventHandlerService = eventHandlerService;
    this.messagePublisher = messagePublisher;
  }

  /**
   * Handle event.
   *
   * @param event   the event
   * @param message the message
   */
  @Async("subscriberExecutor")
  public void handleEvent(final Event event, final Message message) {
    final boolean isSynchronous = message.getReplyTo() != null;
    final byte[] result;
    try {
      switch (event.getEventType()) {
        case CREATE_MACRO:
          log.info("received create macro data :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          result = this.getEventHandlerService().handleCreateMacroEvent(event);
          this.publishToNATS(event, message, isSynchronous, result);
          break;
        case UPDATE_MACRO:
          log.info("received update macro data :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          result = this.getEventHandlerService().handleUpdateMacroEvent(event);
          this.publishToNATS(event, message, isSynchronous, result);
          break;
        default:
          log.info("silently ignoring other event :: {}", event);
          break;
      }
    } catch (final Exception e) {
      log.error("Exception", e);
    }
  }

  /**
   * Publish to nats.
   *
   * @param event         the event
   * @param message       the message
   * @param isSynchronous the is synchronous
   * @param response      the response event
   */
  private void publishToNATS(final Event event, final Message message, final boolean isSynchronous, final byte[] response) throws JsonProcessingException {
    if (isSynchronous) { // this is for synchronous request/reply pattern.
      this.getMessagePublisher().dispatchMessage(message.getReplyTo(), response);
    } else { // this is for async.
      this.getMessagePublisher().dispatchMessage(event.getReplyTo(), response);
    }
  }

}
