package hr.fer.rasip.http;

import gsn.http.RequestHandler;

import hr.fer.rasip.passiveHeating.control.*;

import gsn.Main;

import java.io.IOException;

import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.InetAddress;


public class PassiveHeatingControllerHandler implements RequestHandler {

	private static final String CONTROL_SERVLET_PATH = "/passiveheating/control";
	
	private static final String AUTOCONTROL_SERVLET_PATH = "/passiveheating/autocontrol";
	
	private static final String AUTO_CONTROL_DISABLE = "/passiveheating/autocontrol-disable";
	
	private static final String CONFIG_SERVLET_PATH = "/passiveheating/config";
	
	private static final String CONFIG_UPDATE_SERVLET_PATH = "/passiveheating/config-update";
	
	private static final String AIR_SERVLET_PATH = "/passiveheating/air";
	
	private static final String FAN_PARAMETER_STRING = "fan";
	
	private static final String HEATER_PARAMETER_STRING = "heater";
	
	private static final String INTAKE_PARAMETER_STRING = "intake";
	
	private static final String INTAKE_NORMAL_STRING = "normal";
	
	private static final String INTAKE_OVERRIDE_STRING = "override";
	
	private static final String CONFIG_FILE_PATH = "passiveHeating/config.xml";
	
	private static transient Logger logger = Logger.getLogger(PassiveHeatingControllerHandler.class);

	public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
        Main.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(Main.getContainerConfig().getTimeFormat());
        
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<response>\n");
        
