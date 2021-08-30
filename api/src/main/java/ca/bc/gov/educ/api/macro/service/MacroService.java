package ca.bc.gov.educ.api.macro.service;

import ca.bc.gov.educ.api.macro.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.macro.model.MacroEntity;
import ca.bc.gov.educ.api.macro.repository.MacroRepository;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Service
public class MacroService {
  @Getter(PRIVATE)
  private final MacroRepository macroRepository;

  @Autowired
  public MacroService(MacroRepository macroRepository) {
    this.macroRepository = macroRepository;
  }

  public List<MacroEntity> findAllMacros() {
    return getMacroRepository().findAll();
  }

  public Optional<MacroEntity> getMacro(UUID macroId) {
    return getMacroRepository().findById(macroId);
  }

  public List<MacroEntity> findMacrosByBusinessUseTypeCodeAndMacroTypeCode(String businessUseTypeCode, String macroTypeCode) {
    return getMacroRepository().findAllByBusinessUseTypeCodeAndMacroTypeCode(businessUseTypeCode, macroTypeCode);
  }

  public List<MacroEntity> findMacrosByBusinessUseTypeCode(String businessUseTypeCode) {
    return getMacroRepository().findAllByBusinessUseTypeCode(businessUseTypeCode);
  }

  public List<MacroEntity> findMacrosByBusinessUseTypeCodeIn(Collection<String> businessUseTypeCodes) {
    return getMacroRepository().findAllByBusinessUseTypeCodeIn(businessUseTypeCodes);
  }

  public MacroEntity createMacro(MacroEntity entity) {
    return getMacroRepository().save(entity);
  }

  public MacroEntity updateMacro(UUID macroId, MacroEntity entity) {
    val result = getMacroRepository().findById(macroId);
    if (result.isPresent()) {
      return getMacroRepository().save(entity);
    } else {
      throw new EntityNotFoundException(entity.getClass(),"macroId", macroId.toString());
    }
  }
}
