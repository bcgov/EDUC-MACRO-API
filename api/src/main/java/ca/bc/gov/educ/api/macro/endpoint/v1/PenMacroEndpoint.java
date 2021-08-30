package ca.bc.gov.educ.api.macro.endpoint.v1;

import ca.bc.gov.educ.api.macro.struct.v1.Macro;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.macro.constants.v1.URL.*;

@RequestMapping(BASE_URL + PEN_MACRO)
public interface PenMacroEndpoint {

  @GetMapping
  @PreAuthorize("hasAuthority('SCOPE_READ_PEN_MACRO')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<Macro> findMacros(@RequestParam(value = "businessUseTypeCode", required = false) String businessUseTypeCode, @RequestParam(value = "macroTypeCode", required = false) String macroTypeCode);

  @GetMapping(MACRO_ID)
  @PreAuthorize("hasAuthority('SCOPE_READ_PEN_MACRO')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  Macro findMacroById(@PathVariable UUID macroId);

  @PostMapping("/create-macro")
  @PreAuthorize("hasAuthority('SCOPE_WRITE_PEN_MACRO')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK.")})
  ResponseEntity<String> createMacro(@Validated @RequestBody Macro macro);

  @PostMapping("/update-macro")
  @PreAuthorize("hasAuthority('SCOPE_WRITE_PEN_MACRO')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  ResponseEntity<String> updateMacro(@Validated @RequestBody Macro macro);
}
