package ca.bc.gov.educ.api.macro.service.events;

import ca.bc.gov.educ.api.macro.repository.MacroRepository;
import ca.bc.gov.educ.api.macro.service.MacroService;
import ca.bc.gov.educ.api.macro.struct.v1.Event;
import ca.bc.gov.educ.api.macro.support.NatsMessageImpl;
import ca.bc.gov.educ.api.macro.constants.EventOutcome;
import ca.bc.gov.educ.api.macro.mapper.v1.MacroMapper;
import ca.bc.gov.educ.api.macro.messaging.MessagePublisher;
import ca.bc.gov.educ.api.macro.model.MacroEntity;
import ca.bc.gov.educ.api.macro.model.MacroEvent;
import ca.bc.gov.educ.api.macro.repository.MacroEventRepository;
import ca.bc.gov.educ.api.macro.struct.v1.Macro;
import ca.bc.gov.educ.api.macro.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Message;
import lombok.val;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.api.macro.constants.EventStatus.MESSAGE_PUBLISHED;
import static ca.bc.gov.educ.api.macro.constants.EventType.CREATE_MACRO;
import static ca.bc.gov.educ.api.macro.constants.EventType.UPDATE_MACRO;
import static ca.bc.gov.educ.api.macro.constants.TopicsEnum.MACRO_CREATE_SAGA_TOPIC;
import static ca.bc.gov.educ.api.macro.constants.TopicsEnum.MACRO_UPDATE_SAGA_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class EventHandlerDelegatorServiceTest {

  private static final MacroMapper mapper = MacroMapper.mapper;

  @Autowired
  MessagePublisher messagePublisher;

  @Autowired
  Connection connection;

  @Autowired
  EventHandlerDelegatorService eventHandlerDelegatorService;

  @Captor
  ArgumentCaptor<byte[]> eventCaptor;

  @Autowired
  MacroEventRepository macroEventRepository;


  @Autowired
  MacroService service;

  @Autowired
  MacroRepository macroRepository;

  @After
  public void after() {
    this.macroEventRepository.deleteAll();
    this.macroRepository.deleteAll();
  }

  @Test
  public void handleEvent_givenCreateMacroEvent_shouldRespondToNats() throws JsonProcessingException {
    final var payload = this.getMacroPayloadFromJsonString(null);
    final var event = Event.builder()
        .eventType(CREATE_MACRO)
        .replyTo(MACRO_CREATE_SAGA_TOPIC.toString())
        .eventPayload(JsonUtil.getJsonStringFromObject(payload))
        .sagaId(UUID.randomUUID())
        .build();
    final Message message = NatsMessageImpl.builder()
        .connection(this.connection)
        .data(JsonUtil.getJsonBytesFromObject(event))
        .SID("SID")
        .replyTo(MACRO_CREATE_SAGA_TOPIC.toString())
        .build();
    this.eventHandlerDelegatorService.handleEvent(event, message);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(any(), this.eventCaptor.capture());
    final var replyEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    final Macro macro = new ObjectMapper().readValue(replyEvent.getEventPayload(), new TypeReference<>() {
    });
    assertThat(replyEvent).isNotNull();
    assertThat(replyEvent.getEventOutcome()).isEqualTo(EventOutcome.MACRO_CREATED);
    assertThat(macro.getMacroId()).isNotBlank();
    assertThat(macro.getBusinessUseTypeCode()).isEqualTo(payload.getBusinessUseTypeCode());
    assertThat(macro.getMacroTypeCode()).isEqualTo(payload.getMacroTypeCode());
    assertThat(macro.getMacroCode()).isEqualTo(payload.getMacroCode());
    assertThat(macro.getMacroText()).isEqualTo(payload.getMacroText());

  }

  @Test
  public void handleEvent_givenDuplicateCreateMacroEvent_shouldRespondToNats() throws JsonProcessingException {
    final var payload = createMacroEntities();
    final var event = Event.builder()
      .eventType(CREATE_MACRO)
      .replyTo(MACRO_CREATE_SAGA_TOPIC.toString())
      .eventPayload(JsonUtil.getJsonStringFromObject(payload))
      .sagaId(UUID.randomUUID())
      .build();
    final Message message = NatsMessageImpl.builder()
      .connection(this.connection)
      .data(JsonUtil.getJsonBytesFromObject(event))
      .SID("SID")
      .replyTo(MACRO_CREATE_SAGA_TOPIC.toString())
      .build();

    this.createMacroEvent(event);

    this.eventHandlerDelegatorService.handleEvent(event, message);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(any(), this.eventCaptor.capture());
    final var replyEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    final Macro macro = new ObjectMapper().readValue(replyEvent.getEventPayload(), new TypeReference<>() {
    });
    assertThat(replyEvent).isNotNull();
    assertThat(replyEvent.getEventOutcome()).isEqualTo(EventOutcome.MACRO_CREATED);
    assertThat(macro.getMacroId()).isNotBlank();
    assertThat(macro.getBusinessUseTypeCode()).isEqualTo(payload.getBusinessUseTypeCode());
    assertThat(macro.getMacroTypeCode()).isEqualTo(payload.getMacroTypeCode());
    assertThat(macro.getMacroCode()).isEqualTo(payload.getMacroCode());
    assertThat(macro.getMacroText()).isEqualTo(payload.getMacroText());

  }

  @Test
  public void handleEvent_givenUpdateMacroEvent_shouldRespondToNats() throws JsonProcessingException {
    MacroEntity savedEntity = createMacroEntities();
    final var changedText = "Changed Text";
    savedEntity.setMacroText(changedText);
    final var payload = mapper.toStructure(savedEntity);
    final var event = Event.builder()
      .eventType(UPDATE_MACRO)
      .replyTo(MACRO_UPDATE_SAGA_TOPIC.toString())
      .eventPayload(JsonUtil.getJsonStringFromObject(payload))
      .sagaId(UUID.randomUUID())
      .build();
    final Message message = NatsMessageImpl.builder()
      .connection(this.connection)
      .data(JsonUtil.getJsonBytesFromObject(event))
      .SID("SID")
      .replyTo(MACRO_UPDATE_SAGA_TOPIC.toString())
      .build();
    this.eventHandlerDelegatorService.handleEvent(event, message);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(any(), this.eventCaptor.capture());
    final var replyEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    final Macro macro = new ObjectMapper().readValue(replyEvent.getEventPayload(), new TypeReference<>() {
    });
    assertThat(replyEvent).isNotNull();
    assertThat(replyEvent.getEventOutcome()).isEqualTo(EventOutcome.MACRO_UPDATED);
    assertThat(macro.getMacroId()).isNotBlank();
    assertThat(macro.getBusinessUseTypeCode()).isEqualTo(payload.getBusinessUseTypeCode());
    assertThat(macro.getMacroTypeCode()).isEqualTo(payload.getMacroTypeCode());
    assertThat(macro.getMacroCode()).isEqualTo(payload.getMacroCode());
    assertThat(macro.getMacroText()).isEqualTo(changedText);

  }

  @Test
  public void handleEvent_givenUpdateMacroEventWithInvalidMacroId_shouldRespondToNats() throws JsonProcessingException {
    final var payload = getMacroPayloadFromJsonString(UUID.randomUUID().toString());
    final var event = Event.builder()
      .eventType(UPDATE_MACRO)
      .replyTo(MACRO_UPDATE_SAGA_TOPIC.toString())
      .eventPayload(JsonUtil.getJsonStringFromObject(payload))
      .sagaId(UUID.randomUUID())
      .build();
    final Message message = NatsMessageImpl.builder()
      .connection(this.connection)
      .data(JsonUtil.getJsonBytesFromObject(event))
      .SID("SID")
      .replyTo(MACRO_UPDATE_SAGA_TOPIC.toString())
      .build();
    this.eventHandlerDelegatorService.handleEvent(event, message);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(any(), this.eventCaptor.capture());
    final var replyEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    final Macro macro = new ObjectMapper().readValue(replyEvent.getEventPayload(), new TypeReference<>() {
    });
    assertThat(replyEvent).isNotNull();
    assertThat(replyEvent.getEventOutcome()).isEqualTo(EventOutcome.MACRO_NOT_FOUND);
    assertThat(macro.getMacroId()).isNotBlank();
    assertThat(macro.getBusinessUseTypeCode()).isEqualTo(payload.getBusinessUseTypeCode());
    assertThat(macro.getMacroTypeCode()).isEqualTo(payload.getMacroTypeCode());
    assertThat(macro.getMacroCode()).isEqualTo(payload.getMacroCode());
    assertThat(macro.getMacroText()).isEqualTo(payload.getMacroText());

  }

  /**
   * Dummy macro saga data json string.
   *
   * @return the string
   */
  protected String placeholderMacroData(String macroId) {
    var macroData = " {\n";
    if(macroId != null) {
      macroData += "    \"macroId\": \"" + macroId + "\",\n";
    }
    return macroData +
      "    \"createUser\": \"user\",\n" +
      "    \"updateUser\": \"user\",\n" +
      "    \"businessUseTypeCode\": \"PENREG\",\n" +
      "    \"macroCode\": \"hi\",\n" +
      "    \"macroTypeCode\": \"MERGE\",\n" +
      "    \"macroText\": \"hello\"\n" +
      "  }";
  }



  /**
   * Gets macro data from json string.
   *
   * @return the macro data from json string
   */
  protected Macro getMacroPayloadFromJsonString(String macroId) {
    try {
      return JsonUtil.getJsonObjectFromString(Macro.class, this.placeholderMacroData(macroId));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected MacroEntity createMacroEntities() {
    val entity = mapper.toModel(this.getMacroPayloadFromJsonString(null));
    entity.setMacroId(null);
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    return this.service.createMacro(entity);
  }

  protected MacroEvent createMacroEvent(Event event) {
    event.setEventOutcome(EventOutcome.MACRO_CREATED);
    var macroEvent = MacroEvent.builder()
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
    return macroEventRepository.save(macroEvent);
  }
}
