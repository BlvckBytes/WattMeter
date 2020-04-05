package me.blvckbytes.wattmeter;

import lombok.Setter;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class WattMeter {

  @Setter
  private ParamCallback< DataPoint > dataCallback;

  private CommunicationLink comm;
  private TreeMap< Long, Integer > buffer;
  private long duration;

  /**
   * Set up a new wattmeter to keep track of power
   * usage and plot graphs over time
   */
  public WattMeter( long dataLength, TimeUnit lengthUnit, CommunicationLink comm ) {
    // Buffer is sorted by key ( time )
    this.buffer = new TreeMap<>();

    // Calculate duration of a datapoint in milliseconds
    this.duration = TimeUnit.MILLISECONDS.convert( dataLength, lengthUnit );

    // Start communication
    this.comm = comm;
    this.comm.setLineCallback( this::receive );
  }

  /**
   * Shut down the wattmeter and write results in buffer
   */
  public void shutdown() {
    // Shut down communicator
    this.comm.shutdown();

    // Write last data point
    process( true );
    SimpleLogger.getInst().log( "Wrote last datapoint!", SLLevel.INFO );
  }

  /**
   * Receive an input-line from the arduino that sends it's
   * analyzed information on how much watts are used at the moment
   * @param line Line containing the number
   */
  private void receive( String line ) {
    if( !line.matches( "[0-9]+" ) )
      return;

    // Append value to buffer with current time
    int watts = Integer.parseInt( line );
    buffer.put( System.currentTimeMillis(), watts );

    // Try to collect entries together
    process( false );
  }

  /**
   * Create a new datapoint if the specified duration of
   * one point has been reached
   * @param force Wether to write if the duration has still not been reached
   */
  private void process( boolean force ) {
    // Not enough elements or callback not set
    if( this.dataCallback == null || buffer.size() < 2 )
      return;

    // Calculate difference between last and first key ( total duration in buffer )
    long delta = this.buffer.lastKey() - this.buffer.firstKey();

    // Timespan of elements is too low
    if( delta < this.duration && !force )
      return;

    // Sum up all values ( all watt numbers )
    float sum = this.buffer.values().stream().mapToLong( Integer::intValue ).sum();
    sum = Math.round( sum / this.buffer.size() * 100F ) / 100F;

    // Handle datapoint and clear buffer
    this.dataCallback.call( new DataPoint( this.buffer.lastKey(), sum ) );
    this.buffer.clear();
  }
}
