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
	
	public static void main( String[] args ) throws Exception {
		/**
		 * Parse Configuration
		 */
		config = new Configuration(args);
		
		/**
		 * Connect to MQTT
		 */
		InitiateMQTT sender = new InitiateMQTT(config);
		
		/**
		 * Send Message to MQTT
		 **/
		
		sender.SendMsg("VERY2");
		sender.SendMsg("VERY","Mine/TEST");
		sender.SendMsg("VERY2");
		/**
		 * Disconnect MQTT
		 */
		sender.disconnect();
    }
}
