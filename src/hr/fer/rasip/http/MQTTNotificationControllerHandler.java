package hr.fer.rasip.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gsn.http.RequestHandler;


public class MQTTNotificationControllerHandler  implements RequestHandler  {

	
	private static final String CREATE_SERVLET_PATH = "/notifications/create";
	
	private static final String PERIOD = "delay";
	private static final String CRITICAL_VALUE = "criticalValue";
	private static final String CRITICAL_TYPE = "criticalType";
	private static final String MONITORED_FIELD = "selectedField";
	private static final String SENSOR = "sensor";
	private static final String MQTT = "mqtt";
	
	@Override
	public boolean isValid(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		if(request.getServletPath().equalsIgnoreCase(CREATE_SERVLET_PATH)){
			if(request.getParameter(PERIOD) == null)
				return false;
			
			if(request.getParameter(CRITICAL_VALUE) == null)
				return false;
			
			if(request.getParameter(CRITICAL_TYPE) == null)
				return false;
			
			if(request.getParameter(MONITORED_FIELD) == null)
				return false;
			
			if(request.getParameter(SENSOR) == null)
				return false;

			if(request.getParameter(MQTT) == null)
				return false;
		}
			
		return true;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		String servletPath = request.getServletPath();
		
		if(servletPath.equalsIgnoreCase(CREATE_SERVLET_PATH)){
			
			String generatedSensorName = "MqttNotification"+Long.toString(System.currentTimeMillis());
			String criticalType = request.getParameter(CRITICAL_TYPE);
			
			String content;
			
			content = generateNotification(request,generatedSensorName);
		
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
			
			resp.append("<status>ok</status>\n<description>Notification generated</description>\n</response>");
			response.setHeader("Cache-Control", "no-store");
	        response.setDateHeader("Expires", 0);
	        response.setHeader("Pragma", "no-cache");
	        response.getWriter().write(resp.toString());
			
		}
	}
	
	private String generateNotification(HttpServletRequest request, String generatedSensorName){
		
		String sensor = request.getParameter(SENSOR);

		Integer delay = Integer.parseInt(request.getParameter(PERIOD))*1000;
		Integer criticalValue = Integer.parseInt(request.getParameter(CRITICAL_VALUE));
		String field = request.getParameter(MONITORED_FIELD).toUpperCase();
		String criticalType = request.getParameter(CRITICAL_TYPE);
		
		//String generatedSensorName = "notification"+Long.toString(System.currentTimeMillis());
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<virtual-sensor name=\""+generatedSensorName+"\" priority=\"10\">\n");
        sb.append("<processing-class>\n");
        sb.append("<class-name>hr.fer.rasip.mqtt.processor.MqttNotificationProcessor</class-name>\n");
        sb.append("    <init-params>\n");
       	sb.append("      <param name=\"period\">" + delay + "</param>\n");
       	sb.append("      <param name=\"delay\">" + delay + "</param>\n");
       	sb.append("      <param name=\"critical-value\">" + criticalValue + "</param>\n");
       	sb.append("      <param name=\"sensor-name\">" + sensor + "</param>\n");
       	sb.append("      <param name=\"critical-type\">" + criticalType + "</param>\n");
       	sb.append("      <param name=\"monitored-field\">" + field + "</param>\n");
        sb.append("    </init-params>\n");
        sb.append("    <output-structure />\n");
        sb.append("  </processing-class>\n");
        sb.append("  <description>VS for MQTT notifications</description>\n");
        sb.append("  <addressing />\n");
        sb.append("  <storage history-size=\"1\" />\n");
        sb.append("  <streams>\n");
        sb.append("    <stream name=\"stream1\">\n");
        sb.append("      <source alias=\"source1\" storage-size=\"1\" sampling-rate=\"1\">\n");
        sb.append("        <address wrapper=\"local\">\n");
        sb.append("          <predicate key=\"query\">select * from " + sensor + "</predicate>\n");
        sb.append("        </address>\n");
        sb.append("        <query>select * from wrapper</query>\n");
        sb.append("      </source>\n");
        sb.append("      <query>select * from source1</query>\n");
        sb.append("    </stream>\n");
        sb.append("  </streams>*/\n");
        sb.append("</virtual-sensor>");
        
        return sb.toString();
	}
	
	
}





