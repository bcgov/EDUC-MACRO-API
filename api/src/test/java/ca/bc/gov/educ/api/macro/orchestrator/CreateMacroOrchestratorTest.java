package ca.bc.gov.educ.api.macro.orchestrator;

import ca.bc.gov.educ.api.macro.MacroApiResourceApplication;
import ca.bc.gov.educ.api.macro.constants.*;
import ca.bc.gov.educ.api.macro.messaging.MessagePublisher;
import ca.bc.gov.educ.api.macro.repository.SagaEventRepository;
import ca.bc.gov.educ.api.macro.repository.SagaRepository;
import ca.bc.gov.educ.api.macro.service.SagaService;
import ca.bc.gov.educ.api.macro.struct.v1.Event;
import ca.bc.gov.educ.api.macro.struct.v1.Macro;
import ca.bc.gov.educ.api.macro.struct.v1.MacroEditNotificationEvent;
import ca.bc.gov.educ.api.macro.util.JsonUtil;
import ca.bc.gov.educ.api.macro.model.Saga;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {MacroApiResourceApplication.class})
@AutoConfigureMockMvc
public class CreateMacroOrchestratorTest {
  /**
   * The Repository.
   */
  @Autowired
  SagaRepository repository;
  /**
   * The Saga event repository.
   */
  @Autowired
  SagaEventRepository sagaEventRepository;
  /**
   * The Saga service.
   */
  @Autowired
  private SagaService sagaService;

  /**
   * The Message publisher.
   */
  @Autowired
  private MessagePublisher messagePublisher;

  @Autowired
  private CreateMacroOrchestrator orchestrator;

  /**
   * The Saga.
   */
  private Saga saga;
  /**
   * The Saga data.
   */
  private Macro sagaData;

  /**
   * The Event captor.
   */
  @Captor
  ArgumentCaptor<byte[]> eventCaptor;

  String sagaPayload;

  String macroID = UUID.randomUUID().toString();

  /**
   * Sets up.
   */
  @Before
  public void setUp() throws JsonProcessingException {
    MockitoAnnotations.openMocks(this);
    sagaData = getMacroSagaDataFromJsonString();
    sagaPayload = JsonUtil.getJsonStringFromObject(sagaData);
    saga = sagaService.createSagaRecordInDB(SagaEnum.MACRO_CREATE_SAGA.toString(), "Test",
      sagaPayload, UUID.fromString(macroID));
  }

  /**
   * After.
   */
  @After
  public void after() {
    sagaEventRepository.deleteAll();
    repository.deleteAll();
  }

  @Test
  public void testCreateMacro_givenEventAndSagaData_shouldPostEventToMacroApi() throws IOException, TimeoutException, InterruptedException {
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
      .eventType(EventType.INITIATED)
      .eventOutcome(EventOutcome.INITIATE_SUCCESS)
      .sagaId(saga.getSagaId())
      .macroId(macroID)
      .build();

    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(ArgumentMatchers.eq(TopicsEnum.MACRO_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    Assertions.assertThat(newEvent.getEventType()).isEqualTo(EventType.CREATE_MACRO);
    var macro = JsonUtil.getJsonObjectFromString(Macro.class, newEvent.getEventPayload());
    assertThat(macro.getBusinessUseTypeCode()).isEqualTo(sagaData.getBusinessUseTypeCode());
    assertThat(macro.getMacroTypeCode()).isEqualTo(sagaData.getMacroTypeCode());
    assertThat(macro.getMacroCode()).isEqualTo(sagaData.getMacroCode());
    assertThat(macro.getMacroText()).isEqualTo(sagaData.getMacroText());

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(EventType.CREATE_MACRO.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    Assertions.assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    Assertions.assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());
  }

  @Test
  public void testSendMacroCreateEmail_givenEventAndSagaData_shouldPostEventToEmailApi() throws IOException, InterruptedException, TimeoutException {
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var macroPayload = getMacroSagaDataFromJsonString();
    var event = Event.builder()
      .eventType(EventType.CREATE_MACRO)
      .eventOutcome(EventOutcome.MACRO_CREATED)
      .sagaId(saga.getSagaId())
      .macroId(macroID)
      .eventPayload(JsonUtil.getJsonStringFromObject(macroPayload))
      .build();

    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(ArgumentMatchers.eq(TopicsEnum.PROFILE_REQUEST_EMAIL_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    Assertions.assertThat(newEvent.getEventType()).isEqualTo(EventType.NOTIFY_MACRO_CREATE);
    var notificatonEvent = JsonUtil.getJsonObjectFromString(MacroEditNotificationEvent.class, newEvent.getEventPayload());
    var businessUseTypeCode = BusinessUseTypeCodes.valueOf(sagaData.getBusinessUseTypeCode());
    assertThat(notificatonEvent.getBusinessUseTypeName()).isEqualTo(businessUseTypeCode.getName());
    assertThat(notificatonEvent.getAppName()).isEqualTo(businessUseTypeCode.getApp());
    assertThat(notificatonEvent.getMacroTypeCode()).isEqualTo(sagaData.getMacroTypeCode());
    assertThat(notificatonEvent.getMacroCode()).isEqualTo(sagaData.getMacroCode());
    assertThat(notificatonEvent.getMacroText()).isEqualTo(sagaData.getMacroText());

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(EventType.NOTIFY_MACRO_CREATE.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates.size()).isEqualTo(1);
    Assertions.assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.CREATE_MACRO.toString());
    Assertions.assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.MACRO_CREATED.toString());
  }

  /**
   * Dummy macro saga data json string.
   *
   * @return the string
   */
  protected String placeholderMacroSagaData() {
    return " {\n" +
      "    \"createUser\": \"user\",\n" +
      "    \"updateUser\": \"user\",\n" +
      "    \"businessUseTypeCode\": \"PENREG\",\n" +
      "    \"macroCode\": \"hi\",\n" +
      "    \"macroTypeCode\": \"MERGE\",\n" +
      "    \"macroText\": \"hello\"\n" +
      "  }";
  }

  /**
   * Gets macro saga data from json string.
   *
   * @return the macro saga data from json string
   */
  protected Macro getMacroSagaDataFromJsonString() {
    try {
      return JsonUtil.getJsonObjectFromString(Macro.class, this.placeholderMacroSagaData());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
