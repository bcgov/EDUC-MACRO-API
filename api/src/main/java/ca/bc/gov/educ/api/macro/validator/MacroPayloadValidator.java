package ca.bc.gov.educ.api.macro.validator;

import ca.bc.gov.educ.api.macro.repository.*;
import ca.bc.gov.educ.api.macro.struct.v1.Macro;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Component
public class MacroPayloadValidator {

  private MacroRepository macroRepository;

  public static final String BUSINESS_USE_TYPE_CODE = "businessUseTypeCode";

  @Autowired
  public MacroPayloadValidator(MacroRepository macroRepository) {
    this.macroRepository = macroRepository;
  }

  public List<FieldError> validatePayload(Macro macro, boolean isCreateOperation, List<String> BusinessUseTypeCodes) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && macro.getMacroId() != null) {
      apiValidationErrors.add(createFieldError("macroId", macro.getMacroId(), "macroId should be null for post operation."));
    }
    if (isCreateOperation) {
      var macroList = macroRepository.findAllByBusinessUseTypeCodeAndMacroTypeCodeAndMacroCode(macro.getBusinessUseTypeCode(), macro.getMacroTypeCode(), macro.getMacroCode());
      if (!macroList.isEmpty()) {
        apiValidationErrors.add(createFieldError("uniqueConstraintBusinessUseTypeCodeAndMacroTypeCodeAndMacroCode", macro, "combination of BusinessUseTypeCode, MacroTypeCode and MacroCode already exists"));
      }
    }
    if(!BusinessUseTypeCodes.contains(macro.getBusinessUseTypeCode())) {
      apiValidationErrors.add(createFieldError(BUSINESS_USE_TYPE_CODE, macro.getBusinessUseTypeCode(), "businessUseTypeCode Invalid."));
    }
    return apiValidationErrors;
  }

  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError(Macro.class.getName(), fieldName, rejectedValue, false, null, null, message);
  }
}
