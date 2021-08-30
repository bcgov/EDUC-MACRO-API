package ca.bc.gov.educ.api.macro.constants;

/**
 * The enum Event outcome.
 */
public enum EventOutcome {
  /**
   * Initiate success event outcome.
   */
  INITIATE_SUCCESS,

  /**
   * Macro created event outcome.
   */
  MACRO_CREATED,
  /**
   * Macro updated event outcome.
   */
  MACRO_UPDATED,
  /**
   * Macro not found event outcome.
   */
  MACRO_NOT_FOUND,
  /**
   * notified event outcome.
   */
  NOTIFIED,
  /**
   * Saga completed event outcome.
   */
  SAGA_COMPLETED
  }
