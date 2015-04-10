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
	int delay=0;
	String server="NONE";
	InitiateMQTT sender;
	private final static Logger LOGGER = Logger.getLogger(GpsHandler.class.getName());
	Double lastLON=0.0,lastLAT=0.0;
	
	PrintStream originalStream = System.out;
	PrintStream dummyStream    = new PrintStream(new OutputStream(){
	    @Override
		public void write(int b) {
	        //NO-OP
	    }
	});
	
	
	public GpsHandler(Configuration config,InitiateMQTT insender) {
		port = Integer.parseInt(config.getProperty("gpsdPort"));
		server = config.getProperty("gpsdServer");
		sender = insender;
		if(! config.getProperty("gpsdDelay").isEmpty()) {
			delay = Integer.parseInt(config.getProperty("gpsdDelay"));
		} else {
			delay = 1000;
		}
	}
	private double distance(double lat1, double lon1, double lat2, double lon2) {
		  double theta = lon1 - lon2;
		  double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		  dist = Math.acos(dist);
		  dist = rad2deg(dist);
		  dist = dist * 60 * 1.1515 * 1.609344 / 1000;
		  return (dist);
		}
		private double deg2rad(double deg) {
		  return (deg * Math.PI / 180.0);
		}
		private double rad2deg(double rad) {
		  return (rad * 180 / Math.PI);
		}
	public void run() {
		try {
			final GPSdEndpoint ep = new GPSdEndpoint(server, port, new ResultParser());
			ObjectListener p = new ObjectListener() {
				@Override
				public void handleTPV(final TPVObject tpv) {
					//System.setOut(originalStream);
					Double dist = distance(lastLAT,lastLON,tpv.getLongitude(),tpv.getLongitude());
					if(dist > 100) {
						String msg = Double.toString(tpv.getLatitude()) +
								"," +Double.toString(tpv.getLongitude()) +
								"," +Double.toString(tpv.getSpeed()) +
								"," +Double.toString(tpv.getAltitude()) +
								"," +Long.toString((long) tpv.getTimestamp()) +
								"," +Double.toString(dist) +
								"";
						sender.SendMsg(msg);
						LOGGER.info("Sent: " + msg);
					}
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
				Thread.sleep(delay);
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
