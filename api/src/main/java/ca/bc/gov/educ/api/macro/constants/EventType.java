package ca.bc.gov.educ.api.macro.constants;

/**
 * The enum Event type.
 */
public enum EventType {
  /**
   * Initiated event type.
   */
  INITIATED,

  /**
   * Create macro event type.
   */
  CREATE_MACRO,

  /**
   * Update macro event type.
   */
  UPDATE_MACRO,

  /**
   * Notify macro create event type.
   */
  NOTIFY_MACRO_CREATE,

  /**
   * Notify macro update event type.
   */
  NOTIFY_MACRO_UPDATE,

  /**
   * Mark saga complete event type.
   */
  MARK_SAGA_COMPLETE,

}
