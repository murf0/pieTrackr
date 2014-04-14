package se.murf.pietrackr;

import java.io.IOException;
import java.util.Date;

/**
 * @author Murf Mellgren 20140414
 * daemon running on Raspberry pie for tracking GPS positions
 * https://trackrd.murf.se
 */
public class PieTrackr  {
	private static Configuration config;
	private static Connect connection;
	
	public static void main( String[] args ) throws Exception {
		/**
		 * Parse Configuration
		 */
		config = new Configuration(args);
		/**
		 * Connect to MQTT
		 */
		connection = new Connect(config);
		
		/**
		 * Send Message to MQTT
		 */
		
        System.out.println( "Hello World!" );
    }
}