        String servletPath = request.getServletPath();
        
       
        if(servletPath.equalsIgnoreCase(CONFIG_UPDATE_SERVLET_PATH)) {
        	
        	 int externalTempLimit = Integer.MAX_VALUE;
             int internalTempLimit1 = Integer.MAX_VALUE;
             int internalTempLimit2 = Integer.MAX_VALUE;
             int internalTempLimit3 = Integer.MAX_VALUE;
             int state1Fan = -1;
             int state1Heater = -1;
             int state2Fan = -1;
             int state2Heater = -1;
             int state3Fan = -1;
             int state3Heater = -1;
             int state4Fan = -1;
             int state4Heater = -1;
             int state5Fan = -1;
             int state5Heater = -1;
             String rabbitIP = "-1";
             int freeServerPort = -1;
             /*int autoControl = -1;
             int manualFan = -1;
             int manualHeater = -1;
             int airIntake = -1;*/
             String email = "-1";
             
             if(request.getParameter("externalTempLimit") != null && request.getParameter("externalTempLimit").trim().length() != 0){
            	 externalTempLimit = Integer.parseInt(request.getParameter("externalTempLimit"));
         	 }
                
             if(request.getParameter("internalTempLimit1") != null && request.getParameter("internalTempLimit1").trim().length() != 0){
            	 internalTempLimit1 = Integer.parseInt(request.getParameter("internalTempLimit1"));
         	 }
             
             if(request.getParameter("internalTempLimit2") != null && request.getParameter("internalTempLimit2").trim().length() != 0){
            	 internalTempLimit2 = Integer.parseInt(request.getParameter("internalTempLimit2"));
         	 }
             
             if(request.getParameter("internalTempLimit3") != null && request.getParameter("internalTempLimit3").trim().length() != 0){
            	 internalTempLimit3 = Integer.parseInt(request.getParameter("internalTempLimit3"));
         	 }
             
             if(request.getParameter("state1-fan") != null && request.getParameter("state1-fan").trim().length() != 0){
            	 state1Fan = Integer.parseInt(request.getParameter("state1-fan"));
         	 }
             
             if(request.getParameter("state1-heater") != null && request.getParameter("state1-heater").trim().length() != 0){
            	 state1Heater = Integer.parseInt(request.getParameter("state1-heater"));
         	 }
               
             if(request.getParameter("state2-fan") != null && request.getParameter("state2-fan").trim().length() != 0){
            	 state2Fan = Integer.parseInt(request.getParameter("state2-fan"));
         	 }
             
             if(request.getParameter("state2-heater") != null && request.getParameter("state2-heater").trim().length() != 0){
            	 state2Heater = Integer.parseInt(request.getParameter("state2-heater"));
         	 }
             
             if(request.getParameter("state3-fan") != null && request.getParameter("state3-fan").trim().length() != 0){
            	 state3Fan = Integer.parseInt(request.getParameter("state3-fan"));
         	 }
             
             if(request.getParameter("state3-heater") != null && request.getParameter("state3-heater").trim().length() != 0){
            	 state3Heater = Integer.parseInt(request.getParameter("state3-heater"));
         	 }
             
             if(request.getParameter("state4-fan") != null && request.getParameter("state4-fan").trim().length() != 0){
            	 state4Fan = Integer.parseInt(request.getParameter("state4-fan"));
         	 }
             
             if(request.getParameter("state4-heater") != null && request.getParameter("state4-heater").trim().length() != 0){
            	 state4Heater = Integer.parseInt(request.getParameter("state4-heater"));
         	 }
             
             if(request.getParameter("state5-fan") != null && request.getParameter("state5-fan").trim().length() != 0){
            	 state5Fan = Integer.parseInt(request.getParameter("state5-fan"));
         	 }
             
             if(request.getParameter("state5-heater") != null && request.getParameter("state5-heater").trim().length() != 0){
            	 state5Heater = Integer.parseInt(request.getParameter("state5-heater"));
         	 }
             
             if(request.getParameter("rabbit-ip") != null && request.getParameter("rabbit-ip").trim().length() != 0){
            	 rabbitIP = request.getParameter("rabbit-ip");
         	 }
             
             if(request.getParameter("free-server-port") != null && request.getParameter("free-server-port").trim().length() != 0){
            	 freeServerPort = Integer.parseInt(request.getParameter("free-server-port"));
         	 }
             
             if(request.getParameter("email") != null && request.getParameter("email").trim().length() != 0){
            	 email = request.getParameter("email");
         	 }
             
                    
        	try{
            	SAXBuilder builder = new SAXBuilder();
            	
    			File xmlFile = new File(CONFIG_FILE_PATH);
    			Document doc = (Document) builder.build(xmlFile);
    			Element root = doc.getRootElement();
    			Element corePrams = root.getChild("core-parameters");
    			
    			if(externalTempLimit != Integer.MAX_VALUE){
    				corePrams.getChild("externalTempLimit").setText(String.valueOf(externalTempLimit));
    			}
    			
    			if(internalTempLimit1 != Integer.MAX_VALUE){
    				corePrams.getChild("internalTempLimit1").setText(String.valueOf(internalTempLimit1));
    			}
    			
    			
    			if(internalTempLimit2 != Integer.MAX_VALUE){
    				corePrams.getChild("internalTempLimit2").setText(String.valueOf(internalTempLimit2));
    			}
    			
    			if(internalTempLimit3 != Integer.MAX_VALUE){
    				corePrams.getChild("internalTempLimit3").setText(String.valueOf(internalTempLimit3));
    			}
    			
    			
    			if(state1Fan != -1){
    				corePrams.getChild("state1").getChild("fan").setText(String.valueOf(state1Fan));
    			} 
    			
    			if(state1Heater != -1){
    				corePrams.getChild("state1").getChild("heater").setText(String.valueOf(state1Heater));
    			} 
    			
  
    			if(state2Fan != -1){
    				corePrams.getChild("state2").getChild("fan").setText(String.valueOf(state2Fan));
    			} 
    			
    			if(state2Heater != -1){
    				corePrams.getChild("state2").getChild("heater").setText(String.valueOf(state2Heater));
    			}
    			
    			
    			if(state3Fan != -1){
    				corePrams.getChild("state3").getChild("fan").setText(String.valueOf(state3Fan));
    			} 
    			
    			if(state3Heater != -1){
    				corePrams.getChild("state3").getChild("heater").setText(String.valueOf(state3Heater));
    			}
    			
    			if(state4Fan != -1){
    				corePrams.getChild("state4").getChild("fan").setText(String.valueOf(state4Fan));
    			} 
    			
    			if(state4Heater != -1){
    				corePrams.getChild("state4").getChild("heater").setText(String.valueOf(state4Heater));
    			}
    			
    			if(state5Fan != -1){
    				corePrams.getChild("state5").getChild("fan").setText(String.valueOf(state5Fan));
    			} 
    			
    			if(state5Heater != -1){
    				corePrams.getChild("state5").getChild("heater").setText(String.valueOf(state5Heater));
    			}
    			
    			if(!rabbitIP.equals("-1")){
    				corePrams.getChild("rabbit-ip").setText(rabbitIP);
    			}
    			
    			if(freeServerPort != -1){
    				corePrams.getChild("free-server-port").setText(String.valueOf(freeServerPort));
    			}
    			
    			if( !email.equals("-1")){
    				root.getChild("notifications").getChild("email").setText(email);
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
        
        
       
        if(servletPath.equalsIgnoreCase(CONTROL_SERVLET_PATH)){
        	//control servlet
        	int fan = -1;
        	int heater = -1;
        	if(request.getParameter(FAN_PARAMETER_STRING) != null && request.getParameter(FAN_PARAMETER_STRING).trim().length() != 0){
        		fan = Integer.parseInt(request.getParameter(FAN_PARAMETER_STRING));
        	}
        	if(request.getParameter(HEATER_PARAMETER_STRING) != null && request.getParameter(HEATER_PARAMETER_STRING).trim().length() != 0){
        		heater = Integer.parseInt(request.getParameter(HEATER_PARAMETER_STRING));
        	}
        	
        	try {
        		//ovdje eventualno staviti kasnjenje
        		Thread.sleep(2000);
        		SAXBuilder builder = new SAXBuilder();
    			File xmlFile = new File(CONFIG_FILE_PATH);
    			Document doc = (Document) builder.build(xmlFile);
    			Element root = doc.getRootElement();
    			Element coreParameters = root.getChild("core-parameters");
    			String rabbitIP = coreParameters.getChild("rabbit-ip").getValue();
    			int freeServerPort = Integer.parseInt(coreParameters.getChild("free-server-port").getValue());
        		InetAddress localInetAddress = InetAddress.getLocalHost();
				InetAddress remoteInetAddress = InetAddress.getByName(rabbitIP);
				Control control = new Control(localInetAddress, freeServerPort, remoteInetAddress);
				if(fan != -1){
					control.setFanPower(fan);
				}
				if(heater != -1){
					control.setHeaterState((heater==1)?true:false);
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
        	
        	try{
            	SAXBuilder builder = new SAXBuilder();
            	
    			File xmlFile = new File(CONFIG_FILE_PATH);
    			Document doc = (Document) builder.build(xmlFile);
    			Element root = doc.getRootElement();
    			Element state = root.getChild("state");
  
    			//disable auto control
    			state.getChild("auto-control").setText("0");
    			if(fan != -1){
    				state.getChild("manual-fan").setText(String.valueOf(fan));
    			}
    			if(heater != -1){
    				state.getChild("manual-heater").setText(String.valueOf(heater));
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
        
        	sb.append("<status>ok</status>\n<description>Passive heating values set to "
        				+ ((fan != -1)?"fan = " + fan + " ":" "  )
        				+ ((heater != -1)?"heater = " + heater + " ":" "  ) + "</description>\n</response>");
        	
        }
        
        
        if(servletPath.equalsIgnoreCase(AUTO_CONTROL_DISABLE)){
        	//auto control servlet
        	try {
        		SAXBuilder builder = new SAXBuilder();
    			File xmlFile = new File(CONFIG_FILE_PATH);
    			Document doc = (Document) builder.build(xmlFile);
    			Element root = doc.getRootElement();
    			
    			root.getChild("state").getChild("auto-control").setText("0");
    			
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
        	sb.append("<status>ok</status>\n<description>Auto control disabled</description></response>\n");
        }

        if(servletPath.equalsIgnoreCase(AUTOCONTROL_SERVLET_PATH)){
        	//auto control servlet
        	try {
        		SAXBuilder builder = new SAXBuilder();
    			File xmlFile = new File(CONFIG_FILE_PATH);
    			Document doc = (Document) builder.build(xmlFile);
    			Element root = doc.getRootElement();
    			
    			root.getChild("state").getChild("auto-control").setText("1");
    			
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
        	sb.append("<status>ok</status>\n<description>Auto control active</description></response>\n");
        }
        
        if(servletPath.equalsIgnoreCase(CONFIG_SERVLET_PATH)){
        	//config servlet
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
        
        if(servletPath.equalsIgnoreCase(AIR_SERVLET_PATH)){
        	
        	try {
        		SAXBuilder builder = new SAXBuilder();
    			File xmlFile = new File(CONFIG_FILE_PATH);
    			Document doc = (Document) builder.build(xmlFile);
    			Element root = doc.getRootElement();
    			
    			//get parameters from config file
    			Element coreParameters = root.getChild("core-parameters");
    			String rabbitIP = coreParameters.getChild("rabbit-ip").getValue();
    			int freeServerPort = Integer.parseInt(coreParameters.getChild("free-server-port").getValue());
        		InetAddress localInetAddress = InetAddress.getLocalHost();
				InetAddress remoteInetAddress = InetAddress.getByName(rabbitIP);
				Control control = new Control(localInetAddress, freeServerPort, remoteInetAddress);
						
    			//set value to config file and send command to system
    			if(request.getParameter(INTAKE_PARAMETER_STRING).equalsIgnoreCase(INTAKE_NORMAL_STRING)){
    				control.setAirIntake(AirIntake.NORMAL);
    				root.getChild("state").getChild("air-intake").setText(INTAKE_NORMAL_STRING);				
    			}
    			if(request.getParameter(INTAKE_PARAMETER_STRING).equalsIgnoreCase(INTAKE_OVERRIDE_STRING)){
    				control.setAirIntake(AirIntake.OVERRIDE);
    				root.getChild("state").getChild("air-intake").setText(INTAKE_OVERRIDE_STRING);
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
        	
        	if(request.getParameter(INTAKE_PARAMETER_STRING).equalsIgnoreCase(INTAKE_NORMAL_STRING)){
        		sb.append("<status>ok</status>\n<description>Air intake set to " + INTAKE_NORMAL_STRING + "</description></response>\n");
			}
        	else{
        		sb.append("<status>ok</status>\n<description>Air intake set to " + INTAKE_OVERRIDE_STRING + "</description></response>\n");
        	}
        }
    	
        //control and servlet finished successfully
		response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-cache");
        response.getWriter().write(sb.toString());
       
    }

    public boolean isValid(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
    	boolean oneParameter = false;
    	
        String servletPath = request.getServletPath();
        
        if(servletPath.equalsIgnoreCase(CONFIG_UPDATE_SERVLET_PATH)) {
           	
        	
        }
        else if(servletPath.equalsIgnoreCase(CONTROL_SERVLET_PATH)){
        	//control servlet
        	if(request.getParameter(FAN_PARAMETER_STRING) != null && request.getParameter(FAN_PARAMETER_STRING).trim().length() != 0){
        		int fan = Integer.parseInt(request.getParameter(FAN_PARAMETER_STRING));
        		if((fan < 0)|| (fan > 5)){
        			response.sendError(WebConstants.UNSUPPORTED_REQUEST_ERROR, "Unsupported value for parameter " + FAN_PARAMETER_STRING );
        			return false;
        		}
        		else{
        			oneParameter = true;
        		}
        	}
        	if(request.getParameter(HEATER_PARAMETER_STRING) != null && request.getParameter(HEATER_PARAMETER_STRING).trim().length() != 0){
        		int heater = Integer.parseInt(request.getParameter(HEATER_PARAMETER_STRING));
        		if ((heater < 0) || (heater > 1)){
        			response.sendError(WebConstants.UNSUPPORTED_REQUEST_ERROR, "Unsupported value for parameter " + HEATER_PARAMETER_STRING + ".");
        			return false;
        		}
        		else{
        			oneParameter = true;
        		}
        	}
        	if(!oneParameter){
        		response.sendError(WebConstants.UNSUPPORTED_REQUEST_ERROR, "Unsupported parameters. Parameters for " + CONTROL_SERVLET_PATH + " are " + HEATER_PARAMETER_STRING + " and " + FAN_PARAMETER_STRING + ".");
    			return false;
        	}
        	
        }
        else{
        	if(servletPath.equalsIgnoreCase(AUTOCONTROL_SERVLET_PATH)){
        		//switch to auto control servlet
        	}
        	else if(servletPath.equalsIgnoreCase(AUTO_CONTROL_DISABLE)){
        		
        	}
        	else{
        		if(servletPath.equalsIgnoreCase(CONFIG_SERVLET_PATH)){
        			//return passive heating config file servlet
        		}
        		else{
        			if(servletPath.equalsIgnoreCase(AIR_SERVLET_PATH)){
        				if(request.getParameter(INTAKE_PARAMETER_STRING) == null || request.getParameter(INTAKE_PARAMETER_STRING).trim().length() == 0){
        					response.sendError(WebConstants.UNSUPPORTED_REQUEST_ERROR, "Unsupported parameter. Try with \"" + INTAKE_PARAMETER_STRING + "\"" );
                			return false;
        				}
        				if(request.getParameter(INTAKE_PARAMETER_STRING).equalsIgnoreCase(INTAKE_NORMAL_STRING) == false  && request.getParameter(INTAKE_PARAMETER_STRING).equalsIgnoreCase(INTAKE_OVERRIDE_STRING) == false){
        					response.sendError(WebConstants.UNSUPPORTED_REQUEST_ERROR, "Unsupported " + INTAKE_PARAMETER_STRING + " parameter value. Allowed is: " + INTAKE_NORMAL_STRING + " or " + INTAKE_OVERRIDE_STRING + ".");
                			return false;
        				}
        				
        			}
        			else{
        				response.sendError(WebConstants.UNSUPPORTED_REQUEST_ERROR, "Unknown url-pattern for PassiveHeatingControllServlet. Check web.xml.");
        				logger.error("Validation failed. Unknown url-pattern for PassiveHeatingControllServlet. Check web.xml");
        				return false;
        			}
        			
        		}
        	}
        }        
        return true;
    }
}

