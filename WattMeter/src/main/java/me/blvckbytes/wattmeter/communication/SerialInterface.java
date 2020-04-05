package me.blvckbytes.wattmeter.communication;

import com.fazecast.jSerialComm.SerialPort;
import me.blvckbytes.wattmeter.utils.SLLevel;
import me.blvckbytes.wattmeter.utils.SimpleLogger;

import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SerialInterface extends CommunicationLink {

  private SerialPort target;
  private ExecutorService listener;
  private boolean active;
  private String targetName;

  /**
   * Initialize a new communicator for the arduino serial interface
   * which can receive lines individually and send over usb
   */
  public SerialInterface( String targetName ) {
    this.targetName = targetName;

    // Listener thread for receiving data
    this.listener = Executors.newSingleThreadExecutor();
  }

  /**
   * Send text to the arduino interface
   * @param line Line to send
   */
  @Override
  public void sendLine( String line ) {
    try {
      // Write out line, followed by a carriage return and a new line
      PrintWriter writer = new PrintWriter( this.target.getOutputStream() );
      writer.write( line );
      writer.write( "\r\n" );

      // Close writer
      writer.close();
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Error while trying to send over serial!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }
  }

  /**
   * Shut this communicator down, this means listener thread
   * shutdown and serial port close
   */
  @Override
  public void shutdown() {
    // Stop listening thread
    this.active = false;
    this.listener.shutdown();
    SimpleLogger.getInst().log( "Shut down listener thread!", SLLevel.INFO );


    // Close down serial port
    if( this.target != null && this.target.isOpen() )
      this.target.closePort();

    SimpleLogger.getInst().log( "Shut down serial port!", SLLevel.INFO );
  }

  /**
   * Initialize port and start listening in main loop
   */
  @Override
  public void connect() {
    try {
      // Search for correct port
      if( !searchPort() )
        throw new Exception( "Serial port could not be found, is it plugged in?" );

      // Open connection
      if( !this.target.openPort() )
        throw new Exception( "Could not open the serial port, it probably is already in use!" );

      // Begin
      startListening();
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Error while initializing serial port!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }
  }

  /**
   * Start listening for messages and call specified
   * callback if it has been set
   */
  private void startListening() {
    this.listener.execute( () -> {
      try {
        // Loop while this listener is active, this is done in order
        // to stop the thread on shutdown
        while ( this.active ) {

          // Nothing to read...
          while ( this.target.bytesAvailable() == 0 || this.lineCallback == null )
            Thread.sleep( 20 );

          // On shutting down, this value will get negative
          if( this.target.bytesAvailable() < 0 )
            return;

          // Read into buffer and convert to trimmed string
          byte[] readBuffer = new byte[ this.target.bytesAvailable() ];
          this.target.readBytes( readBuffer, readBuffer.length );

          // Handle calling the callback ( superclass stuff )
          handleCallback( new String( readBuffer ).replaceAll( "(^ +)|( +$)", "" ) );
        }
      } catch ( Exception e ) {
        SimpleLogger.getInst().log( "Error while receiving from serial port!", SLLevel.ERROR );
        SimpleLogger.getInst().log( e, SLLevel.ERROR );
      }
    } );

    // Start thread
    this.active = true;
  }

  /**
   * Search for the right serial port on the system
   * @return True if a port has been found, false otherwise
   */
  private boolean searchPort() {
    // Loop all available ports on this system
    for( SerialPort available : SerialPort.getCommPorts() ) {
      // Search in the ports name for the given target name, ignore casing
      if( !available.getDescriptivePortName().toLowerCase().contains( this.targetName.toLowerCase() ) )
        continue;

      // Found serial port, stop looping
      this.target = available;
      break;
    }

    // True if everything was properly done
    return this.target != null;
  }
}
