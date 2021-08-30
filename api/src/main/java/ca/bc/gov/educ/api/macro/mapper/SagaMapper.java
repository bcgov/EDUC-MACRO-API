package ca.bc.gov.educ.api.macro.mapper;

import ca.bc.gov.educ.api.macro.struct.v1.Saga;
import ca.bc.gov.educ.api.macro.struct.v1.SagaEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * The interface Saga mapper.
 */
@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface SagaMapper {
  /**
   * The constant mapper.
   */
  SagaMapper mapper = Mappers.getMapper(SagaMapper.class);

  /**
   * To struct saga.
   *
   * @param saga the saga
   * @return the saga struct
   */
  Saga toStruct(ca.bc.gov.educ.api.macro.model.Saga saga);


  /**
   * To model saga.
   *
   * @param struct the struct
   * @return the saga
   */
  ca.bc.gov.educ.api.macro.model.Saga toModel(Saga struct);

  @Mapping(target = "sagaId", source = "saga.sagaId")
  SagaEvent toEventStruct(ca.bc.gov.educ.api.macro.model.SagaEvent sagaEvent);
}
