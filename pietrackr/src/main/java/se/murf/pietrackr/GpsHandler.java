package se.murf.pietrackr;


import java.util.logging.Level;

import de.taimos.gpsd4java.api.ObjectListener;
import de.taimos.gpsd4java.backend.GPSdEndpoint;
import de.taimos.gpsd4java.backend.ResultParser;
import de.taimos.gpsd4java.types.ATTObject;
import de.taimos.gpsd4java.types.DeviceObject;
import de.taimos.gpsd4java.types.DevicesObject;
import de.taimos.gpsd4java.types.PollObject;
import de.taimos.gpsd4java.types.SATObject;
import de.taimos.gpsd4java.types.SKYObject;
import de.taimos.gpsd4java.types.TPVObject;
import de.taimos.gpsd4java.types.subframes.SUBFRAMEObject;



public class GpsHandler implements Runnable {
	int port=0;
	String server="NONE";

	public GpsHandler(Configuration config) {
		port = config.getGPSDPORT();
		server = config.getGPSDSERVER();
	}
	
	public void run() {
		try {
			final GPSdEndpoint ep = new GPSdEndpoint(server, port, new ResultParser());
			
			ObjectListener p = new ObjectListener() {
				@Override
				public void handleTPV(final TPVObject tpv) {
					System.out.println(Double.toString(tpv.getAltitude()));
					System.out.println("TPVOBJECT");
				}
			};
			ep.addListener(p);
			
			ep.start();
			ep.watch(true, false);
			ep.version();
			
			int i=0;
			while(true) {
				i++;
				ep.handle(null);
				
				System.out.println("Runo: " + i);
				try {
				Thread.sleep(1000);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt(); // very important
			        break;
				}
			}
			System.out.println("Shutting down thread");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
