package hr.fer.rasip.http;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gsn.http.RequestHandler;


public class MQTTWatchdogControllerHandler  implements RequestHandler  {

	
	private static final String CREATE_SERVLET_PATH = "/watchdog/create";
	private static final String DEACTIVATE_SERVLET_PATH = "/watchdog/deactivate";
	
	
	private static final String DELAY_PARAM = "delay";
	private static final String CRITICAL_PERIOD = "criticalPeriod";
	private static final String SENSOR = "sensor";
	private static final String MQTT = "mqtt";
	
	@Override
	public boolean isValid(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		if(request.getServletPath().equalsIgnoreCase(CREATE_SERVLET_PATH)){
			if(request.getParameter(DELAY_PARAM) == null)
				return false;
			
			if(request.getParameter(CRITICAL_PERIOD) == null)
				return false;
			
			if(request.getParameter(MQTT) == null)
				return false;
			
			if(request.getParameter(SENSOR) == null)
				return false;
		}
		
		if(request.getServletPath().equalsIgnoreCase(DEACTIVATE_SERVLET_PATH)){
			if(request.getParameter(SENSOR) == null)
				return false;
		}
		
		return true;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		String servletPath = request.getServletPath();
		
		if(servletPath.equalsIgnoreCase(DEACTIVATE_SERVLET_PATH)){
			String sensor = request.getParameter(SENSOR);
			StringBuilder resp = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<response>\n");
			File file = new File("virtual-sensors/"+ sensor + ".xml");
			// if file doesnt exists, then create it
			if (file.exists()) {
				if(file.delete()){
					resp.append("<status>ok</status>\n<description>Watchdog deactivated</description>\n</response>");
					response.setHeader("Cache-Control", "no-store");
			        response.setDateHeader("Expires", 0);
			        response.setHeader("Pragma", "no-cache");
			        response.getWriter().write(resp.toString());
					return;
				}
				else{
					resp.append("<status>exception</status>\n<description>Watchdog deactivation failed!!!</description>\n</response>");
					response.setHeader("Cache-Control", "no-store");
			        response.setDateHeader("Expires", 0);
			        response.setHeader("Pragma", "no-cache");
			        response.getWriter().write(resp.toString());
					return;
				}
			}else{
				resp.append("<status>exception</status>\n<description>Watchdog does not exist!!!</description>\n</response>");
				response.setHeader("Cache-Control", "no-store");
		        response.setDateHeader("Expires", 0);
		        response.setHeader("Pragma", "no-cache");
		        response.getWriter().write(resp.toString());
				return;
			}
		}
		
		
		if(servletPath.equalsIgnoreCase(CREATE_SERVLET_PATH)){
			
			String sensor = request.getParameter(SENSOR);

			Integer delay = Integer.parseInt(request.getParameter(DELAY_PARAM))*1000;
			Integer criticalPeriod = Integer.parseInt(request.getParameter(CRITICAL_PERIOD))*1000;
			String generatedSensorName = "MqttWatchdog"+Long.toString(System.currentTimeMillis());
			
			StringBuilder  content = new StringBuilder();

			content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	        content.append("<virtual-sensor name=\""+generatedSensorName+"\" priority=\"10\">\n");
	        content.append("<processing-class>\n");
	        content.append("<class-name>hr.fer.rasip.mqtt.processor.MqttWatchdogProcessor</class-name>\n");
	        content.append("    <init-params>\n");
	       	content.append("      <param name=\"delay\">" + delay + "</param>\n");
	       	content.append("      <param name=\"critical-period\">" + criticalPeriod + "</param>\n");
	       	content.append("      <param name=\"sensor-name\">" + sensor + "</param>\n");
	        content.append("    </init-params>\n");
	        content.append("    <output-structure />\n");
	        content.append("  </processing-class>\n");
	        content.append("  <description></description>\n");
	        content.append("  <addressing />\n");
	        content.append("  <storage history-size=\"1\" />\n");
	        content.append("  <streams>\n");
	        content.append("    <stream name=\"stream1\">\n");
	        content.append("      <source alias=\"source1\" storage-size=\"1\" sampling-rate=\"1\">\n");
	        content.append("        <address wrapper=\"local\">\n");
	        content.append("          <predicate key=\"query\">select * from " + sensor + "</predicate>\n");
	        content.append("        </address>\n");
	        content.append("        <query>select * from wrapper</query>\n");
	        content.append("      </source>\n");
	        content.append("      <query>select * from source1</query>\n");
	        content.append("    </stream>\n");
	        content.append("  </streams>*/\n");
	        content.append("</virtual-sensor>");
			
			

			FileOutputStream fop = null;
			File file;
			StringBuilder resp = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<response>\n");
			try {
	 
				file = new File("virtual-sensors/"+ generatedSensorName + ".xml");
				fop = new FileOutputStream(file);
	 
				// if file doesnt exists, then create it
				if (!file.exists()) {
					file.createNewFile();
				}
	 
				// get the content in bytes
				byte[] contentInBytes = content.toString().getBytes();
	 
				fop.write(contentInBytes);
				fop.flush();
				fop.close();
			} catch (IOException e) {
				resp.append("<status>exception</status>\n<description>Error while generating sensor</description>\n</response>");
				response.setHeader("Cache-Control", "no-store");
		        response.setDateHeader("Expires", 0);
		        response.setHeader("Pragma", "no-cache");
		        response.getWriter().write(resp.toString());
		        return;
			} finally {
				try {
					if (fop != null) {
						fop.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			resp.append("<status>ok</status>\n<description>Watchdog generated</description>\n</response>");
			response.setHeader("Cache-Control", "no-store");
	        response.setDateHeader("Expires", 0);
	        response.setHeader("Pragma", "no-cache");
	        response.getWriter().write(resp.toString());
			
		}
	}
	
}
