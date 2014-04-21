package se.murf.pietrackr.server;

import java.util.Arrays;
import java.util.logging.Logger;

import se.murf.pietrackr.Configuration;
import se.murf.pietrackr.InitiateMQTT;
import se.murf.pietrackr.client.PieTrackr;

public class Server {
	private static Configuration config;
	private final static Logger LOGGER = Logger.getLogger(PieTrackr.class.getName());

	public static void main( String[] args ) throws Exception {
		/**
		 * Parse Configuration
		 */
		LOGGER.info("Parsing Input");
		LOGGER.info(Arrays.toString(args));
		config = new Configuration(args);
		
		LOGGER.info("Connect to MQTT");
		InitiateMQTT receiver = new InitiateMQTT(config);
		receiver.setSubscribe();
		
		
		Thread.sleep(60000);
		/**
		 * Disconnect MQTT
		 */
		LOGGER.info("Disconnect MQTT");
		receiver.disconnect();
    }

}
