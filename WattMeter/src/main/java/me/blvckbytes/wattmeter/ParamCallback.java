package me.blvckbytes.wattmeter;

public interface ParamCallback< T > {

  /**
   * Used to make lambda callbacks
   * @param val Generic value for calling
   */
  void call( T val );

}
