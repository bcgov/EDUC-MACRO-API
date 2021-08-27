package ca.bc.gov.educ.api.macro.endpoint.v1;

import ca.bc.gov.educ.api.macro.struct.v1.Saga;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
}
