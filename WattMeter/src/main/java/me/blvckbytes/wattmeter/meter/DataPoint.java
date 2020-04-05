package me.blvckbytes.wattmeter.meter;

import lombok.Getter;

public class DataPoint {

  @Getter
  private long timestamp;

  @Getter
  private float watts;

  /**
   * A datapoint represents a point in time with a given
   * amount of wattage which has been drawed around this point
   * @param timestamp Timestamp of this datapoint
   * @param watts Watts drawn
   */
  public DataPoint( long timestamp, float watts ) {
    this.timestamp = timestamp;
    this.watts = watts;
  }

  @Override
  public String toString() {
    return "DataPoint{timestamp=" + timestamp + ", watts=" + watts + "}";
  }
}
