/**
 * 
 */
package se.murf.pietrackr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.codec.binary.Base64;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.commons.io.FileUtils;

/**
 * @author mikael
 *
 */
public class Configuration {

	/**
	 * 
	 */
	private static final String HELP_OPTION = "help";
	private static final String OPTION_PUSH = "mqtttopic";
	private static final String OPTION_SERVER = "mqttserver";
	private static final String OPTION_PORT = "mqttport";
	private static final String OPTION_CLIENTID = "mqttclientid";
	private static final String OPTION_KEYSTORE = "keystore";
	private static final String OPTION_GPSDSERVER = "gpsdserver";
	private static final String OPTION_GPSDPORT = "gpsdport";
	private static final String WRITE_OPTION = "configfilewriter";
	private static final String OPTION_USER = "username";
	private static final String OPTION_USERPW = "password";
	
	private static OptionSet options = null;
	public Configuration(String[] args) throws Exception {
		if(new File(args[1]).isFile()) {
			args = readConfigFile(args[1]);
		}
		
		OptionParser parser = new OptionParser() {
			{
				accepts(HELP_OPTION, "Shows this help message.");
				accepts(WRITE_OPTION, "Writes pietrackrd.conf");
				accepts(OPTION_PUSH, "Topic to push to.").withRequiredArg().describedAs("mqtttopic").ofType(String.class);
				accepts(OPTION_SERVER, "Server to publish to").withRequiredArg().describedAs("mqttserver").ofType(String.class);
				accepts(OPTION_PORT, "ServerPort.").withRequiredArg().describedAs("mqttport").ofType(String.class);
				accepts(OPTION_CLIENTID, "MQTT Clientid").withRequiredArg().describedAs("mqttclientid").ofType(String.class);
				accepts(OPTION_KEYSTORE, "MQTT Keystore").withOptionalArg().describedAs("keystore").ofType(String.class);
				accepts(OPTION_GPSDSERVER, "GPSd ServerIP.").withOptionalArg().describedAs("gpsdserver").ofType(String.class);
				accepts(OPTION_GPSDPORT, "GPSd Port.").withOptionalArg().describedAs("gpsdport").ofType(int.class);
				accepts(OPTION_USER, "MQTT UserID").withRequiredArg().describedAs("username").ofType(String.class);
				accepts(OPTION_USERPW, "MQTT User Password").withRequiredArg().describedAs("password").ofType(String.class);
			}
		};
		/*
		 * OptionSet options = null;
		 */
		try {
			options = parser.parse(args);
		} catch (OptionException oe) {
			System.out.println(oe.toString());
			printUsage(parser);
			System.exit(1);
		}
		if (!options.hasArgument(OPTION_PUSH) || !options.hasArgument(OPTION_SERVER) || !options.hasArgument(OPTION_PORT)) {
			printUsage(parser);
			System.exit(1);
		}
		if(options.hasArgument(WRITE_OPTION)) {
			writeConfigFile(args,"pietrackrd.conf");
		}
		
		
	}
	private String[] readConfigFile(String file) throws ClassNotFoundException {
		String yourString="N/A";
		String[] args = null;
		try {
			yourString = FileUtils.readFileToString(new File(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		ByteArrayInputStream in = new ByteArrayInputStream(Base64.decodeBase64(yourString.getBytes()));
		try {
			args = (String[]) new ObjectInputStream(in).readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return args;
	}
	private void writeConfigFile(String[] args, String file) {
	    try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			new ObjectOutputStream(out).writeObject(args);
		    String yourString = new String(Base64.encodeBase64(out.toByteArray()));
		    FileUtils.writeStringToFile(new File(file), yourString);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	private static void printUsage(OptionParser parser) throws IOException {
		System.out.println("Usage:");
		System.out.println("      ");
		parser.printHelpOn(System.out);
	}
	public String getPUSH() {
		return (String) options.valueOf(OPTION_PUSH);
	}
	public String getSERVER() {
		return (String) options.valueOf(OPTION_SERVER);
	}
	public  String getPORT() {
		return (String) options.valueOf(OPTION_PORT);
	}
	public  String getCLIENTID() {
		return (String) options.valueOf(OPTION_CLIENTID);
	}
	public String getGPSDSERVER() {
		return (String) options.valueOf(OPTION_GPSDSERVER);
	}
	public String getKEYSTORE() {
		return (String) options.valueOf(OPTION_KEYSTORE);
	}
	public int getGPSDPORT() {
		return (Integer) options.valueOf(OPTION_GPSDPORT);
	}
	public static String getDate() {
		return new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(Calendar.getInstance().getTime());
	}
	public boolean getSSL() {
		return options.hasArgument(OPTION_KEYSTORE);
	}
	public String getUserName() {
		return (String) options.valueOf(OPTION_USER);
	}
	public char[] getPassword() {
		String str = (String) options.valueOf(OPTION_USERPW);
		return str.toCharArray();
	}
}
