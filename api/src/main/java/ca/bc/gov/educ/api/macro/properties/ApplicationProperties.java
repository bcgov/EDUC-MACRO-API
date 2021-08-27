package ca.bc.gov.educ.api.macro.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class holds all application properties
 *
 * @author Marco Villeneuve
 */
@Component
@Getter
@Setter
public class ApplicationProperties {

  /**
   * The constant MACRO_API.
   */
  public static final String MACRO_API = "MACRO-API";
  public static final String CORRELATION_ID = "correlationID";

  /**
   * The Server.
   */
  @Value("${nats.server}")
  private String server;
  /**
   * The Max reconnect.
   */
  @Value("${nats.maxReconnect}")
  private int maxReconnect;
  /**
   * The Connection name.
   */
  @Value("${nats.connectionName}")
  private String connectionName;

  @Value("${app.email}")
  private String fromEmail;

  @Value("${helpdesk.email}")
  private String toEmail;
}
