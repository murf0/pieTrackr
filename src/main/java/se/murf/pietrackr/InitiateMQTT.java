package se.murf.pietrackr;


import java.util.Properties;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import se.murf.pietrackr.server.SqlConnector;

public class InitiateMQTT implements MqttCallback {
	private MqttAsyncClient client;
	private String topic;
	private String publishtopic;
	private MqttConnectOptions options;
	private String Server;
	private String Port;
	private String ClientID;
	private String Republish;
	private int QOS=2;
	private boolean RETAIN=false;
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private SqlConnector sql=null;
	private IMqttToken conToken=null;
	
	public InitiateMQTT(Configuration config) throws Exception  {
		this.topic=config.getProperty("mqttTopic");
		this.publishtopic=config.getProperty("mqttTopic");
		this.Server=config.getProperty("mqttServer");
		this.Port=config.getProperty("mqttPort");
		this.ClientID=config.getProperty("mqttClientid");
		this.Republish=config.getProperty("mqttRepublish");
		//pop off the # for server publishing
		this.publishtopic=this.publishtopic.substring(0,this.publishtopic.indexOf("#"));
		options = new MqttConnectOptions();
		try {
			Properties props = new Properties();
			if( ! config.getProperty("mqttKeystore").isEmpty()) {
		        System.setProperty("javax.net.ssl.trustStore", config.getProperty("mqttKeystore"));
		        System.setProperty("javax.net.ssl.trustStorePassword", config.getProperty("mqttKeystorePW"));
		        //System.setProperty("javax.net.ssl.keyStore", config.getKEYSTORE());
		        //System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
		        client = new MqttAsyncClient("ssl://" + Server + ":" + Port , ClientID);
		        
		        props.setProperty("com.ibm.ssl.protocol", "TLSv1.2");
		        options.setSSLProperties(props);
			} else {
				client = new MqttAsyncClient("tcp://" + Server + ":" + Port , ClientID);
			}
			if(config.getProperty("mqttClean").equalsIgnoreCase("true")) {
				options.setCleanSession(true);
			} else {
				options.setCleanSession(false);
			}
			options.setPassword(config.getProperty("mqttPassword").toCharArray());
			options.setUserName(config.getProperty("mqttUsername"));
			client.setCallback(this);
			LOGGER.fine("Before Connect");
			connect();
			LOGGER.fine("After Connect");
			
		} catch (MqttException e) {
			LOGGER.fine("Exception in initiation");
			e.printStackTrace();
		}
	}
	public void connect(){
		LOGGER.info(" Connect MQTT");
		try {
			conToken = client.connect(options,null,null);
			conToken.waitForCompletion();
			SendMsg("PCon");
		} catch (Exception e) {
			LOGGER.fine("Exception in connet();");
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
			client.publish(publishtopic, msg.getBytes(),QOS,RETAIN);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(99);
		}
	}
	
	public void SendMsg(String msg, String intopic) {
		this.publishtopic=intopic;
		this.SendMsg(msg);
	}
	
	public void setTopic(String intopic) {
		this.topic = intopic;
	}
	public void setPublishTopic(String intopic) {
		this.publishtopic = intopic;
	}	
	public void setSubscribe() throws MqttException {
		LOGGER.info("Start subscription " + topic);
		client.subscribe(topic, QOS);
	}
	public void messageArrived(String ontopic, MqttMessage msg) throws Exception {
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
		LOGGER.finer(ontopic + " " + new String (msg.getPayload()));
		String data= new String(msg.getPayload());
		String user= ontopic.split("/")[1];
		
		JSONObject obj;
		//owntracks:  {"_type": "location", "lat": "58.0396857", "lon": "12.7939241", "tst": "1398365109", "acc": "25.46", "batt": "51"}
		//
		if(!data.contains("PCon")) {
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
				obj.put("dist",data2[5]);
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
				obj.put("dist","0");  //owntracks does not send
			} else {
				LOGGER.info("Unknown Topic " + ontopic);
				obj = null;
			}
			if( sql != null && obj != null) {
				LOGGER.finer("logging to SQL" + obj.toString());
				sql.addRow(obj);
			}
			if( obj != null) {
				// Here we republish to enable the webtracking
				publishtopic=Republish.replace("<user>", obj.getString("user"));
				LOGGER.finer("Republish to " + publishtopic);
				SendMsg(obj.toString(),publishtopic);
			}
		} else {
			LOGGER.info("TCon Recieved Client connected:" + user);
		}
	}

	public void setSql(SqlConnector insql) {
		sql = insql;
	}

	public void connectionLost(Throwable arg0) {
		try {
			Thread.sleep(10000);
			connect();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}

}
