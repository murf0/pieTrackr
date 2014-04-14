/**
 * 
 */
package se.murf.pietrackr;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * @author mikael
 *
 */
public class Configuration {

	/**
	 * 
	 */
	private static final String HELP_OPTION = "help";
	private static final String OPTION_PUSH = "topic";
	private static final String OPTION_SERVER = "server";
	private static final String OPTION_PORT = "port";
	private static final String OPTION_CLIENTID = "clientid";
	private static OptionSet options = null;
	public Configuration(String[] args) throws Exception {
		// TODO Auto-generated constructor stub
		
		OptionParser parser = new OptionParser() {
			{
				accepts(HELP_OPTION, "Shows this help message.");
				accepts(OPTION_PUSH, "Topic to push to.").withRequiredArg().describedAs("topic").ofType(String.class);
				accepts(OPTION_SERVER, "Server to publish to").withRequiredArg().describedAs("server").ofType(String.class);
				accepts(OPTION_PORT, "ServerPort.").withRequiredArg().describedAs("port").ofType(String.class);
				accepts(OPTION_CLIENTID, "Client ID.").withRequiredArg().describedAs("clientid").ofType(String.class);
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
	public static String getDate() {
		return new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(Calendar.getInstance().getTime());
	}
}
