package se.murf.pietrackr;

import java.io.IOException;
import java.util.Date;

import joptsimple.OptionException;
import joptsimple.OptionParser;

import joptsimple.OptionSet;

/**
 * @author Murf Mellgren 20140414
 * daemon running on Raspberry pie for tracking GPS positions
 * https://trackrd.murf.se
 */
public class PieTrackr  {
	private static final String HELP_OPTION = "help";
	private static final String OPTION_PUSH = "pushtopic";
	private static final String OPTION_SERVER = "pushtopic";
	private static final String OPTION_PORT = "pushtopic";
	
	public static void main( String[] args ) {
		
		OptionParser parser = new OptionParser() {
			{
				accepts(HELP_OPTION, "Shows this help message.");
				accepts(OPTION_PUSH, "Topic to push to.").withRequiredArg().describedAs("pushtopic").ofType(String.class);

			}
		};
		
        System.out.println( "Hello World!" );
    }
}
