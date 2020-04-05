package me.blvckbytes.wattmeter.communication;

import me.blvckbytes.wattmeter.meter.DataPoint;
import me.blvckbytes.wattmeter.utils.SLLevel;
import me.blvckbytes.wattmeter.utils.SimpleLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;

public class Database {

  private Connection conn;
  private Properties connData;

  /**
   * Set up a new database connection based on the given
   * data ( credentials, location )
   * @param data Properties with needed kv pairs
   */
  public Database( Properties data ) {
    this.connData = data;

    connect();
    createSchema();
  }

  /**
   * Write a datapoint to database
   * @param point Datapoint to write
   */
  public void write( DataPoint point ) {
    try {
      String table = connData.getProperty( "sql_table_name" );

      // Prepare statement for insertion into datapoint table
      PreparedStatement ps = this.conn.prepareStatement(
        "INSERT INTO " + table +
        "( `time`, `watts` ) VALUES ( ?, ? )"
      );

      // Set values of prepared statement
      ps.setLong( 1, point.getTimestamp() );
      ps.setFloat( 2, point.getWatts() );

      // Execute
      ps.executeUpdate();
      SimpleLogger.getInst().log( "Wrote " + point + "!", SLLevel.INFO );
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Could not write datapoint to database!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }
  }

  /**
   * Open the connection to database
   */
  private void connect() {
    try {
      // Read out connection data from provided property map
      int port = Integer.parseInt( connData.getProperty( "sql_port" ) );
      String usr = connData.getProperty( "sql_user" );
      String passwd = connData.getProperty( "sql_pass" );
      String host = connData.getProperty( "sql_host" );

      // Open connection with provided credentials
      Class.forName( "com.mysql.cj.jdbc.Driver" );
      this.conn = DriverManager.getConnection(
        "jdbc:mysql://" + host + ":" + port + "/?user=" + usr + "&password=" + passwd +
        "&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
      );

      SimpleLogger.getInst().log( "Successfully connected to database!", SLLevel.INFO );
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Could not connect to database!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }
  }

  /**
   * Creates the database and the needed table
   * if they don't already exist
   */
  private void createSchema() {
    try {
      String db = connData.getProperty( "sql_db_name" );
      String table = connData.getProperty( "sql_table_name" );

      // Create database
      Statement stmt = conn.createStatement();
      stmt.executeUpdate(  "CREATE DATABASE IF NOT EXISTS " + db );

      // Use database
      stmt = conn.createStatement();
      stmt.executeUpdate(  "USE " + db );

      // Create table
      stmt = conn.createStatement();
      stmt.executeUpdate(
        "CREATE TABLE IF NOT EXISTS " + table + " ( " +
          "`time` BIGINT, " +
          "`watts` FLOAT, " +
          "PRIMARY KEY( `time` ) " +
        ")"
      );
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Could not create needed schema!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }
  }

  /**
   * Shut down the active database connection
   */
  public void shutdown() {
    try {
      if( this.conn == null || this.conn.isClosed() )
        return;

      this.conn.close();
      SimpleLogger.getInst().log( "Shut down database!", SLLevel.INFO );
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Could not close database connection!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }
  }
}
