package se.murf.pietrackr;

import java.util.*;

/**
 * @author Murf Mellgren 20140414
 * daemon running on Raspberry pie for tracking GPS positions
 * https://trackrd.murf.se
 */
public class PieTrackr  {
	private static Configuration config;
	private static List<Thread> threads = new ArrayList<Thread>();
	
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
		
		Runnable gps = new GpsHandler(config);
		Thread worker = new Thread(gps);
		// We can set the name of the thread
		worker.setName("GPSThread1");
		System.out.println("Starting GPS Thread" + worker.getName());
		worker.start();
		// Remember the thread for later usage
		threads.add(worker);
		Thread.sleep(6000);
		worker.interrupt();
		
    }
}
