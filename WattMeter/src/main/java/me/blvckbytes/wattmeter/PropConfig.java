package me.blvckbytes.wattmeter;

import lombok.Getter;

import java.io.*;
import java.util.Properties;

public class PropConfig {

  private static PropConfig inst;

  @Getter
  private Properties props;
  private String outputFile = "/config/config.properties";

  /**
   * Create property map and read the file into
   * it if possible
   */
  private PropConfig() {
    // Create an empty property map
    inst = this;
    this.props = new Properties();

    // Load if existent, write otherwise - then load
    if( !load() ) {
      copyInternal();
      load();
    }
  }

  /**
   * Load the property config from an input stream
   * @param stream Input stream containing property data
   */
  private void loadFromStream( InputStream stream ) {
    try {
      // Load file properties if file is existent
      props.load( stream );

      // Close after reading
      stream.close();
    } catch( Exception e ) {
      SimpleLogger.getInst().log( "Error while loading PropConfig from stream!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }
  }

  /**
   * Load properties config from file into memory
   * @return True if found, false if non existent
   */
  private boolean load() {
    try {
      File f = new File( Main.path + outputFile );
      loadFromStream( new FileInputStream( f ) );
      return true;
    } catch ( Exception e ) {
      return false;
    }
  }

  /**
   * Save the properties from the internal resources into the file
   */
  public void copyInternal() {
    // Nothing has been done yet, abort
    if( this.props == null )
      return;

    try {
      // Create output stream to file
      File f = new File( Main.path + outputFile );
      if( !f.exists() && !f.getParentFile().mkdirs() && !f.createNewFile() )
        throw new Exception( "Could not create output file!" );

      // Get file outputstream and resource's inputstream
      OutputStream outS = new FileOutputStream( f );
      InputStream is = getClass().getClassLoader().getResourceAsStream( "config.properties" );
      if( is == null )
        throw new Exception( "Error while getting PropConfig from internal resources folder!" );

      // Create buffer with bytes from internal config
      byte[] buffer = new byte[ is.available() ];

      // Store into file
      outS.write( buffer, 0, is.read( buffer ) );

      // Close after writing
      outS.close();
    } catch( Exception e ) {
      e.printStackTrace();
    }
  }

  /**
   * Singleton instance getter, instantiates if non existent
   * @return Instance of PropConfig
   */
  public static PropConfig getInstance() {
    if( inst == null )
      inst = new PropConfig();

    return inst;
  }
}
