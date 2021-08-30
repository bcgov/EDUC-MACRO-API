package ca.bc.gov.educ.api.macro.endpoint.v1;

import ca.bc.gov.educ.api.macro.struct.v1.Saga;
import ca.bc.gov.educ.api.macro.struct.v1.SagaEvent;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static ca.bc.gov.educ.api.macro.constants.v1.URL.BASE_URL;

/**
 * The interface Macro api endpoint.
 */
@RequestMapping(BASE_URL)
@OpenAPIDefinition(info = @Info(title = "API for Macros.", description = "This API is for macro management.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_PEN_MACRO", "WRITE_PEN__MACRO"})})
public interface MacroAPIEndpoint {
  /**
   * Read saga response entity.
   *
   * @param sagaID the saga id
   * @return the response entity
   */
  @GetMapping("/saga/{sagaID}")
  @PreAuthorize("hasAuthority('SCOPE_MACRO_READ_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK."), @ApiResponse(responseCode = "404", description = "Not Found.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to retrieve saga by its ID (GUID).", description = "Endpoint to retrieve saga by its ID (GUID).")
  ResponseEntity<Saga> readSaga(@PathVariable UUID sagaID);

  /**
   * Find all Sagas for given search criteria.
   *
   * @param pageNumber             the page number
   * @param pageSize               the page size
   * @param sortCriteriaJson       the sort criteria json
   * @param searchCriteriaListJson the search list , the JSON string ( of Array or List of {@link ca.bc.gov.educ.api.macro.struct.v1.Search})
   * @return the completable future Page {@link Saga}
   */
  @GetMapping("/saga/paginated")
  @PreAuthorize("hasAuthority('SCOPE_MACRO_READ_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to support data table view in frontend, with sort, filter and pagination, for Sagas.", description = "This API endpoint exposes flexible way to query the entity by leveraging JPA specifications.")
  CompletableFuture<Page<Saga>> findAllSagas(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                             @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson,
                                             @ArraySchema(schema = @Schema(name = "searchCriteriaList",
                                               description = "searchCriteriaList if provided should be a JSON string of Search Array",
                                               implementation = ca.bc.gov.educ.api.macro.struct.v1.Search.class))
                                             @RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson);

  @GetMapping("/saga/{sagaId}/saga-events")
  @PreAuthorize("hasAuthority('SCOPE_MACRO_READ_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK."), @ApiResponse(responseCode = "404", description = "Not Found.")})
  @Transactional(readOnly = true)
  @Tag(name = "Endpoint to retrieve all saga events by its ID (GUID).", description = "Endpoint to retrieve all saga events by its ID (GUID).")
  ResponseEntity<List<SagaEvent>> getSagaEventsBySagaID(@PathVariable UUID sagaId);

  @PutMapping("/saga/{sagaId}")
  @PreAuthorize("hasAuthority('SCOPE_MACRO_WRITE_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK."), @ApiResponse(responseCode = "404", description = "Not Found."), @ApiResponse(responseCode = "409", description = "Conflict.")})
  @Transactional
  @Tag(name = "Endpoint to update saga by its ID.", description = "Endpoint to update saga by its ID.")
  ResponseEntity<Saga> updateSaga(@RequestBody Saga saga, @PathVariable UUID sagaId);
}
