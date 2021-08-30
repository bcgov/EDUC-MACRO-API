package ca.bc.gov.educ.api.macro.controller.v1;

import ca.bc.gov.educ.api.macro.endpoint.v1.MacroAPIEndpoint;
import ca.bc.gov.educ.api.macro.exception.InvalidParameterException;
import ca.bc.gov.educ.api.macro.filter.SagaFilterSpecs;
import ca.bc.gov.educ.api.macro.struct.v1.Saga;
import ca.bc.gov.educ.api.macro.mapper.SagaMapper;
import ca.bc.gov.educ.api.macro.service.SagaService;
import ca.bc.gov.educ.api.macro.struct.v1.SagaEvent;
import ca.bc.gov.educ.api.macro.struct.v1.Search;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

/**
 * The type Macro api controller.
 */
@RestController
@Slf4j
public class MacroAPIController extends PaginatedController implements MacroAPIEndpoint {
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
   * The saga filter specs.
   */
  @Getter(PRIVATE)
  private final SagaFilterSpecs sagaFilterSpecs;

  /**
   * Instantiates a new Macro api controller.
   *
   * @param sagaService   the saga service
   */
  @Autowired
  public MacroAPIController(final SagaService sagaService, final SagaFilterSpecs sagaFilterSpecs) {
    this.sagaService = sagaService;
    this.sagaFilterSpecs = sagaFilterSpecs;
  }

  @Override
  public ResponseEntity<Saga> readSaga(final UUID sagaID) {
    return this.getSagaService().findSagaById(sagaID)
      .map(sagaMapper::toStruct)
      .map(ResponseEntity::ok)
      .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  /**
   * Find all sagas completable future.
   *
   * @param pageNumber             the page number
   * @param pageSize               the page size
   * @param sortCriteriaJson       the sort criteria json
   * @param searchCriteriaListJson the search criteria list json
   * @return the completable future
   */
  @Override
  public CompletableFuture<Page<Saga>> findAllSagas(final Integer pageNumber, final Integer pageSize, final String sortCriteriaJson, final String searchCriteriaListJson) {
    final ObjectMapper objectMapper = new ObjectMapper();
    final List<Sort.Order> sorts = new ArrayList<>();
    Specification<ca.bc.gov.educ.api.macro.model.Saga> sagaEntitySpecification = null;
    try {
      final var associationNames = this.getSortCriteria(sortCriteriaJson, objectMapper, sorts);
      if (StringUtils.isNotBlank(searchCriteriaListJson)) {
        final List<Search> searches = objectMapper.readValue(searchCriteriaListJson, new TypeReference<>() {
        });
        this.getAssociationNamesFromSearchCriterias(associationNames, searches);
        int i = 0;
        for (final var search : searches) {
          sagaEntitySpecification = this.getSpecifications(sagaEntitySpecification, i, search, associationNames, this.getSagaFilterSpecs());
          i++;
        }

      }
    } catch (final JsonProcessingException e) {
      throw new InvalidParameterException(e.getMessage());
    }
    return this.getSagaService().findAll(sagaEntitySpecification, pageNumber, pageSize, sorts).thenApplyAsync(sagas -> sagas.map(sagaMapper::toStruct));
  }

  /**
   * Find all saga events for a given saga id
   *
   * @param sagaId - the saga id
   * @return - the list of saga events
   */
  @Override
  public ResponseEntity<List<SagaEvent>> getSagaEventsBySagaID(final UUID sagaId) {
    val sagaOptional = this.getSagaService().findSagaById(sagaId);
    return sagaOptional.map(saga -> ResponseEntity.ok(this.getSagaService().findAllSagaStates(saga).stream()
      .map(SagaMapper.mapper::toEventStruct).collect(Collectors.toList())))
      .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  /**
   * Update saga
   *
   * @param saga - the saga
   * @return - the updated saga
   */
  @Override
  @Transactional
  public ResponseEntity<Saga> updateSaga(final Saga saga, final UUID sagaId) {
    final var sagaOptional = this.getSagaService().findSagaById(sagaId);
    if (sagaOptional.isPresent()) {
      val sagaFromDB = sagaOptional.get();
      if (!sagaMapper.toStruct(sagaFromDB).getUpdateDate().equals(saga.getUpdateDate())) {
        log.error("Updating saga failed. The saga has already been updated by another process :: " + saga.getSagaId());
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
      }
      sagaFromDB.setPayload(saga.getPayload());
      sagaFromDB.setUpdateDate(LocalDateTime.now());
      this.getSagaService().updateSagaRecord(sagaFromDB);
      return ResponseEntity.ok(sagaMapper.toStruct(sagaFromDB));
    } else {
      log.error("Error attempting to get saga. Saga id does not exist :: " + saga.getSagaId());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

}
