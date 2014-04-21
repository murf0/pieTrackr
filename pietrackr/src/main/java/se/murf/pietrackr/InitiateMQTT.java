package se.murf.pietrackr;


import java.util.Properties;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import se.murf.pietrackr.client.GpsHandler;

public class InitiateMQTT implements MqttCallback {
	private MqttClient client;
	private String topic;
	private MqttConnectOptions options;
	private String Server;
	private String Port;
	private String ClientID;
	private int QOS=2;
	private boolean RETAIN=false;
	private final static Logger LOGGER = Logger.getLogger(GpsHandler.class.getName());

	
	
	public InitiateMQTT(Configuration config) throws Exception  {
		this.topic=config.getPUSH();
		this.Server=config.getSERVER();
		this.Port=config.getPORT();
		this.ClientID=config.getCLIENTID();

		
        System.setProperty("javax.net.ssl.trustStore", config.getKEYSTORE());
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        System.setProperty("javax.net.ssl.keyStore", config.getKEYSTORE());
        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
        
		try {
			client = new MqttClient("ssl://" + Server + ":" + Port , ClientID);
			options = new MqttConnectOptions();
			options.setCleanSession(true);
			Properties props = new Properties();
			props.setProperty("com.ibm.ssl.protocol", "TLSv1.2");
			options.setSSLProperties(props);
			/* options.setW */
			LOGGER.info(" Connect MQTT");
			client.connect(options);
			client.setCallback(this);
		} catch (MqttException e) { 
			e.printStackTrace();
		}
	}
	
	public void disconnect( ) {
		try {
			LOGGER.info(" Disconnect MQTT");
			client.disconnect();
		} catch (MqttException e) { 
			e.printStackTrace();
		}
		
	}
	
	public void SendMsg(String msg) {

	    try {
			this.client.publish(topic, msg.getBytes(),this.QOS,this.RETAIN);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(99);
		}
	}
	
	public void SendMsg(String msg, String intopic) {
		this.topic=intopic;
		this.SendMsg(msg);
	}
	
	public void setTopic(String intopic) {
		this.topic = intopic;
	}
	
	public void setSubscribe() throws MqttException {
		client.subscribe(topic);
	}
	
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		
	}

	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}

	public void messageArrived(String ontopic, MqttMessage msg) throws Exception {
		LOGGER.info(ontopic + " " + new String (msg.getPayload()));
		
	}
}
