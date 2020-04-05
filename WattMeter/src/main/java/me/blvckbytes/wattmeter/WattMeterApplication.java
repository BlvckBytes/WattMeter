package me.blvckbytes.wattmeter;

import me.blvckbytes.wattmeter.communication.*;
import me.blvckbytes.wattmeter.meter.WattMeter;
import me.blvckbytes.wattmeter.utils.PropConfig;
import me.blvckbytes.wattmeter.utils.SLLevel;
import me.blvckbytes.wattmeter.utils.SimpleLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class WattMeterApplication {

  public static String path;

  // Format path to proper standard value
  static {
    try {
      path = WattMeterApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();

      // Remove file if exists
      if( path.endsWith( ".jar" ) || path.endsWith( "/" ) )
        path = path.substring( 0, path.lastIndexOf( "/" ) );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  /**
   * Main entry point of this program
   */
  public static void main( String[] args ) {
    // Launch watt meter
    if( initializeWattMeter() ) {
      // Only launch spring if watt meter could be started successfully
      SpringApplication.run( WattMeterApplication.class, args );
    }
  }

  /**
   * Initialize the wattmeter with its database and it's communication link
   */
  private static boolean initializeWattMeter() {
    try {
      // Read out interval from config
      Properties props = PropConfig.getInstance().getProps();
      int interval = Integer.parseInt( props.getProperty( "meter_interval" ) );
      TimeUnit intervalUnit = TimeUnit.valueOf( props.getProperty( "meter_interval_unit" ) );
      ConnectionType connType = ConnectionType.valueOf( props.getProperty( "conn_type" ) );

      // Create communication link based on config property
      CommunicationLink link = null;

      // Switch on provided conn-type
      switch( connType ) {

        // Connection over usb
        case USB:
          link = new SerialInterface( props.getProperty( "usb_portname" ) );
          break;

        // Connection over socket
        case SOCKET:
          String[] connData = props.getProperty( "socket_connstr" ).split( ":" );

          // IP and port, otherwise format is corrupted
          if( connData.length != 2 )
            throw new Exception( "Wrong input format on socket_connstr value in config!" );

          // Try to parse given data to create a new socket interface
          try {
            link = new SocketInterface( connData[ 0 ], Integer.parseInt( connData[ 1 ] ) );
          } catch ( Exception e ) {
            SimpleLogger.getInst().log( "Unable to parse port number, check config!", SLLevel.ERROR );
            SimpleLogger.getInst().log( e, SLLevel.ERROR );
          }
          break;
      }

      // Unable to create link, type probably not found or exception while creating
      if( link == null )
        throw new Exception( "Unable to load up proper communication link, please check config!" );

      // Connect to this link
      link.connect();

      // Create database and watt meter
      Database db = new Database( PropConfig.getInstance().getProps() );
      WattMeter meter = new WattMeter( interval, intervalUnit, link );

      // Write new datapoints directly to database
      meter.setDataCallback( db::write );

      // Initialize a new console to listen for commands
      CommunicationLink finalLink = link;
      new CommandConsole( () -> {
        // Shutdown wattmeter and database
        meter.shutdown();
        db.shutdown();
        finalLink.shutdown();
      } );

      SimpleLogger.getInst().log( "WattMeter is up and running!", SLLevel.INFO );
      return true;
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Error while trying to launch the service!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
      return false;
    }
  }
}
