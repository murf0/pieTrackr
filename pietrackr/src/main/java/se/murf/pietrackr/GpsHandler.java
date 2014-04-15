package se.murf.pietrackr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;



public class GpsHandler {
	int port=0;
	String server="NONE";
	
	public GpsHandler(Configuration config) {
		port = config.getGPSDPORT();
		server = config.getGPSDSERVER();
	}
	public void run() {
		try{
			Socket socket = new Socket(server, port);
			PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String text = "?DEVICES;";
			out.println(text);
			try{
				String line = in.readLine();
				System.out.println("Text received: " + line);
			} catch (IOException e){
				System.out.println("Read failed");
				System.exit(1);
			}
			socket.close();
  		} catch (UnknownHostException e) {
  			System.out.println("Unknown host: kq6py");
  			System.exit(1);
		} catch  (IOException e) {
	   		System.out.println("No I/O");
     		System.exit(1);
   		}
		
	}


}
