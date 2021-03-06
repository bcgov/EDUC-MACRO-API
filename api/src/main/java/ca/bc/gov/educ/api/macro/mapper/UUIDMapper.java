package ca.bc.gov.educ.api.macro.mapper;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * The type Uuid mapper.
 */
public class UUIDMapper {

  /**
   * Map uuid.
   *
   * @param value the value
   * @return the uuid
   */
  public UUID map(final String value) {
    if (StringUtils.isBlank(value)) {
      return null;
    }
    return UUID.fromString(value);
  }

  /**
   * Map string.
   *
   * @param value the value
   * @return the string
   */
  public String map(final UUID value) {
    if (value == null) {
      return null;
    }
    return value.toString();
  }
}
