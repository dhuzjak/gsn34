package hr.fer.rasip.http;

import gsn.Main;
import gsn.http.RequestHandler;


//import gsn.wrappers.rasip.PowerSupplyModel.*;
import gsn.wrappers.rasip.RelayModel.Control;

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

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class RelayControllerHandler implements RequestHandler {
	
	private static transient Logger logger = Logger.getLogger(RelayControllerHandler.class);
	
	private static final String RELAY_STATUS = "/relay/status";
	
	private static final String CONFIG_SERVLET_PATH = "/relay/config";
	
	private static final String CONFIG_UPDATE_SERVLET_PATH = "/relay/config-update";
	
	private static final String COMMAND_SERVLET_PATH = "/relay/command";
	
	private static final String CONFIG_FILE_PATH = "relay/config.xml";
	
	private static final String RELAY_NAME_PARAMETER = "name";
	
	private static final String ACTION_PARAMETER = "action";

	private static final String ACTION_PARAMETER_ON = "ON";
	
	private static final String ACTION_PARAMETER_OFF = "OFF";
	
	private Control relayControl;
	
	
	@Override
	public boolean isValid(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		 return true;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		 Main.getInstance();
		 SimpleDateFormat sdf = new SimpleDateFormat(Main.getContainerConfig().getTimeFormat());
	        
	     StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<response>\n");
	        
	     String servletPath = request.getServletPath();
	     
	     String remoteIPAddress = null;
	     int localPort;
	     int remotePort;
	     HashMap<String, Integer> relays = new HashMap<String, Integer>();
	     List<String> relayNames = new ArrayList<String>();
	     
	     try {
     		SAXBuilder builder = new SAXBuilder();
 			File xmlFile = new File(CONFIG_FILE_PATH);
 			Document doc = (Document) builder.build(xmlFile);
 			Element root = doc.getRootElement();
 			
 			//get parameters from config file
 			Element connectionParameters = root.getChild("connection-params");
 			remoteIPAddress = connectionParameters.getChild("ip-address").getValue();
 			localPort = Integer.valueOf(connectionParameters.getChild("local-port").getValue());
 			remotePort = Integer.valueOf(connectionParameters.getChild("remote-port").getValue());
 			
 			for(Element relay :  (List<Element>) root.getChild("relays").getChildren()){
 				relays.put(relay.getChild("displayName").getValue(), 
 						Integer.valueOf(relay.getChild("id").getValue()));
 				relayNames.add(relay.getChild("displayName").getValue());
 			}
     	}
     	catch(Exception e){
     		sb.append("<status>exception</status>\n<description>"+ e.getClass()+": " + e.getMessage() + "</description>\n</response>");
 			response.setHeader("Cache-Control", "no-store");
 	        response.setDateHeader("Expires", 0);
 	        response.setHeader("Pragma", "no-cache");
 	        response.getWriter().write(sb.toString());
     		return; 
     	}
	     
	     if(servletPath.equalsIgnoreCase(CONFIG_SERVLET_PATH)) {
	    	 sb = new StringBuilder("");
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
	        	sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<response>\n");
	        	sb.append("<status>exception</status>\n<description>"+ e.getClass()+": " + e.getMessage() + "</description>\n</response>");
	    		response.setHeader("Cache-Control", "no-store");
	    	    response.setDateHeader("Expires", 0);
	    	    response.setHeader("Pragma", "no-cache");
	    	    response.getWriter().write(sb.toString());
	        	return; 
	        }	
	     }
	    
	     if(servletPath.equalsIgnoreCase(CONFIG_UPDATE_SERVLET_PATH)) {
	    	 try {
	      		SAXBuilder builder = new SAXBuilder();
	  			File xmlFile = new File(CONFIG_FILE_PATH);
	  			Document doc = (Document) builder.build(xmlFile);
	  			Element root = doc.getRootElement();
	  				
	  			//get parameters from config file
	  			Element connectionParameters = root.getChild("connection-params");
	  			connectionParameters.getChild("ip-address").setText(request.getParameter("ip-address"));
	  			connectionParameters.getChild("local-port").setText(request.getParameter("local-port"));
	  			connectionParameters.getChild("remote-port").setText(request.getParameter("remote-port"));
	  			
	  			Element relaysElement = root.getChild("relays");
	  			
	  			relaysElement.removeChildren("relay");
	  			
	  			Integer numberOfRelays = Integer.valueOf(request.getParameter("numberOfRelays"));
	  			
	  			for(int i = 0; i < numberOfRelays; ++i) {
	  				Element relayElement = new Element("relay");
	  				
	  				relayElement.addContent(
	  					new Element("displayName").
	  							setText(request.getParameter("relay["+i+"].displayName")));
	  				
	  				relayElement.addContent(
		  				new Element("id").
		  						setText(request.getParameter("relay["+i+"].id")));
	  				
	  				relaysElement.addContent(relayElement);
	  			}
	  					
	  		    // save updated xml file
    	        XMLOutputter xmlOutput = new XMLOutputter();
    	   	 	xmlOutput.setFormat(Format.getPrettyFormat());
    			xmlOutput.output(doc, new FileWriter(CONFIG_FILE_PATH));
    			
    			sb.append("<status>ok</status>");
	      	}
	      	catch(Exception e){
	      		sb.append("<status>exception</status>\n<description>"+ e.getClass()+": " + e.getMessage() + "</description>\n</response>");
	  			response.setHeader("Cache-Control", "no-store");
	  	        response.setDateHeader("Expires", 0);
	  	        response.setHeader("Pragma", "no-cache");
	  	        response.getWriter().write(sb.toString());
	      		return; 
	      	}	 
	     }
	     
	     if(servletPath.equalsIgnoreCase(RELAY_STATUS)) {
	    	 InetAddress local = InetAddress.getLocalHost();
			 InetAddress address = InetAddress.getByName(remoteIPAddress);
				
	         try {
				relayControl = new Control(local, localPort, address, remotePort);
				
				boolean[] status = relayControl.getOutputStatus();
				
				sb.append("<relays>\n");
				
				for(int i=0; i < relayNames.size(); ++i){
					sb.append("<relay>\n");
					sb.append("<displayName>"+relayNames.get(i)+"</displayName>");
					sb.append("<id>"+relays.get(relayNames.get(i)) +"</id>");
					sb.append("<status>"+status[ relays.get(relayNames.get(i))]+"</status>");
					sb.append("</relay>\n");
				}
				
				sb.append("</relays>");
				
	         } catch (Exception e) {
				sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<response>\n");
		        sb.append("<status>exception</status>\n<description>"+ e.getClass()+": " + e.getMessage() + "</description>\n</response>");
		    	response.setHeader("Cache-Control", "no-store");
		    	response.setDateHeader("Expires", 0);
		    	response.setHeader("Pragma", "no-cache");
		    	response.getWriter().write(sb.toString());
		    	return;	
			} 	 
	     }
	     
	     
	     if(servletPath.equalsIgnoreCase(COMMAND_SERVLET_PATH)) {
	    	 
	    	 InetAddress local = InetAddress.getLocalHost();
			 InetAddress address = InetAddress.getByName(remoteIPAddress);
				
	         try {
				relayControl = new Control(local, localPort, address, remotePort);
				
				String name = null;
				String action = null;
				
				if(request.getParameter(RELAY_NAME_PARAMETER) != null && request.getParameter(RELAY_NAME_PARAMETER).trim().length() != 0){
	        		name = request.getParameter(RELAY_NAME_PARAMETER);
	        	}
				
				if(request.getParameter(ACTION_PARAMETER) != null && request.getParameter(ACTION_PARAMETER).trim().length() != 0){
	        		action = request.getParameter(ACTION_PARAMETER);
	        	}
				
				relayControl.setOutputState(relays.get(name)+1,action.equalsIgnoreCase(ACTION_PARAMETER_ON) ? true :false);
					
				sb.append("<relay><displayName>"+name+"</displayName><status>"+action+"</status></relay>");
				
	         } catch (Exception e) {
				sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<response>\n");
		        sb.append("<status>exception</status>\n<description>"+ e.getClass()+": " + e.getMessage() + "</description>\n</response>");
		    	response.setHeader("Cache-Control", "no-store");
		    	response.setDateHeader("Expires", 0);
		    	response.setHeader("Pragma", "no-cache");
		    	response.getWriter().write(sb.toString());
		    	return;	
			} 
	     }
	    
	     
	     
	     sb.append("\n</response>");
	     //control and servlet finished successfully
		 response.setHeader("Cache-Control", "no-store");
	     response.setDateHeader("Expires", 0);
	     response.setHeader("Pragma", "no-cache");
	     response.getWriter().write(sb.toString());
	}
}
