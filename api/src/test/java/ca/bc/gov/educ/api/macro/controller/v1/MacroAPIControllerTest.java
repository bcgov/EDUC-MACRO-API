package ca.bc.gov.educ.api.macro.controller.v1;

import ca.bc.gov.educ.api.macro.MacroApiResourceApplication;
import ca.bc.gov.educ.api.macro.constants.SagaEnum;
import ca.bc.gov.educ.api.macro.constants.v1.URL;
import ca.bc.gov.educ.api.macro.filter.FilterOperation;
import ca.bc.gov.educ.api.macro.filter.ValueType;
import ca.bc.gov.educ.api.macro.mapper.SagaMapper;
import ca.bc.gov.educ.api.macro.mapper.v1.MacroMapper;
import ca.bc.gov.educ.api.macro.repository.SagaEventRepository;
import ca.bc.gov.educ.api.macro.service.SagaService;
import ca.bc.gov.educ.api.macro.repository.SagaRepository;
import ca.bc.gov.educ.api.macro.struct.v1.Saga;
import ca.bc.gov.educ.api.macro.struct.v1.Search;
import ca.bc.gov.educ.api.macro.struct.v1.SearchCriteria;
import ca.bc.gov.educ.api.macro.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.macro.constants.SagaEnum.MACRO_CREATE_SAGA;
import static ca.bc.gov.educ.api.macro.filter.Condition.AND;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The Macro api controller tests
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {MacroApiResourceApplication.class})
@AutoConfigureMockMvc
@Slf4j
public class MacroAPIControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  PenMacroController controller;

  @Autowired
  SagaRepository repository;

  @Autowired
  SagaEventRepository sagaEventRepository;

  @Autowired
  SagaService sagaService;

  private final String macroID = "7f000101-7151-1d84-8171-5187006c0001";

  private static final SagaMapper mapper = SagaMapper.mapper;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @After
  public void after() {
    sagaEventRepository.deleteAll();
    repository.deleteAll();
  }

  @Test
  public void testReadSaga_GivenInValidID_ShouldReturnStatusNotFound() throws Exception {
    this.mockMvc.perform(get(URL.BASE_URL + "/saga/" + UUID.randomUUID().toString())
            .with(jwt().jwt(jwt -> jwt.claim("scope", "MACRO_READ_SAGA")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testReadSaga_GivenValidID_ShouldReturnStatusOK() throws Exception {
    var payload = placeholderMacroSagaData();
    var sagaFromDB = sagaService.createSagaRecordInDB(SagaEnum.MACRO_CREATE_SAGA.toString(), "Test", payload, UUID.fromString(macroID));

    this.mockMvc.perform(get(URL.BASE_URL + "/saga/" + sagaFromDB.getSagaId().toString())
            .with(jwt().jwt(jwt -> jwt.claim("scope", "MACRO_READ_SAGA")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(status().isOk())
            .andExpect(jsonPath("$.sagaId").value(sagaFromDB.getSagaId().toString()));
  }

  @Test
  @SuppressWarnings("java:S100")
  public void testGetSagaPaginated_givenNoSearchCriteria_shouldReturnAllWithStatusOk() throws Exception {
    final File file = new File(
      Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_multiple_sagas.json")).getFile()
    );
    final List<Saga> sagaStructs = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    final List<ca.bc.gov.educ.api.macro.model.Saga> sagaEntities = sagaStructs.stream().map(mapper::toModel).collect(Collectors.toList());

    for (val saga : sagaEntities) {
      saga.setSagaId(null);
      saga.setCreateDate(LocalDateTime.now());
      saga.setUpdateDate(LocalDateTime.now());
    }
    this.repository.saveAll(sagaEntities);
    final MvcResult result = this.mockMvc
      .perform(get("/api/v1/macro/saga/paginated")
        .with(jwt().jwt(jwt -> jwt.claim("scope", "MACRO_READ_SAGA")))
        .contentType(APPLICATION_JSON))
      .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(3)));
  }

  @Test
  @SuppressWarnings("java:S100")
  public void testGetSagaPaginated_givenNoData_shouldReturnStatusOk() throws Exception {
    final MvcResult result = this.mockMvc
      .perform(get("/api/v1/macro/saga/paginated")
        .with(jwt().jwt(jwt -> jwt.claim("scope", "MACRO_READ_SAGA")))
        .contentType(APPLICATION_JSON))
      .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
  }

  @Test
  @SuppressWarnings("java:S100")
  public void testGetSagaPaginated_givenSearchCriteria_shouldReturnStatusOk() throws Exception {
    final File file = new File(
      Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_multiple_sagas.json")).getFile()
    );
    final List<Saga> sagaStructs = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    final List<ca.bc.gov.educ.api.macro.model.Saga> sagaEntities = sagaStructs.stream().map(mapper::toModel).collect(Collectors.toList());

    for (val saga : sagaEntities) {
      saga.setSagaId(null);
      saga.setCreateDate(LocalDateTime.now());
      saga.setUpdateDate(LocalDateTime.now());
    }
    this.repository.saveAll(sagaEntities);

    final SearchCriteria criteria = SearchCriteria.builder().key("sagaState").operation(FilterOperation.IN).value("IN_PROGRESS").valueType(ValueType.STRING).build();
    final SearchCriteria criteria2 = SearchCriteria.builder().key("sagaName").condition(AND).operation(FilterOperation.EQUAL).value("MACRO_CREATE_SAGA").valueType(ValueType.STRING).build();
    final List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    criteriaList.add(criteria2);
    final List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaList).build());
    final ObjectMapper objectMapper = new ObjectMapper();
    final String criteriaJSON = objectMapper.writeValueAsString(searches);

    final MvcResult result = this.mockMvc
      .perform(get("/api/v1/macro/saga/paginated")
        .with(jwt().jwt(jwt -> jwt.claim("scope", "MACRO_READ_SAGA")))
        .param("searchCriteriaList", criteriaJSON)
        .contentType(APPLICATION_JSON))
      .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  @SuppressWarnings("java:S100")
  public void testReadSagaEvents_givenSagaDoesntExist_shouldReturnStatusNotFound() throws Exception {
    this.mockMvc.perform(get("/api/v1/macro/saga/{sagaId}/saga-events", UUID.randomUUID())
      .with(jwt().jwt(jwt -> jwt.claim("scope", "MACRO_READ_SAGA")))
      .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  @SuppressWarnings("java:S100")
  public void testGetSagaEventsBySagaID_whenSagaIDIsValid_shouldReturnStatusOk() throws Exception {
    final File sagEventsFile = new File(
      Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock-saga-events.json")).getFile()
    );
    final File sagFile = new File(
      Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock-saga.json")).getFile()
    );
    val sagaEvents = Arrays.asList(objectMapper.readValue(sagEventsFile, ca.bc.gov.educ.api.macro.model.SagaEvent[].class));
    val saga = objectMapper.readValue(sagFile, ca.bc.gov.educ.api.macro.model.Saga.class);
    saga.setSagaId(null);
    saga.setCreateDate(LocalDateTime.now());
    saga.setUpdateDate(LocalDateTime.now());
    this.repository.save(saga);
    for (val sagaEvent : sagaEvents) {
      sagaEvent.setSaga(saga);
      sagaEvent.setCreateDate(LocalDateTime.now());
      sagaEvent.setUpdateDate(LocalDateTime.now());
    }
    this.sagaEventRepository.saveAll(sagaEvents);
    this.mockMvc.perform(get("/api/v1/macro/saga/{sagaId}/saga-events", saga.getSagaId())
      .with(jwt().jwt(jwt -> jwt.claim("scope", "MACRO_READ_SAGA"))))
      .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)));
  }

  @Test
  public void testUpdateSaga_givenNoBody_shouldReturn400() throws Exception {
    this.mockMvc.perform(put("/api/v1/macro/saga/{sagaId}", UUID.randomUUID())
      .with(jwt().jwt(jwt -> jwt.claim("scope", "MACRO_WRITE_SAGA")))
      .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testUpdateSaga_givenInvalidID_shouldReturn404() throws Exception {
    val saga = createMockSaga();
    this.mockMvc.perform(put("/api/v1/macro/saga/{sagaId}", UUID.randomUUID()).content(objectMapper.writeValueAsBytes(saga))
      .with(jwt().jwt(jwt -> jwt.claim("scope", "MACRO_WRITE_SAGA")))
      .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testUpdateSaga_givenPastUpdateDate_shouldReturn409() throws Exception {
    final var sagaFromDB = this.sagaService.createSagaRecordInDB(MACRO_CREATE_SAGA.toString(), "Test", "Test", UUID.fromString(this.macroID));
    sagaFromDB.setUpdateDate(LocalDateTime.now());
    this.mockMvc.perform(put("/api/v1/macro/saga/{sagaId}", sagaFromDB.getSagaId()).content(objectMapper.writeValueAsBytes(mapper.toStruct(sagaFromDB)))
      .with(jwt().jwt(jwt -> jwt.claim("scope", "MACRO_WRITE_SAGA")))
      .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isConflict());
  }

  @Test
  public void testUpdateSaga_givenValidData_shouldReturnOk() throws Exception {
    final var sagaFromDB = this.sagaService.createSagaRecordInDB(MACRO_CREATE_SAGA.toString(), "Test", "Test", UUID.fromString(this.macroID));
    sagaFromDB.setUpdateDate(sagaFromDB.getUpdateDate().withNano((int)Math.round(sagaFromDB.getUpdateDate().getNano()/1000.00)*1000)); //db limits precision, so need to adjust
    this.mockMvc.perform(put("/api/v1/macro/saga/{sagaId}", sagaFromDB.getSagaId()).content(objectMapper.writeValueAsBytes(mapper.toStruct(sagaFromDB)))
      .with(jwt().jwt(jwt -> jwt.claim("scope", "MACRO_WRITE_SAGA")))
      .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isOk());
  }

  protected String placeholderMacroSagaData() {
    return " {\n" +
            "    \"createUser\": \"test\",\n" +
            "    \"updateUser\": \"test\",\n" +
            "    \"businessUseTypeCode\": \"PENREG\",\n" +
            "    \"macroCode\": \"hi\",\n" +
            "    \"macroTypeCode\": \"MERGE\",\n" +
            "    \"macroText\": \"hello\"\n" +
            "  }";
  }

  private Saga createMockSaga() {
    return Saga.builder().sagaId(UUID.randomUUID()).payload("test").updateDate(LocalDateTime.now().toString()).build();
  }

}
