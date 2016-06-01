package hr.fer.rasip.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gsn.http.RequestHandler;


public class NotificationControllerHandler  implements RequestHandler  {

	
	private static final String CREATE_SERVLET_PATH = "/notifications/create";
	
	private static final String DELAY_PARAM = "delay";
	private static final String CRITICAL_VALUE = "criticalValue";
	private static final String CRITICAL_TYPE = "criticalType";
	private static final String FIELD = "selectedField";
	private static final String EMAIL = "email";
	private static final String SENSOR = "sensor";
	
	@Override
	public boolean isValid(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		if(request.getServletPath().equalsIgnoreCase(CREATE_SERVLET_PATH)){
			if(request.getParameter(DELAY_PARAM) == null)
				return false;
			
			if(request.getParameter(CRITICAL_VALUE) == null)
				return false;
			
			if(request.getParameter(CRITICAL_TYPE) == null)
				return false;
			
			if(request.getParameter(FIELD) == null)
				return false;
			
			if(request.getParameter(EMAIL) == null)
				return false;
			
			if(request.getParameter(SENSOR) == null)
				return false;
		}
			
		return true;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		String servletPath = request.getServletPath();
		
		if(servletPath.equalsIgnoreCase(CREATE_SERVLET_PATH)){
			
			String generatedSensorName = "notification"+Long.toString(System.currentTimeMillis());
			String criticalType = request.getParameter(CRITICAL_TYPE);
			
			String content;
			if(criticalType.equals("above"))
				content = generateNotificationAbove(request,generatedSensorName);
			else
				content = generateNotificationBelow(request, generatedSensorName);
			
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
	
	private String generateNotificationAbove(HttpServletRequest request, String generatedSensorName){
		
		String sensor = request.getParameter(SENSOR);
		String email = request.getParameter(EMAIL);
		Integer delay = Integer.parseInt(request.getParameter(DELAY_PARAM))*1000;
		Integer criticalValue = Integer.parseInt(request.getParameter(CRITICAL_VALUE));
		String field = request.getParameter(FIELD);
		String fieldUpper = field.toUpperCase();
		//String generatedSensorName = "notification"+Long.toString(System.currentTimeMillis());
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<virtual-sensor name=\""+generatedSensorName+"\" priority=\"10\">\n");
        sb.append("<processing-class>\n");
        sb.append("<class-name>gsn.processor.ScriptletProcessor</class-name>\n");
        sb.append("    <init-params>\n");
        sb.append("      <param name=\"persistant\">false</param>\n");
        sb.append("      <param name=\"notification-state\">0</param>\n");
		sb.append("	     <param name=\"mail-state\">0</param>\n");
        sb.append("      <param name=\"scriptlet\"><![CDATA[\n");
        sb.append("                    //this is a start of a scriptlet\n");
        sb.append("                    //data definition\n");
        sb.append("          def delay = "+ delay +"; //time in miliseconds between resending emails\n");
        sb.append("          def filePath =\"virtual-sensors/"+generatedSensorName+".xml\";\n");
        sb.append("          def recipients = [\""+email+"\"]; // Define one or more recipients\n");
        sb.append("          def criticalValue ="+ criticalValue+";\n");
        sb.append("          def measuringUnit = \""+ field +"\";\n");
        sb.append("          def state = notificationState; //notification state variable is defined in scriptlet from init-params\n");
		sb.append("			 def mail = mailState;\n");
        sb.append("          def nodePath =\"streams/stream/query\";\n");
        sb.append("          def emailTitle =\"\";\n");
        sb.append("          def emailContent=\"\";\n");
        sb.append("          def newQuery=\"\";\n");
		sb.append("			 if(mail == 1){\n");
		sb.append("				if(state == 0){\n");
		sb.append("					mail = 0;\n");
		sb.append("					updateMailState(filePath, mail);\n");
		sb.append("						}\n");
		sb.append("					 	else{\n");
		sb.append("							state = 2;\n");
		sb.append(" 						}\n");
		sb.append("					 }\n");
        sb.append("                    switch(state){\n");
        sb.append("                        case 0: def delayTime = TIMED + delay;\n");
        sb.append("                        newQuery = \"select \"+measuringUnit+\", timed from source1 where ((\"+measuringUnit+\">\"+criticalValue+\") and (timed >\" + delayTime + \")) or (\"+measuringUnit+\"<=\"+criticalValue+\")\";\n");
        sb.append("                        emailTitle = \"Warning!\";\n");
        sb.append("                        emailContent = \"Dear user,\\n\"+measuringUnit+\" measured on "+ sensor +" rised over "+criticalValue+"\";\n");
        sb.append("                        state++;\n");
        sb.append("                        break;\n");
        sb.append("                          case 1: if ("+fieldUpper+">criticalValue){\n");
        sb.append("                                def delayTime = TIMED + delay;\n");
        sb.append("                                newQuery = \"select \"+measuringUnit+\", timed from source1 where ((\"+measuringUnit+\">\"+criticalValue+\") and (timed >\" + delayTime + \")) or (\"+measuringUnit+\"<=\"+criticalValue+\")\";\n");
        sb.append("                                emailTitle = \"Warning again\";\n");
        sb.append("                                emailContent = \"Dear user,\\n\"+measuringUnit+\" measured on "+ sensor +" still over "+criticalValue+"\\nMeasured value is \" +" + fieldUpper + "+\"\\nDo something!\";\n");
        sb.append("                                  }\n");
        sb.append("                                  else{\n");
        sb.append("                                       newQuery = \"select \"+measuringUnit+\", timed from source1 where \"+measuringUnit+\">\"+criticalValue;\n");
        sb.append("                                       emailTitle = \"Everything OK\";\n");
        sb.append("                                       emailContent = \"Dear user,\\n\"+measuringUnit+\" measured on "+ sensor + " dropped under \"+criticalValue+\".\\nMeasured value is \"+" + fieldUpper + "+\"!!!\\nSensor readings are ok.\";\n");
        sb.append("                                       state=0;\n");
        sb.append("                                  }\n");
        sb.append("                                  break;\n");
		sb.append("						   case 2: if ("+ fieldUpper +"<=criticalValue){\n");
		sb.append("										newQuery = \"select \"+measuringUnit+\", timed from source1 where \"+measuringUnit+\">\"+criticalValue;\n");
		sb.append("										emailTitle = \"Everything OK\";\n");
		sb.append("										emailContent = \"Dear user,\\n\"+measuringUnit+\" measured on "+sensor +" rised over \"+criticalValue+\".\\nMeasured value is  \"+" + fieldUpper +"+\"!!!\\nSensor readings are ok.\";\n");
		sb.append("										state = 0;\n");
		sb.append("										mail = 0;\n");
		sb.append("										updateMailState(filePath,mail);\n");
		sb.append("								   }\n");
		sb.append("								   else{\n");
		sb.append("										newQuery = \"select \"+measuringUnit+\", timed from source1 where \"+measuringUnit+\"<=\"+criticalValue;\n");
		sb.append("								   }\n");
		sb.append("								   break;\n");
        sb.append("                    }\n");
		sb.append("					 if(mail != 1 && state != 2){\n");
        sb.append("                    	 sendEmail(recipients, emailTitle, emailContent);\n");
		sb.append("					 }\n");
        sb.append("                    updateNotificationVSXML(filePath,nodePath,newQuery,state);]]></param>\n");
        sb.append("    </init-params>\n");
        sb.append("    <output-structure />\n");
        sb.append("  </processing-class>\n");
        sb.append("  <description></description>\n");
        sb.append("  <addressing />\n");
        sb.append("  <storage history-size=\"1\" />\n");
        sb.append("  <streams>\n");
        sb.append("    <stream name=\"stream1\">\n");
        sb.append("      <source alias=\"source1\" storage-size=\"1\" sampling-rate=\"1\">\n");
        sb.append("        <address wrapper=\"local\">\n");
        sb.append("          <predicate key=\"query\">select * from " + sensor +"</predicate>\n");
        sb.append("        </address>\n");
        sb.append("        <query>select * from wrapper</query>\n");
        sb.append("      </source>\n");
        sb.append("      <query>select " + field + ", timed from source1 where " + field + "&gt;" + criticalValue + "</query>\n");
        sb.append("    </stream>\n");
        sb.append("  </streams>*/\n");
        sb.append("</virtual-sensor>");
        
        return sb.toString();
	}
	
	
	private String generateNotificationBelow(HttpServletRequest request, String generatedSensorName){
		
		String sensor = request.getParameter(SENSOR);
		String email = request.getParameter(EMAIL);
		Integer delay = Integer.parseInt(request.getParameter(DELAY_PARAM))*1000;
		Integer criticalValue = Integer.parseInt(request.getParameter(CRITICAL_VALUE));
		String field = request.getParameter(FIELD);
		String fieldUpper = field.toUpperCase();
		//String generatedSensorName = "notification"+Long.toString(System.currentTimeMillis());
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<virtual-sensor name=\""+generatedSensorName+"\" priority=\"10\">\n");
        sb.append("<processing-class>\n");
        sb.append("<class-name>gsn.processor.ScriptletProcessor</class-name>\n");
        sb.append("    <init-params>\n");
        sb.append("      <param name=\"persistant\">false</param>\n");
        sb.append("      <param name=\"notification-state\">0</param>\n");
		sb.append("	     <param name=\"mail-state\">0</param>\n");
        sb.append("      <param name=\"scriptlet\"><![CDATA[\n");
        sb.append("                    //this is a start of a scriptlet\n");
        sb.append("                    //data definition\n");
        sb.append("          def delay = "+ delay +"; //time in miliseconds between resending emails\n");
        sb.append("          def filePath =\"virtual-sensors/"+generatedSensorName+".xml\";\n");
        sb.append("          def recipients = [\""+email+"\"]; // Define one or more recipients\n");
        sb.append("          def criticalValue ="+ criticalValue+";\n");
        sb.append("          def measuringUnit = \""+ field +"\";\n");
        sb.append("          def state = notificationState; //notification state variable is defined in scriptlet from init-params\n");
		sb.append("			 def mail = mailState;\n");
        sb.append("          def nodePath =\"streams/stream/query\";\n");
        sb.append("          def emailTitle =\"\";\n");
        sb.append("          def emailContent=\"\";\n");
        sb.append("          def newQuery=\"\";\n");
		sb.append("			 if(mail == 1){\n");
		sb.append("				if(state == 0){\n");
		sb.append("					mail = 0;\n");
		sb.append("					updateMailState(filePath, mail);\n");
		sb.append("						}\n");
		sb.append("					 	else{\n");
		sb.append("							state = 2;\n");
		sb.append(" 						}\n");
		sb.append("					 }\n");
        sb.append("                    switch(state){\n");
        sb.append("                        case 0: def delayTime = TIMED + delay;\n");
        sb.append("                        newQuery = \"select \"+measuringUnit+\", timed from source1 where ((\"+measuringUnit+\"<\"+criticalValue+\") and (timed >\" + delayTime + \")) or (\"+measuringUnit+\">=\"+criticalValue+\")\";\n");
        sb.append("                        emailTitle = \"Warning!\";\n");
        sb.append("                        emailContent = \"Dear user,\\n\"+measuringUnit+\" measured on "+ sensor +" dropped under "+criticalValue+"\";\n");
        sb.append("                        state++;\n");
        sb.append("                        break;\n");
        sb.append("                          case 1: if ("+fieldUpper+"<criticalValue){\n");
        sb.append("                                def delayTime = TIMED + delay;\n");
        sb.append("                                newQuery = \"select \"+measuringUnit+\", timed from source1 where ((\"+measuringUnit+\"<\"+criticalValue+\") and (timed >\" + delayTime + \")) or (\"+measuringUnit+\">=\"+criticalValue+\")\";\n");
        sb.append("                                emailTitle = \"Warning again\";\n");
        sb.append("                                emailContent = \"Dear user,\\n\"+measuringUnit+\" measured on "+ sensor +" still under "+criticalValue+"\\nMeasured value is \"+" + fieldUpper + "+\"\\nDo something!\\n\";\n");
        sb.append("                                  }\n");
        sb.append("                                  else{\n");
        sb.append("                                       newQuery = \"select \"+measuringUnit+\", timed from source1 where \"+measuringUnit+\"<\"+criticalValue;\n");
        sb.append("                                       emailTitle = \"Everything OK\";\n");
        sb.append("                                       emailContent = \"Dear user,\\n\"+measuringUnit+\" measured on "+ sensor + " rised over \"+criticalValue+\".\\nMeasured value is \"+" + fieldUpper + "+\"!!!\\nSensor readings are ok.\";\n");
        sb.append("                                       state=0;\n");
        sb.append("                                  }\n");
        sb.append("                                  break;\n");
		sb.append("						   case 2: if ("+ fieldUpper +">=criticalValue){\n");
		sb.append("										newQuery = \"select \"+measuringUnit+\", timed from source1 where \"+measuringUnit+\"<\"+criticalValue;\n");
		sb.append("										emailTitle = \"Everything OK\";\n");
		sb.append("										emailContent = \"Dear user,\\n\"+measuringUnit+\" measured on "+sensor +" rised over \"+criticalValue+\".\\nMeasured value is  \"+" + fieldUpper +"+\"!!!\\nSensor readings are ok.\";\n");
		sb.append("										state = 0;\n");
		sb.append("										mail = 0;\n");
		sb.append("										updateMailState(filePath,mail);\n");
		sb.append("								   }\n");
		sb.append("								   else{\n");
		sb.append("										newQuery = \"select \"+measuringUnit+\", timed from source1 where \"+measuringUnit+\">=\"+criticalValue;\n");
		sb.append("								   }\n");
		sb.append("								   break;\n");
        sb.append("                    }\n");
		sb.append("					 if(mail != 1 && state != 2){\n");
        sb.append("                    	 sendEmail(recipients, emailTitle, emailContent);\n");
		sb.append("					 }\n");
        sb.append("                    updateNotificationVSXML(filePath,nodePath,newQuery,state);]]></param>\n");
        sb.append("    </init-params>\n");
        sb.append("    <output-structure />\n");
        sb.append("  </processing-class>\n");
        sb.append("  <description></description>\n");
        sb.append("  <addressing />\n");
        sb.append("  <storage history-size=\"1\" />\n");
        sb.append("  <streams>\n");
        sb.append("    <stream name=\"stream1\">\n");
        sb.append("      <source alias=\"source1\" storage-size=\"1\" sampling-rate=\"1\">\n");
        sb.append("        <address wrapper=\"local\">\n");
        sb.append("          <predicate key=\"query\">select * from " + sensor +"</predicate>\n");
        sb.append("        </address>\n");
        sb.append("        <query>select * from wrapper</query>\n");
        sb.append("      </source>\n");
        sb.append("      <query>select " + field + ", timed from source1 where " + field + "&lt;" + criticalValue + "</query>\n");
        sb.append("    </stream>\n");
        sb.append("  </streams>*/\n");
        sb.append("</virtual-sensor>");
        
        return sb.toString();
		
	}	
}





