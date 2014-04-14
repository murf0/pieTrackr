package se.murf.pietrackr;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class InitiateMQTT {
	private MqttClient client;
	private String topic;
	private MqttConnectOptions options;
	private String Server;
	private String Port;
	private String ClientID;
	private int QOS=2;
	private boolean RETAIN=false;
	
	public InitiateMQTT(Configuration config) throws Exception  {
		this.topic=config.getPUSH();
		this.Server=config.getSERVER();
		this.Port=config.getPORT();
		this.ClientID=config.getCLIENTID();

		try {
			client = new MqttClient("tcp://" + Server + ":" + Port , ClientID);
			options = new MqttConnectOptions();
			/* options.setW */
			System.out.println(Configuration.getDate() + " Connect MQTT");
			client.connect(options);
		} catch (MqttException e) { 
			e.printStackTrace();
		}
	}
	
	public void disconnect( ) {
		try {
			System.out.println(Configuration.getDate() + " Disconnect MQTT");
			client.disconnect();
		} catch (MqttException e) { 
			e.printStackTrace();
		}
		
	}
	
	public void SendMsg(String msg) {
		/*
		 * MqttMessage message = new MqttMessage();
		 * message.setPayload(msg.getBytes());
		*/
	    try {
			this.client.publish(topic, msg.getBytes(),this.QOS,this.RETAIN);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void SendMsg(String msg, String intopic) {
		this.topic=intopic;
		this.SendMsg(msg);
	}
	
	public void setTopic(String intopic) {
		this.topic = intopic;
	}
}
