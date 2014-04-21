package se.murf.pietrackr.client;


import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;



import se.murf.pietrackr.Configuration;
import se.murf.pietrackr.InitiateMQTT;
import de.taimos.gpsd4java.api.ObjectListener;
import de.taimos.gpsd4java.backend.GPSdEndpoint;
import de.taimos.gpsd4java.backend.ResultParser;
import de.taimos.gpsd4java.types.TPVObject;



public class GpsHandler implements Runnable {
	int port=0;
	String server="NONE";
	InitiateMQTT sender;
	private final static Logger LOGGER = Logger.getLogger(GpsHandler.class.getName());

	
	PrintStream originalStream = System.out;
	PrintStream dummyStream    = new PrintStream(new OutputStream(){
	    public void write(int b) {
	        //NO-OP
	    }
	});
	
	
	public GpsHandler(Configuration config,InitiateMQTT insender) {
		port = config.getGPSDPORT();
		server = config.getGPSDSERVER();
		sender = insender;
	}
	
	public void run() {
		try {
			final GPSdEndpoint ep = new GPSdEndpoint(server, port, new ResultParser());
			ObjectListener p = new ObjectListener() {
				@Override
				public void handleTPV(final TPVObject tpv) {
					//System.setOut(originalStream);
					LOGGER.info("START");
					LOGGER.info("Altitude: " +Double.toString(tpv.getAltitude()));
					LOGGER.info("longitude: " +Double.toString(tpv.getLongitude()));
					LOGGER.info("latitude: " +Double.toString(tpv.getLatitude()));
					LOGGER.info("speed: " +Double.toString(tpv.getSpeed()));
					LOGGER.info("END");
					String msg = Double.toString(tpv.getAltitude()) + 
							"|" +Double.toString(tpv.getLongitude()) +
							"|" +Double.toString(tpv.getLatitude()) +
							"|" +Double.toString(tpv.getSpeed());
					sender.SendMsg(msg);
					System.setOut(dummyStream);
				}
			};
			ep.addListener(p);
			ep.start();
			
			ep.watch(true, true);
			
			//ep.version().toString();

			
			while(true) {
				System.setOut(dummyStream);
				ep.poll();
				System.setOut(originalStream);
				//System.out.println("Runo: " + i);
				try {
				Thread.sleep(1000);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt(); // very important
			        break;
				}
			}
			LOGGER.info("Shutting down thread GPShandler");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
