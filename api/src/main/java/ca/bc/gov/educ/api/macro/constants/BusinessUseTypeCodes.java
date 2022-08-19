package ca.bc.gov.educ.api.macro.constants;

import lombok.Getter;

/**
 * The enum Business Use Type codes.
 */
@Getter
public enum BusinessUseTypeCodes {
  /**
   * GMP
   */
  GMP("GetMyPEN", "PEN Registry"),
  /**
   * UMP
   */
  UMP("UpdateMyPEN", "PEN Registry"),
  /**
   * PENREG
   */
  PENREG("PENRegistry", "PEN Registry"),
  /**
   * EDX
   */
  EDX("SecureExchange", "EDX");

  /**
   * The Name.
   */
  private final String name;

  /**
   * The App Name.
   */
  private final String app;

  /**
   * Instantiates a new code.
   *
   * @param name the name
   * @param app the app
   */
  BusinessUseTypeCodes(final String name, final String app) {
    this.name = name;
    this.app = app;
  }
}
