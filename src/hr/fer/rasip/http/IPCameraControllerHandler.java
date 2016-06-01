package hr.fer.rasip.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import gsn.http.RequestHandler;
import gsn.utils.Base64;
import gsn.wrappers.rasip.IPCamWrapper;
import gsn.wrappers.rasip.IPKameraStreamWrapper;

public class IPCameraControllerHandler  implements RequestHandler  {

	private final transient Logger logger = Logger.getLogger(IPCamWrapper.class);
	
	private static final String STATUS_SERVLET_PATH = "/ipcamera/status";
	private static final String CONFIG_SERVLET_PATH = "/ipcamera/config";
	private static final String CONFIG_UPDATE_SERVLET_PATH = "/ipcamera/config-update";
	private static final String MOVE_RELATIVE_SERVLET_PATH = "/ipcamera/move-relative";
	private static final String MOVE_ABSOLUTE_SERVLET_PATH = "/ipcamera/move-absolute";
	

	private static String schema = "http://";
	
	private static final String CONFIG_FILE_PATH = "IPCamera/config.xml";
	
	@Override
	public boolean isValid(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		return true;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<response>\n");
		
		String servletPath = request.getServletPath();
		
		String ipAddress;
		Integer port;
		Integer profile;
		String username;
		String password;
		
		 try {
	     		SAXBuilder builder = new SAXBuilder();
	 			File xmlFile = new File(CONFIG_FILE_PATH);
	 			Document doc = (Document) builder.build(xmlFile);
	 			Element root = doc.getRootElement();
	 			
	 			//get parameters from config file
	 			Element connectionParameters = root.getChild("connection-params");
	 			ipAddress = connectionParameters.getChild("ip-address").getValue();
	 			port = Integer.valueOf(connectionParameters.getChild("port").getValue());
	 			profile = Integer.valueOf(connectionParameters.getChild("profile").getValue());
	 			username = connectionParameters.getChild("username").getValue();
	 			password = connectionParameters.getChild("password").getValue();
	 			
	     }
	     catch(Exception e){
	     		sb.append("<status>exception</status>\n<description>"+ e.getClass()+": " + e.getMessage() + "</description>\n</response>");
	 			response.setHeader("Cache-Control", "no-store");
	 	        response.setDateHeader("Expires", 0);
	 	        response.setHeader("Pragma", "no-cache");
	 	        response.getWriter().write(sb.toString());
	     		return; 
	     }
		 
		 
		  
		if(servletPath.equalsIgnoreCase(MOVE_RELATIVE_SERVLET_PATH)) {
			
			Integer X = parseNumber(request.getParameter("x"));
			Integer Y = parseNumber(request.getParameter("y"));
			
			try {
                String urlString =  schema + ipAddress + ":" + port + "/cgi/ptdc.cgi?command=set_relative_pos&posX="+ X + "&posY=" + Y;
                logger.warn("Url je: " + urlString);
                URL url = new URL(urlString);
                String authStr = username + ":" + password;
                String authEncoded = Base64.encodeToString(authStr.getBytes(), false);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", "Basic " + authEncoded);
                connection.setRequestMethod("GET");
                connection.getResponseMessage();
			} catch (MalformedURLException e) {
				return;
			} catch (ProtocolException e) {
				return;
			} catch (IOException e) {
				return;
			}	  
		}
		
		
		if(servletPath.equalsIgnoreCase(MOVE_ABSOLUTE_SERVLET_PATH)) {
			
			Integer X = parseNumber(request.getParameter("x"));
			Integer Y = parseNumber(request.getParameter("y"));
			
			try {
				String urlString =  schema + ipAddress + ":" + port + "/cgi/ptdc.cgi?command=set_pos&posX="+ X + "&posY=" + Y;
                logger.warn("Url je: " + urlString);
                URL url = new URL(urlString);
                String authStr = username + ":" + password;
                String authEncoded = Base64.encodeToString(authStr.getBytes(), false);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", "Basic " + authEncoded);
                connection.setRequestMethod("GET");
                connection.getResponseMessage();
			} catch (MalformedURLException e) {
				return;
			} catch (ProtocolException e) {
				return;
			} catch (IOException e) {
				return;
			}	  
		}
		
		
		
		
        if(servletPath.equalsIgnoreCase(STATUS_SERVLET_PATH)) {
        	
        	 ByteArrayOutputStream baos = null;
             try {
                 URL url = new URL(schema + ipAddress + ":" + port +"/video/mjpg.cgi?profileid=" + profile);

                 String authStr = username + ":" + password;
                 String authEncoded = Base64.encodeToString(authStr.getBytes(), false);

                 HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                 connection.setRequestMethod("GET");
                 connection.setRequestProperty("Authorization", "Basic " + authEncoded);

                 baos = new ByteArrayOutputStream();

                 InputStream webStream = (InputStream) connection.getContent();
                 IPKameraStreamWrapper ipStream = null;
                 try{
                     ipStream = new IPKameraStreamWrapper(webStream);
                     ipStream.writeNextImage(baos);
                 } catch (Exception e) {
                     logger.error(e);
                 } finally {
                     try {
                         if(ipStream != null)
                             ipStream.close();
                     } catch (Exception e) {
                         logger.warn("Could not close IP Cam stream: " + e.getStackTrace());
                     }
                 }

                 byte[] imageInByte = baos.toByteArray();
                 baos.close();
                         
                 response.setContentType("image/jpeg");
         
                 OutputStream out = response.getOutputStream();
         	 	 out.write(imageInByte);	 
         		 out.close();
         		 //return;
             } catch (MalformedURLException e1) {
                 logger.error(e1);
             } catch (IOException e) {
                 logger.error(e);
             } finally {
                 if (baos != null) {
                     try {
                         baos.close();
                     } catch (IOException e) {
                        logger.error(e);
                     }
                     return;
                 }
             }	
        }
        
       
        if(servletPath.equalsIgnoreCase(CONFIG_SERVLET_PATH)){
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
    		  	
    		    //control and servlet finished successfully
    			response.setHeader("Cache-Control", "no-store");
    	        response.setDateHeader("Expires", 0);
    	        response.setHeader("Pragma", "no-cache");
    	        response.getWriter().write(sb.toString());
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
	       	try{
	           	SAXBuilder builder = new SAXBuilder();
	           	
	   			File xmlFile = new File(CONFIG_FILE_PATH);
	   			Document doc = (Document) builder.build(xmlFile);
	   			Element root = doc.getRootElement();
	   			Element connectionParams = root.getChild("connection-params");
	   			
	   			if(request.getParameter("ip-address") != null && request.getParameter("ip-address").trim().length() != 0){
	   				connectionParams.getChild("ip-address").setText(request.getParameter("ip-address"));
	   			} 
	   			
	   			if(request.getParameter("port") != null && request.getParameter("port").trim().length() != 0){
	   				connectionParams.getChild("port").setText(request.getParameter("port"));
	   			}
	   			
	   			if(request.getParameter("profile") != null && request.getParameter("profile").trim().length() != 0){
	   				connectionParams.getChild("profile").setText(request.getParameter("profile"));
	   			}
	   			
	   			
	   			if(request.getParameter("username") != null && request.getParameter("username").trim().length() != 0){
	   				connectionParams.getChild("username").setText(request.getParameter("username"));
	   			}
	   			
	   			
	   			if(request.getParameter("password") != null && request.getParameter("password").trim().length() != 0){
	   				connectionParams.getChild("password").setText(request.getParameter("password"));
	   			}
	   			
	   			// save updated xml file
	   	        XMLOutputter xmlOutput = new XMLOutputter();
	   	   	 	xmlOutput.setFormat(Format.getPrettyFormat());
	   			xmlOutput.output(doc, new FileWriter(CONFIG_FILE_PATH));
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
	}
	
	
	private int parseNumber(String number){
        if(number == null || number.equals("") )
            return 0;
        else{
            int x = Integer.parseInt(number);

            return x;
        }

    }
}
