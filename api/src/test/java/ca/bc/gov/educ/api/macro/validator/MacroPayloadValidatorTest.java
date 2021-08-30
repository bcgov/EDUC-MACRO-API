package ca.bc.gov.educ.api.macro.validator;

import ca.bc.gov.educ.api.macro.MacroApiResourceApplication;
import ca.bc.gov.educ.api.macro.constants.BusinessUseTypeCodes;
import ca.bc.gov.educ.api.macro.struct.v1.Macro;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MacroApiResourceApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class MacroPayloadValidatorTest {
  @InjectMocks
  MacroPayloadValidator macroPayloadValidator;

  List<String> businessUseTypeCodes = List.of(BusinessUseTypeCodes.GMP.toString(), BusinessUseTypeCodes.UMP.toString(), BusinessUseTypeCodes.PENREG.toString());

  @Before
  public void before() {
    this.macroPayloadValidator = new MacroPayloadValidator();
  }

  @Test
  public void testValidatePayload_WhenMacroIdGivenForPost_ShouldAddAnErrorTOTheReturnedList() {
    val errorList = this.macroPayloadValidator.validatePayload(this.getMacroEntityFromJsonString(), true, businessUseTypeCodes);
    assertEquals(2, errorList.size());
    assertEquals("macroId should be null for post operation.", errorList.get(0).getDefaultMessage());
  }
  @Test
  public void testValidatePayload_WhenBusinessUseTypeCodeIsInvalid_ShouldAddAnErrorTOTheReturnedList() {
    val entity = this.getMacroEntityFromJsonString();
    entity.setMacroId(null);
    val errorList = this.macroPayloadValidator.validatePayload(entity, true, businessUseTypeCodes);
    assertEquals(1, errorList.size());
    assertEquals("businessUseTypeCode Invalid.", errorList.get(0).getDefaultMessage());
  }

  protected String dummyMacroJson() {
    return " {\n" +
            "    \"createUser\": \"user\",\n" +
            "    \"updateUser\": \"user\",\n" +
            "    \"macroId\": \"7f000101-7151-1d84-8171-5187006c0000\",\n" +
            "    \"businessUseTypeCode\": \"TEST\",\n" +
            "    \"macroCode\": \"hi\",\n" +
            "    \"macroTypeCode\": \"MERGE\",\n" +
            "    \"macroText\": \"hello\"\n" +
            "  }";
  }

  protected Macro getMacroEntityFromJsonString() {
    try {
      return new ObjectMapper().readValue(this.dummyMacroJson(), Macro.class);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
