package me.blvckbytes.wattmeter.communication;

import me.blvckbytes.wattmeter.utils.SLLevel;
import me.blvckbytes.wattmeter.utils.SimpleLogger;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketInterface extends CommunicationLink {

  private String ip;
  private int port;
  private Socket sock;
  private ExecutorService listener;
  private boolean active;
  private OutputStreamWriter osw;

  /**
   * A new socket connection to the arduino, based on the connection data
   * @param ip IP of the arduino ( get this from serial output )
   * @param port Port of the arduino ( get this from serial output )
   */
  public SocketInterface( String ip, int port ) {
    this.ip = ip;
    this.port = port;

    // Listener thread for receiving data
    this.listener = Executors.newSingleThreadExecutor();
  }

  /**
   * Send a line over the socket to the arduino, ending with a
   * carriage return followed by a new line
   * @param line Line to send
   */
  @Override
  public void sendLine( String line ) {
    try {
      if( this.sock == null || this.sock.isClosed() )
        throw new Exception( "Socket is closed or timed out!" );

      // Write line
      osw.write( line + "\r\n" );

      // Flush buffer
      osw.flush();
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Error while trying to send over socket!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }
  }

  /**
   * Close the active socket connection to the arduino
   */
  @Override
  public void shutdown() {
    try {
      // End while loop for receiving
      this.active = false;

      // Close if it is open
      if( this.sock != null && !this.sock.isClosed() )
        this.sock.close();

      // End thread
      this.listener.shutdown();
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Error while trying to close the socket!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }
  }

  /**
   * Establish a connection over a socket to the arduino
   */
  @Override
  public void connect() {
    try {
      // Create socket
      this.sock = new Socket( this.ip, this.port );

      // Open a new writer on socket's output stream
      this.osw = new OutputStreamWriter( this.sock.getOutputStream() );

      // Start listening
      this.active = true;
      startListening();
      SimpleLogger.getInst().log( "Socket to " + ip + ":" + port + " successfully opened!", SLLevel.INFO );
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Error while trying to open socket!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }
  }

  /**
   * Start listening to incoming data over socket from arduino
   */
  private void startListening() {
    this.listener.execute( () -> {
      try {
        // Get input stream from socket and read as long as object is still active
        InputStream is = this.sock.getInputStream();
        while( this.active ) {

          // Create byte buffer with expected size
          int bytesExpected = is.available();
          byte[] buffer = new byte[ bytesExpected ];

          // Delegate input to callback handler
          if( is.read( buffer ) > 0 ) {
            String recv = new String( buffer );
            handleCallback( recv );
          }
        }
      } catch ( Exception e ) {
        SimpleLogger.getInst().log( "Error while listening on socket!", SLLevel.ERROR );
        SimpleLogger.getInst().log( e, SLLevel.ERROR );
      }
    } );
  }
}
