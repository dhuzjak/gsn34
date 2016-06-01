package hr.fer.rasip.http;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gsn.http.RequestHandler;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class MqttConfigHandler  implements RequestHandler  {

	
	private static final String CONFIG_SERVLET_PATH = "/mqtt/config";
	private static final String CONFIG_FILE_PATH = "mqtt/config.xml";

		
	@Override
	public boolean isValid(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		 return true;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
  
		
		StringBuilder sb = new StringBuilder("");
		String servletPath = request.getServletPath();

		
	     
		if(servletPath.equalsIgnoreCase(CONFIG_SERVLET_PATH)) {

		 try {
			FileInputStream fstream = new FileInputStream(CONFIG_FILE_PATH);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null){
			  	// Print the content on the console
			  	sb.append(strLine + "\n");
			 }
			 in.close();
	        }
	        catch(Exception e){
	        	sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	        	sb.append("<status>exception</status>\n<description>"+ e.getClass()+": " + e.getMessage() + "</description>\n");
	    		response.setHeader("Cache-Control", "no-store");
	    	    response.setDateHeader("Expires", 0);
	    	    response.setHeader("Pragma", "no-cache");
	    	    response.getWriter().write(sb.toString());
	        	return; 
	        }	
	     }

	     //control and servlet finished successfully
		 response.setHeader("Cache-Control", "no-store");
	     response.setDateHeader("Expires", 0);
	     response.setHeader("Pragma", "no-cache");
	     response.getWriter().write(sb.toString());
	}
	
}
