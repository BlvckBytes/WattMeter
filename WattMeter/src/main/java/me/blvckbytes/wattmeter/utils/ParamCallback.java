package me.blvckbytes.wattmeter.utils;

public interface ParamCallback< T > {

  /**
   * Used to make lambda callbacks with one parameter
   * @param val Generic value for calling
   */
  void call( T val );

}
