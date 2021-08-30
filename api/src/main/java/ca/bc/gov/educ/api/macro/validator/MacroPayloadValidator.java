package ca.bc.gov.educ.api.macro.validator;

import ca.bc.gov.educ.api.macro.struct.v1.Macro;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Component
public class MacroPayloadValidator {
  public static final String BUSINESS_USE_TYPE_CODE = "businessUseTypeCode";

  public List<FieldError> validatePayload(Macro macro, boolean isCreateOperation, List<String> BusinessUseTypeCodes) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && macro.getMacroId() != null) {
      apiValidationErrors.add(createFieldError("macroId", macro.getMacroId(), "macroId should be null for post operation."));
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
