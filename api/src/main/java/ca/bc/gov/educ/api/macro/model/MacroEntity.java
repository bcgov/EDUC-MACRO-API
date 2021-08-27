package ca.bc.gov.educ.api.macro.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "MACRO")
public class MacroEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "MACRO_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID macroId;

  @Column(name = "MACRO_CODE")
  private String macroCode;

  @Column(name = "MACRO_TEXT")
  private String macroText;

  @Column(name = "MACRO_TYPE_CODE")
  private String macroTypeCode;

  @Column(name = "BUSINESS_USE_TYPE_CODE")
  private String businessUseTypeCode;

  @Column(name = "CREATE_USER", updatable = false)
  String createUser;

  @PastOrPresent
  @Column(name = "CREATE_DATE", updatable = false)
  LocalDateTime createDate;

  @Column(name = "UPDATE_USER")
  String updateUser;

  @PastOrPresent
  @Column(name = "UPDATE_DATE")
  LocalDateTime updateDate;
}
