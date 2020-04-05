package me.blvckbytes.wattmeter;

import lombok.Setter;

/**
 * This interface represents a possible implementation of
 * a communication link to the hardware of the wattmeter
 */
public abstract class CommunicationLink {

  @Setter
  protected ParamCallback< String > lineCallback;
  private StringBuilder remainder;

  /**
   * Send a line over communication link
   * @param line Line to send
   */
  abstract void sendLine( String line );

  /**
   * Open a connection over the link
   */
  abstract void connect();

  /**
   * Shutdown link-manager with all his internal stuff
   */
  abstract void shutdown();

  /**
   * Handles the management of calling the callback with proper
   * separated lines and buffering or spliting data
   * @param received Whatever string has been received from link
   */
  public void handleCallback( String received ) {
    if( remainder == null )
      remainder = new StringBuilder();

    // Only call callback if string is not empty
    if( received.equals( "" ) || lineCallback == null )
      return;

    // No new line, thus append this part to the remainder buffer
    if( !( received.endsWith( "\n" ) || received.endsWith( "\r" ) ) ) {
      remainder.append( received );
      return;
    }

    // Loop all lines, since sometimes multiple lines stick together
    // This basically happens when incoming speed is > than reading speed
    // I still want to have lines separated
    String[] out = ( remainder.toString() + received ).split( "[\n\r]" );
    for( String line : out ) {

      // Trim current line and check if it still contains anything
      line = line.trim();
      if( line.equals( "" ) )
        continue;

      // Call with current line
      this.lineCallback.call( line );
    }

    // Reset remainder buffer
    remainder = new StringBuilder();
  }
}
