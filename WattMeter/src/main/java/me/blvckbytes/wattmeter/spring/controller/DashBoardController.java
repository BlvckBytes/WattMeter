package me.blvckbytes.wattmeter.spring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Template;
import me.blvckbytes.wattmeter.spring.presentation.HBTemplateLoader;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping( value = { "/" } )
public class DashBoardController {

  private final HBTemplateLoader templateLoader;

  /**
   * Controlls all requests corresponding to the ambulance book page and its processes
   * @param templateLoader Dependency injection for the Handlebars loader
   */
  public DashBoardController( HBTemplateLoader templateLoader ) {
    this.templateLoader = templateLoader;
  }

  /**
   * Renders the dashboard page
   * @param request Request from user
   * @return Rendered HTML web page
   * @throws Exception Stuff like json parse error or templating problems
   */
  @RequestMapping( method = RequestMethod.GET, produces = "text/html" )
  public Object getAmbulanceBookPage( HttpServletRequest request ) throws Exception {
    Template template = templateLoader.getTemplate( "dashboard" );

    // Just a debug, to check handlebars
    JSONObject buf = new JSONObject();
    buf.put( "time", System.currentTimeMillis() );

    // Apply template based on json values
    return template.apply( templateLoader.getContext( new ObjectMapper().readTree( buf.toString() ) ) );
  }
}
