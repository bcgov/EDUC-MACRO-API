package ca.bc.gov.educ.api.macro.controller.v1;

import ca.bc.gov.educ.api.macro.MacroApiResourceApplication;
import ca.bc.gov.educ.api.macro.constants.SagaEnum;
import ca.bc.gov.educ.api.macro.constants.v1.URL;
import ca.bc.gov.educ.api.macro.repository.SagaEventRepository;
import ca.bc.gov.educ.api.macro.service.SagaService;
import ca.bc.gov.educ.api.macro.repository.SagaRepository;
import lombok.extern.slf4j.Slf4j;
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

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "MACRO_READ_SAGA")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testReadSaga_GivenValidID_ShouldReturnStatusOK() throws Exception {
    var payload = placeholderMacroSagaData();
    var sagaFromDB = sagaService.createSagaRecordInDB(SagaEnum.MACRO_CREATE_SAGA.toString(), "Test", payload, UUID.fromString(macroID));

    this.mockMvc.perform(get(URL.BASE_URL + "/saga/" + sagaFromDB.getSagaId().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "MACRO_READ_SAGA")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andExpect(status().isOk())
            .andExpect(jsonPath("$.sagaId").value(sagaFromDB.getSagaId().toString()));
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

}
