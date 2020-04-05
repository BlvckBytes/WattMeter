package me.blvckbytes.wattmeter.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleLogger {

  private SimpleDateFormat dFormatter;
  private static SimpleLogger inst;
  private SimpleLogger() {
    this.dFormatter = new SimpleDateFormat( "dd.MM @ HH:mm" );
  }

  /**
   * Logs the given message to console, automatically appends the prefix
   * @param input What to log
   * @param level What level to log this entry at
   */
  public void log( String input, SLLevel level ) {
    System.out.println( genPrefix( level ) + input );
  }

  /**
   * Logs the given message to console, but no line break is
   * added to the end. Also appends prefix at beginning
   * @param input What to log
   * @param level What level to log this entry at
   */
  public void logInlineBegin( String input, SLLevel level ) {
    System.out.print( genPrefix( level ) + input );
  }

  /**
   * Logs the given message to console, but no line break is
   * added to the end. No prefix is added
   * @param input What to log
   */
  public void logInline( String input ) {
    System.out.print( input );
  }

  /**
   * Directly log an exception without having to stringify it
   * outside of this class, it's a standard procedure
   * @param e Exception to log
   * @param level Level to log at
   */
  public void log( Exception e, SLLevel level ) {
    StringWriter errors = new StringWriter();
    e.printStackTrace( new PrintWriter( errors ) );
    System.out.println( genPrefix( level ) + errors.toString() );
  }

  /**
   * Generate the prefix which gets appended in front of
   * every log entry, represents current point in time
   * @param level What level this log entry is at
   * @return Generated prefix
   */
  private String genPrefix( SLLevel level ) {
    String date = this.dFormatter.format( new Date( System.currentTimeMillis() ) );
    return "[" + date + ", " + level + "]: ";
  }

  /**
   * Singleton getter
   * @return Instance of class
   */
  public static SimpleLogger getInst() {
    if( inst == null )
      inst = new SimpleLogger();

    return inst;
  }
}
