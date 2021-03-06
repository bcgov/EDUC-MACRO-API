package ca.bc.gov.educ.api.macro.repository;

import ca.bc.gov.educ.api.macro.model.MacroEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Macro event repository.
 */
@Repository
public interface MacroEventRepository extends JpaRepository<MacroEvent, UUID> {

  /**
   * Find by saga id and event type optional.
   *
   * @param sagaId    the saga id
   * @param eventType the event type
   * @return the optional
   */
  Optional<MacroEvent> findBySagaIdAndEventType(UUID sagaId, String eventType);

  /**
   * Find by event status list.
   *
   * @param status the status
   * @return the list
   */
  List<MacroEvent> findByEventStatus(String status);

  @Transactional
  @Modifying
  @Query("delete from MacroEvent where createDate <= :createDate")
  void deleteByCreateDateBefore(LocalDateTime createDate);
}
