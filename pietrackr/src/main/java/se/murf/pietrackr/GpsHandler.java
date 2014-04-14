package se.murf.pietrackr;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.taimos.gpsd4java.api.ObjectListener;
import de.taimos.gpsd4java.backend.GPSdEndpoint;
import de.taimos.gpsd4java.backend.ResultParser;
import de.taimos.gpsd4java.types.ATTObject;
import de.taimos.gpsd4java.types.DeviceObject;
import de.taimos.gpsd4java.types.DevicesObject;
import de.taimos.gpsd4java.types.SATObject;
import de.taimos.gpsd4java.types.SKYObject;
import de.taimos.gpsd4java.types.TPVObject;
import de.taimos.gpsd4java.types.subframes.SUBFRAMEObject;


public class GpsHandler {
	static final Logger log = Logger.getLogger(GpsHandler.class.getName());
	private String host = "10.0.1.19";
	private int port = 2947;
	
	public GpsHandler() throws IOException {
		try {
		final GPSdEndpoint ep = new GPSdEndpoint(host, port, new ResultParser());
		ep.addListener(new ObjectListener() {

			@Override
			public void handleTPV(final TPVObject tpv) {
				GpsHandler.log.log(Level.INFO, "TPV: {0}", tpv);
			}

			@Override
			public void handleSKY(final SKYObject sky) {
				GpsHandler.log.log(Level.INFO, "SKY: {0}", sky);
				for (final SATObject sat : sky.getSatellites()) {
					GpsHandler.log.log(Level.INFO, "  SAT: {0}", sat);
				}
			}

			@Override
			public void handleSUBFRAME(final SUBFRAMEObject subframe) {
				GpsHandler.log.log(Level.INFO, "SUBFRAME: {0}", subframe);
			}

			@Override
			public void handleATT(final ATTObject att) {
				GpsHandler.log.log(Level.INFO, "ATT: {0}", att);
			}

			@Override
			public void handleDevice(final DeviceObject device) {
				GpsHandler.log.log(Level.INFO, "Device: {0}", device);
			}

			@Override
			public void handleDevices(final DevicesObject devices) {
				for (final DeviceObject d : devices.getDevices()) {
					GpsHandler.log.log(Level.INFO, "Device: {0}", d);
				}
			}
		});

		ep.start();
		GpsHandler.log.log(Level.INFO, "Version: {0}", ep.version());

		GpsHandler.log.log(Level.INFO, "Watch: {0}", ep.watch(true, false));

		GpsHandler.log.log(Level.INFO, "Poll: {0}", ep.poll());
		GpsHandler.log.log(Level.INFO, "Poll: {0}", ep.poll());
		Thread.sleep(60000);
	} catch (final Exception e) {
		GpsHandler.log.log(Level.SEVERE, null, e);
	}
	}
}