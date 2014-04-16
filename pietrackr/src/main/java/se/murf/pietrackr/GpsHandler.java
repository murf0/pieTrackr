package se.murf.pietrackr;


import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
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
	InitiateMQTT sender;
	
	
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
					System.setOut(originalStream);
					System.out.println("START");
					System.out.println("Altitude: " +Double.toString(tpv.getAltitude()));
					System.out.println("longitude: " +Double.toString(tpv.getLongitude()));
					System.out.println("latitude: " +Double.toString(tpv.getLatitude()));
					System.out.println("speed: " +Double.toString(tpv.getSpeed()));
					System.out.println("END");
					String msg = "Altitude: " +Double.toString(tpv.getAltitude()) + 
							"longitude: " +Double.toString(tpv.getLongitude()) +
							"latitude: " +Double.toString(tpv.getLatitude()) +
							"speed: " +Double.toString(tpv.getSpeed());
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
			System.out.println("Shutting down thread");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
