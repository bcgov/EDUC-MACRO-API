package ca.bc.gov.educ.api.macro.repository;

import ca.bc.gov.educ.api.macro.model.Saga;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Saga repository.
 */
@Repository
public interface SagaRepository extends CrudRepository<Saga, UUID> {
  /**
   * Find all by status in list.
   *
   * @param statuses the statuses
   * @return the list
   */
  List<Saga> findAllByStatusIn(List<String> statuses);

  /**
   * Find all list.
   *
   * @return the list
   */
  @Override
  List<Saga> findAll();

  /**
   * Find by macro id optional.
   *
   * @param macroId the macro id
   * @param sagaName  the saga name
   * @return the optional Saga
   */
  Optional<Saga> findByMacroIdAndSagaName(UUID macroId, String sagaName);

  /**
   * Find by pen request batch student id and status
   *
   * @param macroId the macro id
   * @param sagaName  the saga name
   * @param statuses  the statuses
   * @return the list
   */
  List<Saga> findAllByMacroIdAndSagaNameAndStatusIn(UUID macroId, String sagaName, List<String> statuses);

  /**
   * Find all by create date before list.
   *
   * @param createDate the create date
   * @return the list
   */
  List<Saga> findAllByCreateDateBefore(LocalDateTime createDate);
}
