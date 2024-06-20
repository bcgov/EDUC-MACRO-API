package ca.bc.gov.educ.api.macro.repository;

import ca.bc.gov.educ.api.macro.model.MacroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface MacroRepository extends JpaRepository<MacroEntity, UUID> {

  List<MacroEntity> findAllByBusinessUseTypeCodeAndMacroTypeCode(String businessUseTypeCode, String macroTypeCode);

  List<MacroEntity> findAllByBusinessUseTypeCodeAndMacroTypeCodeAndMacroCode(String businessUseTypeCode, String macroTypeCode, String macroCode);

  List<MacroEntity> findAllByBusinessUseTypeCode(String businessUseTypeCode);

  List<MacroEntity> findAllByBusinessUseTypeCodeIn(Collection<String> businessUseTypeCodes);
}
