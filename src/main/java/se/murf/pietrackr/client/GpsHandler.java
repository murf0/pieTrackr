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
	Double lastLON=0.0,lastLAT=0.0, time_diff=0.0, time_last=0.0;
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
	 public static Double distance(Double lat1, Double lng1, Double lat2, Double lng2) {
	    double earthRadius = 6371000; //meters
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = (earthRadius * c);

	    return dist;
    }
	public void run() {
		try {
			final GPSdEndpoint ep = new GPSdEndpoint(server, port, new ResultParser());
			ObjectListener p = new ObjectListener() {
				@Override
				public void handleTPV(final TPVObject tpv) {
					//System.setOut(originalStream);
					Double dist = distance(lastLAT,lastLON,tpv.getLatitude(),tpv.getLongitude()); // Calculate distance
					time_diff = tpv.getTimestamp() - time_last;
					//System.out.println("DO CALC dist:" + dist.toString());
					if(dist > 100 || time_diff > 3600) { //if distance is greater than 100m send update. On first start the distance will be stupid since we hardcore 0,0 as coordinates
						lastLAT=tpv.getLatitude();
						lastLON=tpv.getLongitude();
						time_last = tpv.getTimestamp();
						String msg = Double.toString(tpv.getLatitude()) +
								"," +Double.toString(tpv.getLongitude()) +
								"," +Double.toString(tpv.getSpeed()) +
								"," +Double.toString(tpv.getAltitude()) +
								"," +Long.toString((long) tpv.getTimestamp()) +
								"," +Double.toString(dist) +
								"";
						sender.SendMsg(msg);
						LOGGER.info("Sent: " + msg + "Distance:" + dist.toString());
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
