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
		port = Integer.parseInt(config.getProperty("gpsdPort"));
		server = config.getProperty("gpsdServer");
		sender = insender;
	}
	
	public void run() {
		try {
			final GPSdEndpoint ep = new GPSdEndpoint(server, port, new ResultParser());
			ObjectListener p = new ObjectListener() {
				@Override
				public void handleTPV(final TPVObject tpv) {
					//System.setOut(originalStream);
					String msg = Double.toString(tpv.getLatitude()) +
							"," +Double.toString(tpv.getLongitude()) +
							"," +Double.toString(tpv.getSpeed()) +
							"," +Double.toString(tpv.getAltitude()) +
							"," +Long.toString((long) tpv.getTimestamp()) +
							"";
					sender.SendMsg(msg);
					LOGGER.info("Sent: " + msg);
					//System.setOut(dummyStream);
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
