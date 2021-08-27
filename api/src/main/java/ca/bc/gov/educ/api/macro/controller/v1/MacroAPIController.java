package ca.bc.gov.educ.api.macro.controller.v1;

import ca.bc.gov.educ.api.macro.endpoint.v1.MacroAPIEndpoint;
import ca.bc.gov.educ.api.macro.struct.v1.Saga;
import ca.bc.gov.educ.api.macro.mapper.SagaMapper;
import ca.bc.gov.educ.api.macro.service.SagaService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

/**
 * The type Macro api controller.
 */
@RestController
@Slf4j
public class MacroAPIController implements MacroAPIEndpoint {
  /**
   * The constant sagaMapper.
   */
  private static final SagaMapper sagaMapper = SagaMapper.mapper;
  /**
   * The Saga service.
   */
  @Getter(PRIVATE)
  private final SagaService sagaService;

  /**
   * Instantiates a new Macro api controller.
   *
   * @param sagaService   the saga service
   */
  @Autowired
  public MacroAPIController(final SagaService sagaService) {
    this.sagaService = sagaService;
  }

  @Override
  public ResponseEntity<Saga> readSaga(final UUID sagaID) {
    return this.getSagaService().findSagaById(sagaID)
      .map(sagaMapper::toStruct)
      .map(ResponseEntity::ok)
      .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

}
