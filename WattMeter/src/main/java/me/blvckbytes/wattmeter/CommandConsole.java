package me.blvckbytes.wattmeter;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandConsole {

  private Runnable onStop;
  private ExecutorService consoleThread;
  private boolean running;

  /**
   * Create a new console for commands to use with this software
   * @param onStop Callback for shutting the program down
   */
  public CommandConsole( Runnable onStop ) {
    this.onStop = onStop;
    this.running = true;
    this.consoleThread = Executors.newSingleThreadExecutor();

    initShutdownHook();
    initConsole();
  }

  /**
   * Adds a shutdown hook to runtime in order to shut this
   * console down and call the stop callback to shut down
   * external resources
   */
  private void initShutdownHook() {
    Runtime.getRuntime().addShutdownHook( new Thread( () -> {
      this.running = false;
      this.consoleThread.shutdown();
      onStop.run();
    } ) );
  }

  /**
   * Initializes the console used to receive commands
   * from standard in
   */
  private void initConsole() {
    consoleThread.execute( () -> {

      // Open scanner on standard in
      Scanner scanner = new Scanner( System.in );
      while( this.running && scanner.hasNextLine() ) {

        // Read user input line
        String line = scanner.nextLine().trim();

        // Only stop is availale for now
        if( !line.equalsIgnoreCase( "stop" ) ) {
          System.out.println( "Only command is 'stop', to stop the wattmeter service!" );
          continue;
        }

        // Call shutdown callback
        onStop.run();
      }
    } );
  }
}
