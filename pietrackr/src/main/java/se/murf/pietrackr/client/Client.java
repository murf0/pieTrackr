package se.murf.pietrackr.client;

import java.util.*;
import java.util.logging.Logger;

import se.murf.pietrackr.Configuration;
import se.murf.pietrackr.InitiateMQTT;

/**
 * @author Murf Mellgren 20140414
 * daemon running on Raspberry pie for tracking GPS positions
 * https://trackrd.murf.se
 */
public class Client  {
	private static Configuration config;
	private static List<Thread> threads = new ArrayList<Thread>();
	private final static Logger LOGGER = Logger.getLogger(Client.class.getName());
	static volatile boolean keepRunning = true;
	
	public static void main( String[] args ) throws Exception {
		final Thread mainThread = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		        keepRunning = false;
		        try {
					mainThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		});
		/**
		 * Parse Configuration
		 */
		LOGGER.info("Parsing Input");
		config = new Configuration("server.config");
		
		LOGGER.info("Connect to MQTT");
		InitiateMQTT sender = new InitiateMQTT(config);
		
		/**
		 * Send Message to MQTT
		 **/
		//sender.SendMsg("firsttstmsg");

		Runnable gps = new GpsHandler(config,sender);
		Thread worker = new Thread(gps);
		// We can set the name of the thread
		worker.setName("GPSThread1");
		System.out.println("Starting GPS Thread" + worker.getName());
		worker.start();
		// Remember the thread for later usage
		threads.add(worker);
		while(keepRunning) {
			Thread.sleep(10000);
		}
		LOGGER.info("Disrupt GPS thread");
		worker.interrupt();
		/**
		 * Disconnect MQTT
		 */
		LOGGER.info("Disconnect MQTT");
		sender.disconnect();
    }
}
