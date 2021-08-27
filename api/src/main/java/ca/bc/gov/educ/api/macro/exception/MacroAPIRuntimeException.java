package ca.bc.gov.educ.api.macro.exception;

/**
 * The type Macro api runtime exception.
 */
public class MacroAPIRuntimeException extends RuntimeException {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 5241655513745148898L;

  /**
   * Instantiates a new Macro reg api runtime exception.
   *
   * @param message the message
   */
  public MacroAPIRuntimeException(String message) {
		super(message);
	}

}
