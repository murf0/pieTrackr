package se.murf.pietrackr.server;


import java.util.logging.Logger;

import se.murf.pietrackr.Configuration;
import se.murf.pietrackr.InitiateMQTT;
import se.murf.pietrackr.MyLogger;

public class Server {
	private static Configuration config;
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	static volatile boolean keepRunning = true;
	
	public static void main( String[] args ) throws Exception {
		MyLogger.setup();
		final Thread mainThread = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    @Override
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
		LOGGER.info("Loading Configruation Input");
		config = new Configuration("server.config");
		LOGGER.info("Connect to MySQL");
		SqlConnector sql = new SqlConnector(config);
		
		LOGGER.info("Connect to MQTT");
		InitiateMQTT receiver = new InitiateMQTT(config);
		
		receiver.setSql(sql);
		receiver.setSubscribe();
		while(keepRunning) {
			Thread.sleep(1000);
		}
		/**
		 * Disconnect MQTT
		 */
		LOGGER.info("Disconnect MQTT");
		receiver.disconnect();
		sql.disconnect();
    }

}
