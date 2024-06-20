package ca.bc.gov.educ.api.macro.controller.v1;

import ca.bc.gov.educ.api.macro.MacroApiResourceApplication;
import ca.bc.gov.educ.api.macro.constants.SagaEnum;
import ca.bc.gov.educ.api.macro.constants.v1.URL;
import ca.bc.gov.educ.api.macro.mapper.v1.MacroMapper;
import ca.bc.gov.educ.api.macro.model.MacroEntity;
import ca.bc.gov.educ.api.macro.repository.MacroRepository;
import ca.bc.gov.educ.api.macro.repository.SagaEventRepository;
import ca.bc.gov.educ.api.macro.repository.SagaRepository;
import ca.bc.gov.educ.api.macro.service.MacroService;
import ca.bc.gov.educ.api.macro.service.SagaService;
import ca.bc.gov.educ.api.macro.struct.v1.Macro;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MacroApiResourceApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PenMacroControllerTest {

  private static final MacroMapper mapper = MacroMapper.mapper;
  @Autowired
  PenMacroController controller;

  @Autowired
  MacroService service;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  MacroRepository macroRepository;

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
    this.macroRepository.deleteAll();
    sagaEventRepository.deleteAll();
    repository.deleteAll();
  }

  @Test
  public void testRetrievePenMacros_ShouldReturnStatusOK() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.get(URL.BASE_URL + URL.PEN_MACRO)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_MACRO"))))
            .andDo(print()).andExpect(status().isOk());
  }

  @Test
  public void testRetrievePenMacros_GivenInvalidMacroID_ShouldReturnStatusNotFound() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.get(URL.BASE_URL + URL.PEN_MACRO + URL.MACRO_ID,UUID.randomUUID().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_MACRO"))))
            .andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testRetrievePenMacros_GivenMacroIDWithInvalidBusinessUseTypeCode_ShouldReturnStatusNotFound() throws Exception {
    MacroEntity savedEntity = createMacroEntities("OTHER");
    this.mockMvc.perform(MockMvcRequestBuilders.get(URL.BASE_URL + URL.PEN_MACRO + URL.MACRO_ID,savedEntity.getMacroId().toString())
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_MACRO"))))
      .andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testRetrievePenMacros_GivenValidMacroID_ShouldReturnStatusOK() throws Exception {
    MacroEntity savedEntity = createMacroEntities("GMP");
    final var result = this.mockMvc.perform(MockMvcRequestBuilders.get(URL.BASE_URL + URL.PEN_MACRO + URL.MACRO_ID, savedEntity.getMacroId().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_MACRO"))))
            .andDo(print()).andExpect(jsonPath("$.macroId").value(savedEntity.getMacroId().toString())).andExpect(status().isOk()).andReturn();
    assertThat(result).isNotNull();
  }

  @Test
  public void testRetrievePenMacros_GivenValidBusinessUseTypeCode_ShouldReturnStatusOK() throws Exception {
    MacroEntity savedEntity = createMacroEntities("GMP");
    final var result = this.mockMvc.perform(MockMvcRequestBuilders.get(URL.BASE_URL + URL.PEN_MACRO +"?businessUseTypeCode=" + savedEntity.getBusinessUseTypeCode())
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_MACRO"))))
      .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    assertThat(result).isNotNull();
  }

  @Test
  public void testRetrievePenMacros_GivenInvalidBusinessUseTypeCode_ShouldReturnStatusOK() throws Exception {
    MacroEntity savedEntity = createMacroEntities("OTHER");
    final var result = this.mockMvc.perform(MockMvcRequestBuilders.get(URL.BASE_URL + URL.PEN_MACRO +"?businessUseTypeCode=" + savedEntity.getBusinessUseTypeCode())
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_MACRO"))))
      .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    assertThat(result).isNotNull();
  }

  @Test
  public void testRetrievePenMacros_GivenValidBusinessUseTypeCodeAndMacroTypeCode_ShouldReturnStatusOK() throws Exception {
    MacroEntity savedEntity = createMacroEntities("GMP");
    final var result = this.mockMvc.perform(MockMvcRequestBuilders.get(URL.BASE_URL + URL.PEN_MACRO +"?businessUseTypeCode=" + savedEntity.getBusinessUseTypeCode() + "&macroTypeCode=" + savedEntity.getMacroTypeCode())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_MACRO"))))
            .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    assertThat(result).isNotNull();
  }

  @Test
  public void testRetrievePenMacros_GivenInvalidBusinessUseTypeCodeAndMacroTypeCode_ShouldReturnStatusOK() throws Exception {
    MacroEntity savedEntity = createMacroEntities("OTHER");
    final var result = this.mockMvc.perform(MockMvcRequestBuilders.get(URL.BASE_URL + URL.PEN_MACRO +"?businessUseTypeCode=" + savedEntity.getBusinessUseTypeCode() + "&macroTypeCode=" + savedEntity.getMacroTypeCode())
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_MACRO"))))
      .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    assertThat(result).isNotNull();
  }

  //create macro saga
  @Test
  public void testCreateMacro_GivenInvalidPayload_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.post(URL.BASE_URL + URL.PEN_MACRO + "/create-macro")
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_MACRO")))
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .content(placeholderInvalidSagaData()))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateMacro_GivenPayloadWithMacroId_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.post(URL.BASE_URL + URL.PEN_MACRO + "/create-macro")
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_MACRO")))
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .content(dummyMacroJsonWithId("PENREG")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateMacro_GivenPayloadWithInvalidBusinessUseTypeCode_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.post(URL.BASE_URL + URL.PEN_MACRO + "/create-macro")
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_MACRO")))
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .content(dummyMacroJson("OTHER")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateMacro_GivenValidPayload_ShouldReturnStatusOk() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.post(URL.BASE_URL + URL.PEN_MACRO + "/create-macro")
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_MACRO")))
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .content(dummyMacroJson("PENREG")))
      .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$").exists());
  }

  @Test
  public void testCreateMacro_GivenDuplicateMacroPayload_ShouldReturnStatusBadRequest() throws Exception {
    MacroEntity macroEntity = createMacroEntities("PENREG");
    this.macroRepository.save(macroEntity);

    this.mockMvc.perform(MockMvcRequestBuilders.post(URL.BASE_URL + URL.PEN_MACRO + "/create-macro")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_MACRO")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(dummyMacroJson("PENREG")))
        .andDo(print()).andExpect(status().isBadRequest());
  }

  //update macro saga
  @Test
  public void testUpdateMacro_GivenInvalidPayload_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.post(URL.BASE_URL + URL.PEN_MACRO + "/update-macro")
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_MACRO")))
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .content(placeholderInvalidSagaData()))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testUpdateMacro_GivenValidPayload_ShouldReturnStatusOk() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.post(URL.BASE_URL + URL.PEN_MACRO + "/update-macro")
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_MACRO")))
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .content(dummyMacroJsonWithId("PENREG")))
      .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$").exists());
  }

  @Test
  public void testUpdateMacro_GivenValidPayload_and_SagaWithSameMacroIdStarted_ShouldReturnStatusConflict() throws Exception {
    var payload = dummyMacroJsonWithId("PENREG");
    sagaService.createSagaRecordInDB(SagaEnum.MACRO_UPDATE_SAGA.toString(), "Test", payload, UUID.fromString(this.macroID));
    this.mockMvc.perform(MockMvcRequestBuilders.post(URL.BASE_URL + URL.PEN_MACRO + "/update-macro")
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_MACRO")))
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .content(payload))
      .andDo(print()).andExpect(status().isConflict());
  }

  protected String dummyMacroJson(String businessUseTypeCode) {
    return " {\n" +
            "    \"createUser\": \"user\",\n" +
            "    \"updateUser\": \"user\",\n" +
            "    \"businessUseTypeCode\": \"" + businessUseTypeCode + "\",\n" +
            "    \"macroCode\": \"hi\",\n" +
            "    \"macroTypeCode\": \"MERGE\",\n" +
            "    \"macroText\": \"hello\"\n" +
            "  }";
  }

  protected String dummyMacroJsonWithId(String businessUseTypeCode) {
    return " {\n" +
            "    \"createUser\": \"user\",\n" +
            "    \"updateUser\": \"user\",\n" +
            "    \"macroCode\": \"hi\",\n" +
            "    \"macroId\": \"" + this.macroID +"\",\n" +
            "    \"businessUseTypeCode\": \"" + businessUseTypeCode + "\",\n" +
            "    \"macroTypeCode\": \"MERGE\",\n" +
            "    \"macroText\": \"hello\"\n" +
            "  }";
  }

  protected String placeholderInvalidSagaData() {
    return " {\n" +
      "    \"createUser\": \"test\",\n" +
      "    \"updateUser\": \"test\"\n" +
      "  }";
  }

  protected Macro getMacroEntityFromJsonString(String businessUseTypeCode) {
    try {
      return new ObjectMapper().readValue(this.dummyMacroJson(businessUseTypeCode), Macro.class);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected MacroEntity createMacroEntities(String businessUseTypeCode) {
    val entity = mapper.toModel(this.getMacroEntityFromJsonString(businessUseTypeCode));
    entity.setMacroId(null);
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    return this.service.createMacro(entity);
  }
}
