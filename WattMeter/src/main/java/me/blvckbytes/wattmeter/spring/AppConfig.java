package me.blvckbytes.wattmeter.spring;

import me.blvckbytes.wattmeter.spring.presentation.HBTemplateLoader;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

  /**
   * Append additional resource locations
   * @param registry Registry of resource handlers
   */
  public void addResourceHandlers( ResourceHandlerRegistry registry ) {
    // Set public/ as root for web paths (css, js, images)
    ResourceHandlerRegistration reg = registry.addResourceHandler( "/**" );
    reg.addResourceLocations(
      "file:///" + HBTemplateLoader.getLocation() + HBTemplateLoader.getFileSep() + "public" + HBTemplateLoader.getFileSep()
    );
  }

}
