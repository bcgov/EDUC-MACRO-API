package ca.bc.gov.educ.api.macro.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MacroEditNotificationEvent {
  private String fromEmail;
  private String toEmail;

  private String macroCode;
  private String macroText;
  private String macroTypeCode;
  private String businessUseTypeName;
  private String appName;
}

