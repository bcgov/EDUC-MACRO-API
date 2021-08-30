package ca.bc.gov.educ.api.macro.mapper.v1;

import ca.bc.gov.educ.api.macro.mapper.LocalDateTimeMapper;
import ca.bc.gov.educ.api.macro.mapper.UUIDMapper;
import ca.bc.gov.educ.api.macro.model.MacroEntity;
import ca.bc.gov.educ.api.macro.struct.v1.Macro;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface MacroMapper {

  MacroMapper mapper = Mappers.getMapper(MacroMapper.class);

  Macro toStructure(MacroEntity entity);

  MacroEntity toModel(Macro struct);
}
