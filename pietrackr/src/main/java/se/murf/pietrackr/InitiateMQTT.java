package se.murf.pietrackr;


import java.util.Properties;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

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
	private final static Logger LOGGER = Logger.getLogger(InitiateMQTT.class.getName());
	private SqlConnector sql=null;
	
	
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
		LOGGER.info("Start subscription " + topic);
		client.subscribe(topic);
	}
	public void messageArrived(String ontopic, MqttMessage msg) throws Exception {
		//TOPIC = tracker/devices/pi/mikael
		// tracker/mikael/pi/DOL616
		// tracker/mikael/owntracks/G2
		// tracker/<username>/<devicetype>/<devicename>
		
		/*
		{
		  "_type": "location",
		  "lat": "58.0396857",
		  "lon": "12.7939241",
		  "tst": "1398365109",
		  "acc": "25.46",
		  "batt": "51"
		}
		*/
		LOGGER.info(ontopic + " " + new String (msg.getPayload()));
		String data= new String (msg.getPayload());
		String user= ontopic.split("/")[1];
		
		JSONObject obj;
		//owntracks:  {"_type": "location", "lat": "58.0396857", "lon": "12.7939241", "tst": "1398365109", "acc": "25.46", "batt": "51"}
		//
		if(ontopic.contains("/pi/")) {
			LOGGER.finest("Pi Parsing");
			obj=new JSONObject();
			obj.put("_type","location");
			String[] data2 = data.split(",");
			obj.put("lat",data2[0]);
			obj.put("lon",data2[1]);
			obj.put("speed",data2[2]);
			obj.put("alt",data2[3]);
			obj.put("tst",Long.parseLong(data2[4]));
			obj.put("batt", new Integer(100));
			obj.put("device","pi");
			obj.put("user",user);
			obj.put("topic",ontopic);
		} else if(ontopic.contains("/owntracks/")) {
			LOGGER.finest("Owntracks Parsing");
			obj=new JSONObject(data);
			obj.put("device","owntracks");
			obj.put("speed","0"); //owntracks does not send
			obj.put("alt","0"); //owntracks does not send
			obj.put("user",user);
			obj.put("topic",ontopic);
		} else {
			LOGGER.info("Unknown Topic " + ontopic);
			obj = null;
		}
		if( sql != null && obj != null) {
			LOGGER.info("logging to SQL" + obj.toString());
			sql.addRow(obj);
		}
		//LOGGER.info("Topic " + ontopic +" Lat " + lat + " Lon " + lon + " speed " + speed + " alt " + alt + " date " + time.toString());

		
	}

	public void setSql(SqlConnector insql) {
		sql = insql;
	}

	public void connectionLost(Throwable arg0) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}
}
