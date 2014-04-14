package se.murf.pietrackr;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public class SendMsg {

	public SendMsg(Connect connection, String topic) {
		MqttMessage message = new MqttMessage();
	    message.setPayload("A single message".getBytes());
	    try {
			connection.client.publish(topic, message);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
