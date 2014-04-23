package se.murf.pietrackr;


import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import se.murf.pietrackr.client.GpsHandler;
import se.murf.pietrackr.server.SqlConnector;

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

		if(config.getSSL()) {
	        System.setProperty("javax.net.ssl.trustStore", config.getKEYSTORE());
	        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
	        //System.setProperty("javax.net.ssl.keyStore", config.getKEYSTORE());
	        //System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
		}
		try {
			client = new MqttClient("ssl://" + Server + ":" + Port , ClientID);
			options = new MqttConnectOptions();
			options.setCleanSession(true);
			options.setPassword(config.getPassword());
			options.setUserName(config.getUserName());
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

	public void messageArrived(String ontopic, MqttMessage msg, SqlConnector sql) throws Exception {
		LOGGER.info(ontopic + " " + new String (msg.getPayload()));
		String data= new String (msg.getPayload());
		String[] data2 = data.split(",");
		//System.out.println(Arrays.toString(data2));
		String lat=data2[0];
		String lon=data2[1];
		String speed=data2[2];
		String alt=data2[3];
		long epoch=Long.parseLong(data2[4]);
		Date time=new Date(epoch * 1000);
		
		System.out.println("LAT" + lat);
		System.out.println("Lon" + lon);
		System.out.println("LAT" + speed);
		System.out.println("LAT" + alt);
		System.out.println("LAT" + time.toString());

		
	}
}
