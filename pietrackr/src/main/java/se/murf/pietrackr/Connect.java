package se.murf.pietrackr;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Connect {
	MqttClient client;
	
	public Connect(final Configuration config) throws Exception {
		try {
			System.out.println("Connect MQTT");
			client = new MqttClient("tcp://" + config.getSERVER() + ":" + config.getPORT() , config.getCLIENTID());
		} catch (MqttException e) { 
			e.printStackTrace();
		}
	}
	
	protected void finalize( ) throws Throwable {
		try {
			System.out.println("Disconnect MQTT");
			client.disconnect();
		} catch (MqttException e) { 
			e.printStackTrace();
		}
		
	}
}
