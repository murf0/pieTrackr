package se.murf.pietrackr;

import java.util.Arrays;

/**
 * @author Murf Mellgren 20140414
 * daemon running on Raspberry pie for tracking GPS positions
 * https://trackrd.murf.se
 */
public class PieTrackr  {
	private static Configuration config;
	
	public static void main( String[] args ) throws Exception {
		/**
		 * Parse Configuration
		 */
		System.out.println(Arrays.toString(args));
		config = new Configuration(args);
		
		/**
		 * Connect to MQTT
		 */
		//InitiateMQTT sender = new InitiateMQTT(config);
		
		/**
		 * Send Message to MQTT
		 **/
		//sender.SendMsg("firsttstmsg");
		/**
		 * Disconnect MQTT
		 */
		//sender.disconnect();
		GpsHandler gps = new GpsHandler(config);
		gps.run();
    }
}
