package me.blvckbytes.wattmeter.spring.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.JsonNodeValueResolver;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.cache.GuavaTemplateCache;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import me.blvckbytes.wattmeter.WattMeterApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class HBTemplateLoader {

  @Getter
  private Handlebars hbs;

  @Getter
  private static String location;

  @Getter
  private static char fileSep;

  static {
    ApplicationHome home = new ApplicationHome( WattMeterApplication.class );
    fileSep = File.separatorChar;
    location = home.getDir().getPath();

    // Get to the correct location
    location = location.substring( 0, location.lastIndexOf( fileSep ));
    location = location.replace( fileSep + "target", fileSep + "src" + fileSep + "main" + fileSep + "resources" );
  }

  /**
   * Load all templates from a known folder and create a
   * cache for them.
   */
  @PostConstruct
  public void loadTemplates() {
    // Initialize template loader on resources/views path for .hbs files
    TemplateLoader loader = new FileTemplateLoader( location + "/views", ".hbs" );
    TemplateLoader partials = new FileTemplateLoader( location + "/views/partials", ".hbs" );

    // Set up the handlebars instance with cache bound
    this.hbs = new Handlebars().with( loader, partials );
  }

  /**
   * Get a compiled template from handlebars
   * @param name Name of template
   * @return Compiled template
   *
   * @throws IOException When file not available
   */
  public Template getTemplate( String name ) throws IOException {
    return this.hbs.compile( name );
  }

  /**
   * Get the context for this template engine
   * @param model Parsed json to render into template
   * @return Built context
   */
  public Context getContext( JsonNode model ) {
    // Initialize a new context builder
    return Context.newBuilder( model ).resolver( JsonNodeValueResolver.INSTANCE ).build();
  }
}
