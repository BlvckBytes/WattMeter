package me.blvckbytes.wattmeter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

  public static String path;

  // Format path to proper standard value
  static {
    try {
      path = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();

      // Remove file if exists
      if( path.endsWith( ".jar" ) || path.endsWith( "/" ) )
        path = path.substring( 0, path.lastIndexOf( "/" ) );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  public static void main( String[] args ) {
    CommunicationLink link = new SocketInterface( "192.168.0.190", 80 );
    link.connect();
    link.setLineCallback( data -> System.out.println( "Received: " + data ) );
  }

  /**
   * Main entry point of this program
   */
  public static void main2( String[] args ) {
    // Read out interval from config
    Properties props = PropConfig.getInstance().getProps();
    int interval = Integer.parseInt( props.getProperty( "meter_interval" ) );
    TimeUnit intervalUnit = TimeUnit.valueOf( props.getProperty( "meter_interval_unit" ) );

    // Create database and watt meter
    Database db = new Database( PropConfig.getInstance().getProps() );
    CommunicationLink link = new SerialInterface( "FT232" );
    WattMeter meter = new WattMeter( interval, intervalUnit, link );

    // Write new datapoints directly to database
    meter.setDataCallback( db::write );
    SimpleLogger.getInst().log( "WattMeter is up and running!", SLLevel.INFO );

    // Console to stop task
    ExecutorService console = Executors.newSingleThreadExecutor();
    AtomicBoolean consoleUp = new AtomicBoolean( true );
    console.execute( () -> {

      // Open scanner on standard in
      Scanner scanner = new Scanner( System.in );
      while( consoleUp.get() && scanner.hasNextLine() ) {

        // Read user input line
        String line = scanner.nextLine().trim();

        // Only stop is availale
        if( !line.equalsIgnoreCase( "stop" ) ) {
          System.out.println( "Only command is 'stop', to stop the wattmeter service!" );
          continue;
        }

        // Exit
        System.exit( 0 );
      }

    } );

    // Shut down meter and db on application shutdown
    Runtime.getRuntime().addShutdownHook( new Thread( () -> {
      // Shutdown console
      console.shutdown();
      consoleUp.set( false );

      // Shutdown wattmeter and database
      meter.shutdown();
      db.shutdown();
    } ) );
  }
}
