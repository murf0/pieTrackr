package se.murf.pietrackr.server;

import java.util.Arrays;
import java.util.logging.Logger;

import se.murf.pietrackr.Configuration;
import se.murf.pietrackr.InitiateMQTT;
import se.murf.pietrackr.client.PieTrackr;

public class Server {
	private static Configuration config;
	private final static Logger LOGGER = Logger.getLogger(PieTrackr.class.getName());
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
		LOGGER.info(Arrays.toString(args));
		config = new Configuration(args);
		LOGGER.info("Connect to MySQL");
		SqlConnector sql = new SqlConnector();
		
		LOGGER.info("Connect to MQTT");
		InitiateMQTT receiver = new InitiateMQTT(config);
		LOGGER.info("do subscritopn");
		receiver.setSql(sql);
		receiver.setSubscribe();
		while(keepRunning) {
			Thread.sleep(10000);
		}
		/**
		 * Disconnect MQTT
		 */
		LOGGER.info("Disconnect MQTT");
		receiver.disconnect();
    }

}
