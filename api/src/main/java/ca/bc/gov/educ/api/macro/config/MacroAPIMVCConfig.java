package ca.bc.gov.educ.api.macro.config;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The type Macro api mvc config.
 *
 * @author Om
 */
@Configuration
public class MacroAPIMVCConfig implements WebMvcConfigurer {

  /**
   * The Macro api interceptor.
   */
  @Getter(AccessLevel.PRIVATE)
  private final RequestResponseInterceptor requestResponseInterceptor;

  /**
   * Instantiates a new Macro api mvc config.
   *
   * @param requestResponseInterceptor the pen reg api interceptor
   */
  @Autowired
  public MacroAPIMVCConfig(final RequestResponseInterceptor requestResponseInterceptor) {
    this.requestResponseInterceptor = requestResponseInterceptor;
  }

  /**
   * Add interceptors.
   *
   * @param registry the registry
   */
  @Override
  public void addInterceptors(final InterceptorRegistry registry) {
    registry.addInterceptor(this.requestResponseInterceptor).addPathPatterns("/**");
  }
}
