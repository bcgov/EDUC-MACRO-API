package ca.bc.gov.educ.api.macro.controller.v1;

import ca.bc.gov.educ.api.macro.constants.BusinessUseTypeCodes;
import ca.bc.gov.educ.api.macro.constants.SagaEnum;
import ca.bc.gov.educ.api.macro.endpoint.v1.PenMacroEndpoint;
import ca.bc.gov.educ.api.macro.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.macro.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.macro.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.macro.exception.errors.ApiError;
import ca.bc.gov.educ.api.macro.mapper.v1.MacroMapper;
import ca.bc.gov.educ.api.macro.model.MacroEntity;
import ca.bc.gov.educ.api.macro.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.macro.service.MacroService;
import ca.bc.gov.educ.api.macro.service.SagaService;
import ca.bc.gov.educ.api.macro.struct.v1.Macro;
import ca.bc.gov.educ.api.macro.util.JsonUtil;
import ca.bc.gov.educ.api.macro.util.RequestUtil;
import ca.bc.gov.educ.api.macro.validator.MacroPayloadValidator;
import ca.bc.gov.educ.api.macro.constants.SagaStatusEnum;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@Slf4j
public class PenMacroController implements PenMacroEndpoint {

  private static final MacroMapper mapper = MacroMapper.mapper;
  @Getter(PRIVATE)
  private final MacroService macroService;
  @Getter(PRIVATE)
  private final MacroPayloadValidator macroPayloadValidator;
  @Getter(PRIVATE)
  private final SagaService sagaService;
  @Getter(PRIVATE)
  private final Map<String, Orchestrator> orchestratorMap = new HashMap<>();

  @Autowired
  public PenMacroController(final MacroService macroService, final MacroPayloadValidator macroPayloadValidator,
                             final SagaService sagaService, final List<Orchestrator> orchestrators) {
    this.macroService = macroService;
    this.macroPayloadValidator = macroPayloadValidator;

    this.sagaService = sagaService;
    orchestrators.forEach(orchestrator -> this.orchestratorMap.put(orchestrator.getSagaName(), orchestrator));
    log.info("'{}' Saga Orchestrators are loaded.", String.join(",", this.orchestratorMap.keySet()));
  }

  @Override
  public List<Macro> findMacros(String businessUseTypeCode, String macroTypeCode) {
    List<MacroEntity> macroEntries = List.of();
    if (StringUtils.isNotBlank(businessUseTypeCode) && this.getBusinessUseTypeCodes().contains(businessUseTypeCode)) {
      if(StringUtils.isNotBlank(macroTypeCode)) {
        macroEntries = getMacroService().findMacrosByBusinessUseTypeCodeAndMacroTypeCode(businessUseTypeCode, macroTypeCode);
      } else {
        macroEntries =  getMacroService().findMacrosByBusinessUseTypeCode(businessUseTypeCode);
      }
    } else if (!StringUtils.isNotBlank(businessUseTypeCode) && StringUtils.isNotBlank(macroTypeCode)) {
      macroEntries = getMacroService().findMacrosByBusinessUseTypeCodeIn(this.getBusinessUseTypeCodes());
    }
    return macroEntries.stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public Macro findMacroById(UUID macroId) {
    val result = getMacroService().getMacro(macroId);
    if (result.isPresent()) {
      final var businessUseTypeCode = result.get().getBusinessUseTypeCode();
      if(this.getBusinessUseTypeCodes().contains(businessUseTypeCode)) {
        return mapper.toStructure(result.get());
      }
    }
    throw new EntityNotFoundException(Macro.class, "macroId", macroId.toString());
  }

  @Override
  public ResponseEntity<String> createMacro(Macro macro) {
    validatePayload(macro, true);
    RequestUtil.setAuditColumnsForCreate(macro);
    return this.processMacroSaga(SagaEnum.MACRO_CREATE_SAGA, macro);
  }

  @Override
  public ResponseEntity<String> updateMacro(Macro macro) {
    validatePayload(macro, false);
    RequestUtil.setAuditColumnsForUpdate(macro);
    return this.processMacroSaga(SagaEnum.MACRO_UPDATE_SAGA, macro);
  }

  private void validatePayload(Macro penRequestMacro, boolean isCreateOperation) {
    val validationResult = getMacroPayloadValidator().validatePayload(penRequestMacro, isCreateOperation, this.getBusinessUseTypeCodes());
    if (!validationResult.isEmpty()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Payload contains invalid data.").status(BAD_REQUEST).build();
      error.addValidationErrors(validationResult);
      throw new InvalidPayloadException(error);
    }
  }

  /**
   * Process saga.
   *
   * @param sagaName the saga name
   * @param sagaData the saga data
   * @return the response entity
   */
  private ResponseEntity<String> processMacroSaga(final SagaEnum sagaName, final Macro sagaData) {
    try {
      UUID macroId = null;
      if(StringUtils.isNotBlank(sagaData.getMacroId())) {
        macroId = UUID.fromString(sagaData.getMacroId());
        final var sagaInProgress = this.getSagaService().findAllByMacroIdAndStatusIn(macroId, sagaName.toString(), this.getStatusesFilter());
        if (!sagaInProgress.isEmpty()) {
          return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
      }

      final String payload = JsonUtil.getJsonStringFromObject(sagaData);
      final var saga = this.getOrchestratorMap()
        .get(sagaName.toString())
        .startSaga(payload, macroId, sagaData.getCreateUser());
      return ResponseEntity.ok(saga.getSagaId().toString());
    } catch (final Exception e) {
      Thread.currentThread().interrupt();
      throw new SagaRuntimeException(e.getMessage());
    }
  }

  /**
   * Gets statuses filter.
   *
   * @return the statuses filter
   */
  private List<String> getStatusesFilter() {
    final var statuses = new ArrayList<String>();
    statuses.add(SagaStatusEnum.IN_PROGRESS.toString());
    statuses.add(SagaStatusEnum.STARTED.toString());
    return statuses;
  }

  /**
   * Gets business use type codes.
   *
   * @return the business use type codes
   */
  private List<String> getBusinessUseTypeCodes() {
    return List.of(BusinessUseTypeCodes.GMP.toString(), BusinessUseTypeCodes.UMP.toString(), BusinessUseTypeCodes.PENREG.toString(), BusinessUseTypeCodes.EDX.toString());
  }
}
