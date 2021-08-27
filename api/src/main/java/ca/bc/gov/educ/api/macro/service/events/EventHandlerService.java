package ca.bc.gov.educ.api.macro.service.events;

import ca.bc.gov.educ.api.macro.service.MacroService;
import ca.bc.gov.educ.api.macro.struct.v1.Event;
import ca.bc.gov.educ.api.macro.constants.EventOutcome;
import ca.bc.gov.educ.api.macro.constants.EventType;
import ca.bc.gov.educ.api.macro.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.macro.mapper.v1.MacroMapper;
import ca.bc.gov.educ.api.macro.model.MacroEvent;
import ca.bc.gov.educ.api.macro.repository.MacroEventRepository;
import ca.bc.gov.educ.api.macro.struct.v1.Macro;
import ca.bc.gov.educ.api.macro.util.JsonUtil;
import ca.bc.gov.educ.api.macro.util.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.api.macro.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.macro.constants.EventStatus.MESSAGE_PUBLISHED;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
public class EventHandlerService {

  /**
   * The constant NO_RECORD_SAGA_ID_EVENT_TYPE.
   */
  public static final String NO_RECORD_SAGA_ID_EVENT_TYPE = "no record found for the saga id and event type combination, processing.";
  /**
   * The constant RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE.
   */
  public static final String RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE = "record found for the saga id and event type combination, might be a duplicate or replay," +
    " just updating the db status so that it will be polled and sent back again.";
  /**
   * The constant EVENT_PAYLOAD.
   */
  public static final String EVENT_PAYLOAD = "event is :: {}";
  /**
   * The constant RESPONDING_BACK.
   */
  public static final String RESPONDING_BACK = "responding back :: {}";

  /**
   * The constant macroMapper.
   */
  private static final MacroMapper macroMapper = MacroMapper.mapper;

  /**
   * The Macro service.
   */
  @Getter(PRIVATE)
  private final MacroService macroService;

  /**
   * The Macro event repo.
   */
  @Getter(PRIVATE)
  private final MacroEventRepository macroEventRepository;


  /**
   * Instantiates a new Event handler service.
   *
   * @param macroEventRepository the macro event repo
   * @param macroService         the macro service
   */
  @Autowired
  public EventHandlerService(final MacroEventRepository macroEventRepository, final MacroService macroService) {
    this.macroEventRepository = macroEventRepository;
    this.macroService = macroService;
  }

  /**
   * Handle create macro event
   *
   * @param event event with the payload
   * @return the response event
   * @throws JsonProcessingException the exception
   */
  @Transactional(propagation = REQUIRES_NEW)
  public byte[] handleCreateMacroEvent(@NonNull final Event event) throws JsonProcessingException {
    MacroEvent macroEvent;
    val macroEventOptional = getMacroEventRepository().findBySagaIdAndEventType(event.getSagaId(), event.getEventType().toString());
    if (macroEventOptional.isEmpty()) {
      log.info(NO_RECORD_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      final Macro macro = JsonUtil.getJsonObjectFromString(Macro.class, event.getEventPayload());
      RequestUtil.setAuditColumnsForCreate(macro);

      val macroEntity = this.macroService.createMacro(macroMapper.toModel(macro));
      event.setEventOutcome(MACRO_CREATED);
      event.setEventPayload(JsonUtil.getJsonStringFromObject(macroMapper.toStructure(macroEntity)));
      macroEvent = createMacroEventRecord(event);
    } else {
      log.info(RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      macroEvent = macroEventOptional.get();
      macroEvent.setUpdateDate(LocalDateTime.now());
    }

    getMacroEventRepository().save(macroEvent);
    return createResponseEvent(macroEvent);
  }

  /**
   * Handle update macro event
   *
   * @param event event with the payload
   * @return the response event
   * @throws JsonProcessingException the exception
   */
  @Transactional(propagation = REQUIRES_NEW)
  public byte[] handleUpdateMacroEvent(@NonNull final Event event) throws JsonProcessingException {
    log.trace(EVENT_PAYLOAD, event);
    final Macro macro = JsonUtil.getJsonObjectFromString(Macro.class, event.getEventPayload());
    RequestUtil.setAuditColumnsForUpdate(macro);
    try {
      val macroEntity = this.macroService.updateMacro(UUID.fromString(macro.getMacroId()), macroMapper.toModel(macro));
      event.setEventOutcome(MACRO_UPDATED);
      event.setEventPayload(JsonUtil.getJsonStringFromObject(macroMapper.toStructure(macroEntity)));
    } catch (EntityNotFoundException ex) {
      event.setEventOutcome(MACRO_NOT_FOUND);
    }

    val macroEvent = createMacroEventRecord(event);
    getMacroEventRepository().save(macroEvent);
    return createResponseEvent(macroEvent);
  }

  private MacroEvent createMacroEventRecord(Event event) {
    return MacroEvent.builder()
      .createDate(LocalDateTime.now())
      .updateDate(LocalDateTime.now())
      .createUser(event.getEventType().toString()) //need to discuss what to put here.
      .updateUser(event.getEventType().toString())
      .eventPayload(event.getEventPayload())
      .eventType(event.getEventType().toString())
      .sagaId(event.getSagaId())
      .eventStatus(MESSAGE_PUBLISHED.toString())
      .eventOutcome(event.getEventOutcome().toString())
      .replyChannel(event.getReplyTo())
      .build();
  }

  private byte[] createResponseEvent(MacroEvent event) throws JsonProcessingException {
    val responseEvent = Event.builder()
      .sagaId(event.getSagaId())
      .eventType(EventType.valueOf(event.getEventType()))
      .eventOutcome(EventOutcome.valueOf(event.getEventOutcome()))
      .eventPayload(event.getEventPayload()).build();
    if (log.isDebugEnabled()) {
      log.debug(RESPONDING_BACK, responseEvent);
    }
    return JsonUtil.getJsonBytesFromObject(responseEvent);
  }

}
